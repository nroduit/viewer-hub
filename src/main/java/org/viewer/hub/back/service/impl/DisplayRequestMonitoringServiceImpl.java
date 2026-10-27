/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.service.impl;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;
import org.viewer.hub.back.model.monitoring.ActivityDay;
import org.viewer.hub.back.model.monitoring.ArchiveStatistics;
import org.viewer.hub.back.model.monitoring.DashboardData;
import org.viewer.hub.back.model.monitoring.TimeBucket;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringFilter;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringRecord;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringRow;
import org.viewer.hub.back.model.monitoring.ViewerAccumulator;
import org.viewer.hub.back.model.monitoring.ViewerStatistics;
import org.viewer.hub.back.repository.DisplayRequestMonitoringRepository;
import org.viewer.hub.back.repository.specification.DisplayRequestMonitoringSpecification;
import org.viewer.hub.back.service.DisplayRequestMonitoringService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DisplayRequestMonitoringServiceImpl implements DisplayRequestMonitoringService {

	private static final int MAX_EXPORT_ROWS = 100_000;

	private static final String UNKNOWN = "UNKNOWN";

	private static final int MAX_BUCKETS = 60;

	// Sizes (minutes) dividing a day, so that buckets are aligned on the clock
	private static final int[] BUCKET_MINUTES = { 1, 5, 15, 30, 60, 180, 360, 720, 1440 };

	private static final int QUEUE_CAPACITY = 10_000;

	private static final int BATCH_SIZE = 500;

	private final BlockingQueue<DisplayRequestMonitoringEntity> pendingRequests = new LinkedBlockingQueue<>(
			QUEUE_CAPACITY);

	private final DisplayRequestMonitoringRepository displayRequestMonitoringRepository;

	private final ConnectorService connectorService;

	private final DisplayRequestMonitoringExporter exporter = new DisplayRequestMonitoringExporter();

	/** Time zone of the recorded dates (default: the one of the server) */
	@Value("${viewer-hub.monitoring.time-zone:}")
	private String timeZone = "";

	/** Rolling retention of the recorded requests, in months */
	@Value("${viewer-hub.monitoring.retention-months:3}")
	private int retentionMonths = 3;

	@Autowired
	public DisplayRequestMonitoringServiceImpl(DisplayRequestMonitoringRepository displayRequestMonitoringRepository,
			ConnectorService connectorService) {
		this.displayRequestMonitoringRepository = displayRequestMonitoringRepository;
		this.connectorService = connectorService;
	}

	@Override
	public void recordRequest(SearchCriteria searchCriteria, Authentication authentication, ViewerType viewerType,
			String selectionRule, long startNanos, RuntimeException error) {
		try {
			DisplayRequestMonitoringRecord.DisplayRequestMonitoringRecordBuilder call = DisplayRequestMonitoringRecord
				.builder()
				.viewer(viewerType != null ? viewerType.getCode() : null)
				.jwtUser(SecurityUtil.resolveUsername(authentication))
				.requestUser(searchCriteria.getUser())
				.host(searchCriteria.getHost())
				.client(searchCriteria.getClient())
				.archive(resolveArchive(searchCriteria))
				.selectionRule(selectionRule)
				.durationMs((System.nanoTime() - startNanos) / 1_000_000)
				.success(error == null)
				.errorMessage(error != null
						? StringUtils.defaultIfBlank(error.getMessage(), error.getClass().getSimpleName()) : null);

			RequestIds ids = switch (searchCriteria) {
				case ArchiveSearchCriteria archiveCriteria -> new RequestIds("REGULAR", archiveCriteria.getPatientID(),
						archiveCriteria.getStudyUID(), archiveCriteria.getSeriesUID(), archiveCriteria.getObjectUID(),
						archiveCriteria.getAccessionNumber());
				case IHESearchCriteria iheCriteria -> new RequestIds("IHE",
						StringUtils.isBlank(iheCriteria.getPatientID()) ? Set.of() : Set.of(iheCriteria.getPatientID()),
						iheCriteria.getStudyUID(), Set.of(), Set.of(), iheCriteria.getAccessionNumber());
				default -> new RequestIds(null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
			};
			call.requestType(ids.requestType())
				.patientId(join(ids.patientIds()))
				.studyUid(join(ids.studyUids()))
				.seriesUid(join(ids.seriesUids()))
				.objectUid(join(ids.objectUids()))
				.accessionNumber(join(ids.accessionNumbers()));
			if (!isEmpty(ids.objectUids())) {
				call.queryLevel("INSTANCE");
			}
			else if (!isEmpty(ids.seriesUids())) {
				call.queryLevel("SERIES");
			}
			else if (!isEmpty(ids.studyUids()) || !isEmpty(ids.accessionNumbers())) {
				call.queryLevel("STUDY");
			}
			else if (!isEmpty(ids.patientIds())) {
				call.queryLevel("PATIENT");
			}

			if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
				call.httpMethod(attributes.getRequest().getMethod());
				// Authenticated endpoint = handler method protected by @PreAuthorize
				call.authenticated(attributes.getRequest()
					.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) instanceof HandlerMethod handler
						&& handler.hasMethodAnnotation(PreAuthorize.class));
			}
			registerRequest(call.build());
		}
		catch (Exception e) {
			LOG.warn("Unable to build the monitoring record of the viewer call", e);
		}
	}

	@Override
	public void registerRequest(DisplayRequestMonitoringRecord request) {
		try {
			DisplayRequestMonitoringEntity entity = new DisplayRequestMonitoringEntity();
			entity.setCreatedAt(LocalDateTime.now(zone()));
			entity.setViewer(truncate(request.getViewer(), 100));
			entity.setRequestType(truncate(request.getRequestType(), 20));
			entity.setQueryLevel(truncate(request.getQueryLevel(), 20));
			entity.setHttpMethod(truncate(request.getHttpMethod(), 10));
			entity.setAuthenticated(request.isAuthenticated());
			entity.setJwtUser(truncate(request.getJwtUser(), 255));
			entity.setRequestUser(truncate(request.getRequestUser(), 255));
			entity.setHost(truncate(request.getHost(), 255));
			entity.setClient(truncate(request.getClient(), 255));
			entity.setSelectionRule(truncate(request.getSelectionRule(), 500));
			entity.setArchive(truncate(request.getArchive(), 1000));
			entity.setPatientId(truncate(request.getPatientId(), 1000));
			entity.setStudyUid(truncate(request.getStudyUid(), 2000));
			entity.setSeriesUid(truncate(request.getSeriesUid(), 2000));
			entity.setObjectUid(truncate(request.getObjectUid(), 2000));
			entity.setAccessionNumber(truncate(request.getAccessionNumber(), 1000));
			entity.setDurationMs(request.getDurationMs());
			entity.setSuccess(request.isSuccess());
			entity.setErrorMessage(truncate(request.getErrorMessage(), 1000));
			if (!this.pendingRequests.offer(entity)) {
				LOG.warn("Monitoring queue is full ({} requests), the request is not recorded", QUEUE_CAPACITY);
			}
		}
		catch (Exception e) {
			LOG.warn("Unable to record the viewer request in the monitoring", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DisplayRequestMonitoringEntity> search(DisplayRequestMonitoringFilter filter, Pageable pageable) {
		return this.displayRequestMonitoringRepository.findAll(toSpecification(filter), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public long count(DisplayRequestMonitoringFilter filter) {
		return this.displayRequestMonitoringRepository.count(toSpecification(filter));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityDay> activityDays(DisplayRequestMonitoringFilter filter) {
		Map<LocalDate, Long> byDay = new TreeMap<>(Comparator.reverseOrder());
		loadRows(filter).forEach(r -> byDay.merge(r.getCreatedAt().toLocalDate(), 1L, Long::sum));
		return byDay.entrySet().stream().map(e -> new ActivityDay(e.getKey(), e.getValue())).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public DashboardData dashboard(DisplayRequestMonitoringFilter filter) {
		return buildDashboard(filter, null);
	}

	@Override
	@Transactional
	public long deleteAll() {
		long count = this.displayRequestMonitoringRepository.count();
		this.displayRequestMonitoringRepository.deleteAllInBatch();
		return count;
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] exportActivityCsv(DisplayRequestMonitoringFilter filter) {
		return this.exporter.activityCsv(loadForExport(filter));
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] exportActivityJson(DisplayRequestMonitoringFilter filter) {
		return this.exporter.activityJson(loadForExport(filter));
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] exportDashboardCsv(DisplayRequestMonitoringFilter filter) {
		return this.exporter.dashboardCsv(buildDashboard(filter, null));
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] exportDashboardJson(DisplayRequestMonitoringFilter filter) {
		return this.exporter.dashboardJson(buildDashboard(filter, Duration.ofDays(1)));
	}

	/**
	 * Write the queued requests in the database by batches, in one transaction per batch
	 */
	@Scheduled(fixedDelayString = "${viewer-hub.monitoring.flush-delay-ms:2000}")
	@PreDestroy
	public void flush() {
		List<DisplayRequestMonitoringEntity> batch = new ArrayList<>(BATCH_SIZE);
		while (this.pendingRequests.drainTo(batch, BATCH_SIZE) > 0) {
			try {
				this.displayRequestMonitoringRepository.saveAll(batch);
			}
			catch (Exception e) {
				LOG.warn("Unable to record {} viewer request(s) in the monitoring", batch.size(), e);
			}
			batch = new ArrayList<>(BATCH_SIZE);
		}
	}

	/**
	 * Rolling retention: delete the requests older than the configured number of months
	 * (property viewer-hub.monitoring.retention-months, 0 or less to keep everything)
	 */
	@Scheduled(cron = "${viewer-hub.monitoring.purge-cron:0 30 3 * * *}")
	public void purgeOldRequests() {
		if (this.retentionMonths <= 0) {
			return;
		}
		LocalDateTime limit = LocalDateTime.now(zone()).minusMonths(this.retentionMonths);
		try {
			int deleted = this.displayRequestMonitoringRepository.deleteOlderThan(limit);
			if (deleted > 0) {
				LOG.info("Monitoring: {} request(s) older than {} deleted", deleted, limit);
			}
		}
		catch (Exception e) {
			LOG.warn("Unable to purge the old monitoring requests", e);
		}
	}

	/**
	 * Archives requested or, when none is requested, the archives actually used (default
	 * connectors)
	 */
	private String resolveArchive(SearchCriteria searchCriteria) {
		if (!isEmpty(searchCriteria.getArchive())) {
			return join(searchCriteria.getArchive());
		}
		if (this.connectorService == null) {
			return null;
		}
		return join(this.connectorService.retrieveConnectors(new LinkedHashSet<>())
			.stream()
			.map(ConnectorProperty::getId)
			.toList());
	}

	private record RequestIds(String requestType, Set<String> patientIds, Set<String> studyUids, Set<String> seriesUids,
			Set<String> objectUids, Set<String> accessionNumbers) {
	}

	@Override
	public ZoneId retrieveTimeZone() {
		return zone();
	}

	private ZoneId zone() {
		return StringUtils.isBlank(this.timeZone) ? ZoneId.systemDefault() : ZoneId.of(this.timeZone.trim());
	}

	private static boolean isEmpty(Collection<String> values) {
		return values == null || values.isEmpty();
	}

	private static String join(Collection<String> values) {
		if (isEmpty(values)) {
			return null;
		}
		// DICOM multi-value separator can be left at the end of a value (e.g. "1111\")
		String joined = values.stream()
			.map(value -> StringUtils.strip(value, "\\ "))
			.filter(StringUtils::isNotEmpty)
			.collect(Collectors.joining(","));
		return joined.isEmpty() ? null : joined;
	}

	private DashboardData buildDashboard(DisplayRequestMonitoringFilter filter, Duration fixedBucketSize) {
		List<DisplayRequestMonitoringRow> rows = loadRows(filter);

		Map<String, ViewerAccumulator> byViewer = new TreeMap<>();
		Map<String, ViewerAccumulator> byArchive = new TreeMap<>();
		Set<String> allUsers = new HashSet<>();
		for (DisplayRequestMonitoringRow row : rows) {
			String user = StringUtils.defaultIfBlank(row.getJwtUser(), row.getRequestUser());
			accumulators(byViewer, StringUtils.defaultIfBlank(row.getViewer(), UNKNOWN)).add(row.getCreatedAt(),
					row.getDurationMs(), row.isSuccess(), user);
			accumulators(byArchive, StringUtils.defaultIfBlank(row.getArchive(), UNKNOWN)).add(row.getCreatedAt(),
					row.getDurationMs(), row.isSuccess(), user);
			if (user != null) {
				allUsers.add(user);
			}
		}
		long errors = rows.stream().filter(r -> !r.isSuccess()).count();
		long authenticated = rows.stream().filter(DisplayRequestMonitoringRow::isAuthenticated).count();
		long totalDuration = rows.stream().mapToLong(DisplayRequestMonitoringRow::getDurationMs).sum();
		LocalDateTime min = rows.stream()
			.map(DisplayRequestMonitoringRow::getCreatedAt)
			.min(Comparator.naturalOrder())
			.orElse(null);
		LocalDateTime max = rows.stream()
			.map(DisplayRequestMonitoringRow::getCreatedAt)
			.max(Comparator.naturalOrder())
			.orElse(null);

		List<ViewerStatistics> viewers = byViewer.values()
			.stream()
			.map(ViewerAccumulator::toViewerStatistics)
			.sorted(Comparator.comparingLong(ViewerStatistics::requests).reversed())
			.toList();

		List<ArchiveStatistics> archives = byArchive.values()
			.stream()
			.map(ViewerAccumulator::toArchiveStatistics)
			.sorted(Comparator.comparingLong(ArchiveStatistics::requests).reversed())
			.toList();

		LocalDateTime start = ObjectUtils.firstNonNull(filter.getFrom(), min);
		LocalDateTime end = ObjectUtils.firstNonNull(filter.getTo(), max);
		Duration bucketSize = fixedBucketSize != null ? fixedBucketSize : chooseBucketSize(start, end);
		List<TimeBucket> timeline = buildTimeline(rows, start, end, bucketSize);

		return new DashboardData(rows.size(), errors, authenticated,
				rows.isEmpty() ? 0 : (double) totalDuration / rows.size(), allUsers.size(), viewers, archives, timeline,
				bucketSize);
	}

	private static ViewerAccumulator accumulators(Map<String, ViewerAccumulator> accumulators, String name) {
		return accumulators.computeIfAbsent(name, ViewerAccumulator::new);
	}

	private List<DisplayRequestMonitoringEntity> loadForExport(DisplayRequestMonitoringFilter filter) {
		return this.displayRequestMonitoringRepository
			.findAll(toSpecification(filter),
					PageRequest.of(0, MAX_EXPORT_ROWS, Sort.by(Sort.Direction.DESC, "createdAt")))
			.getContent();
	}

	private List<DisplayRequestMonitoringRow> loadRows(DisplayRequestMonitoringFilter filter) {
		return this.displayRequestMonitoringRepository.findBy(toSpecification(filter),
				q -> q.as(DisplayRequestMonitoringRow.class).all());
	}

	private static Specification<DisplayRequestMonitoringEntity> toSpecification(
			DisplayRequestMonitoringFilter filter) {
		return new DisplayRequestMonitoringSpecification(filter);
	}

	private static Duration chooseBucketSize(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			return Duration.ofMinutes(BUCKET_MINUTES[0]);
		}
		long spanMinutes = Math.max(1,
				ChronoUnit.MINUTES.between(start.atZone(ZoneId.systemDefault()), end.atZone(ZoneId.systemDefault())));
		for (int minutes : BUCKET_MINUTES) {
			if (spanMinutes / minutes <= MAX_BUCKETS) {
				return Duration.ofMinutes(minutes);
			}
		}
		return Duration.ofDays(1);
	}

	private static LocalDateTime bucketStart(LocalDateTime time, Duration size) {
		long bucketMinutes = size.toMinutes();
		LocalDateTime dayStart = time.truncatedTo(ChronoUnit.DAYS);
		long minuteOfDay = time.toLocalTime().toSecondOfDay() / 60;
		return dayStart.plusMinutes(minuteOfDay / bucketMinutes * bucketMinutes);
	}

	private static List<TimeBucket> buildTimeline(List<DisplayRequestMonitoringRow> rows, LocalDateTime start,
			LocalDateTime end, Duration size) {
		if (rows.isEmpty() || start == null || end == null) {
			return List.of();
		}
		Map<LocalDateTime, Map<String, Long>> buckets = new TreeMap<>();
		// Fill the empty slots so that the chart keeps a continuous time axis
		LocalDateTime last = bucketStart(end, size);
		for (LocalDateTime t = bucketStart(start, size); !t.isAfter(last); t = t.plus(size)) {
			buckets.put(t, new LinkedHashMap<>());
		}
		Map<LocalDateTime, LinkedHashMap<String, Long>> counts = rows.stream()
			.collect(Collectors.groupingBy(row -> bucketStart(row.getCreatedAt(), size), TreeMap::new,
					Collectors.groupingBy(row -> StringUtils.defaultIfBlank(row.getViewer(), UNKNOWN),
							LinkedHashMap::new, Collectors.counting())));
		buckets.putAll(counts);
		return buckets.entrySet().stream().map(e -> new TimeBucket(e.getKey(), e.getValue())).toList();
	}

	private static String truncate(String value, int max) {
		return value != null && value.length() > max ? value.substring(0, max) : value;
	}

}