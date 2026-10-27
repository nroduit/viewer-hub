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

import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;
import org.viewer.hub.back.model.monitoring.ArchiveStatistics;
import org.viewer.hub.back.model.monitoring.DashboardData;
import org.viewer.hub.back.model.monitoring.TimeBucket;
import org.viewer.hub.back.model.monitoring.ViewerStatistics;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates the monitoring export files (CSV and JSON) from already loaded data.
 */
class DisplayRequestMonitoringExporter {

	private static final String[] CSV_ACTIVITY_HEADER = { "id", "createdAt", "viewer", "requestType", "queryLevel",
			"httpMethod", "authenticated", "jwtUser", "requestUser", "host", "client", "archive", "selectionRule",
			"patientId", "studyUid", "seriesUid", "objectUid", "accessionNumber", "durationMs", "success",
			"errorMessage" };

	private final ObjectMapper objectMapper = JsonMapper.builder().build();

	byte[] activityCsv(List<DisplayRequestMonitoringEntity> requests) {
		StringBuilder sb = new StringBuilder();
		appendCsvLine(sb, (Object[]) CSV_ACTIVITY_HEADER);
		for (DisplayRequestMonitoringEntity c : requests) {
			appendCsvLine(sb, c.getId(), c.getCreatedAt(), c.getViewer(), c.getRequestType(), c.getQueryLevel(),
					c.getHttpMethod(), c.isAuthenticated(), c.getJwtUser(), c.getRequestUser(), c.getHost(),
					c.getClient(), c.getArchive(), c.getSelectionRule(), c.getPatientId(), c.getStudyUid(),
					c.getSeriesUid(), c.getObjectUid(), c.getAccessionNumber(), c.getDurationMs(), c.isSuccess(),
					c.getErrorMessage());
		}
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

	byte[] activityJson(List<DisplayRequestMonitoringEntity> requests) {
		return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(requests);
	}

	byte[] dashboardCsv(DashboardData data) {
		StringBuilder sb = new StringBuilder();
		appendCsvLine(sb, "viewer", "requests", "errors", "errorRatePercent", "averageDurationMs", "maxDurationMs",
				"distinctUsers", "lastRequest");
		for (ViewerStatistics s : data.viewers()) {
			appendCsvLine(sb, s.viewer(), s.requests(), s.errors(), String.format("%.2f", s.errorRatePercent()),
					String.format("%.1f", s.averageDurationMs()), s.maxDurationMs(), s.distinctUsers(),
					s.lastRequest());
		}
		sb.append('\n');
		appendCsvLine(sb, "archive", "requests", "errors", "errorRatePercent", "averageDurationMs", "maxDurationMs",
				"distinctUsers", "lastRequest");
		for (ArchiveStatistics s : data.archives()) {
			appendCsvLine(sb, s.archive(), s.requests(), s.errors(), String.format("%.2f", s.errorRatePercent()),
					String.format("%.1f", s.averageDurationMs()), s.maxDurationMs(), s.distinctUsers(),
					s.lastRequest());
		}
		sb.append('\n');
		appendCsvLine(sb, "bucketStart", "viewer", "requests");
		for (TimeBucket b : data.timeline()) {
			b.requestsByViewer().forEach((viewer, requests) -> appendCsvLine(sb, b.start(), viewer, requests));
		}
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

	byte[] dashboardJson(DashboardData data) {
		return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
	}

	private static void appendCsvLine(StringBuilder sb, Object... values) {
		sb.append(Arrays.stream(values).map(DisplayRequestMonitoringExporter::csvCell).collect(Collectors.joining(",")))
			.append('\n');
	}

	private static String csvCell(Object value) {
		if (value == null) {
			return "";
		}
		String text = value.toString();
		// Prevent formula injection when the file is opened in a spreadsheet
		if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0 && !(value instanceof Number)) {
			text = "'" + text;
		}
		return "\"" + text.replace("\"", "\"\"") + "\"";
	}

}
