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

package org.viewer.hub.back.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.model.monitoring.ActivityDay;
import org.viewer.hub.back.model.monitoring.DashboardData;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringFilter;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringRecord;

import java.time.ZoneId;
import java.util.List;

/**
 * Service recording and querying the viewer launch requests (monitoring)
 */
public interface DisplayRequestMonitoringService {

	/**
	 * Record a viewer launch call from its search criteria and outcome. Never throws:
	 * monitoring must not break the launch of a viewer
	 * @param searchCriteria criteria of the call
	 * @param authentication authentication of the caller
	 * @param viewerType viewer selected (can be null)
	 * @param selectionRule description of the selection rule applied (can be null)
	 * @param startNanos {@link System#nanoTime()} at the start of the call
	 * @param error error raised by the call, null on success
	 */
	void recordRequest(SearchCriteria searchCriteria, Authentication authentication, ViewerType viewerType,
			String selectionRule, long startNanos, RuntimeException error);

	/**
	 * Record a request. Never throws: monitoring must not break the launch of a viewer
	 * @param request request to record
	 */
	void registerRequest(DisplayRequestMonitoringRecord request);

	/**
	 * Time zone of the recorded dates (property viewer-hub.monitoring.time-zone, server
	 * time zone by default)
	 * @return the time zone
	 */
	ZoneId retrieveTimeZone();

	/**
	 * Search the requests (activity tab)
	 * @param filter criteria
	 * @param pageable pagination and sort
	 * @return page of requests
	 */
	Page<DisplayRequestMonitoringEntity> search(DisplayRequestMonitoringFilter filter, Pageable pageable);

	/**
	 * Count the requests matching the criteria
	 * @param filter criteria
	 * @return number of requests
	 */
	long count(DisplayRequestMonitoringFilter filter);

	/**
	 * Days having requests matching the criteria, most recent first
	 * @param filter criteria
	 * @return number of requests by day
	 */
	List<ActivityDay> activityDays(DisplayRequestMonitoringFilter filter);

	/**
	 * Aggregate the requests by viewer and over the time (dashboard tab)
	 * @param filter criteria
	 * @return aggregated data
	 */
	DashboardData dashboard(DisplayRequestMonitoringFilter filter);

	/**
	 * Delete all the recorded requests (reset of the statistics)
	 * @return number of requests deleted
	 */
	long deleteAll();

	byte[] exportActivityCsv(DisplayRequestMonitoringFilter filter);

	byte[] exportActivityJson(DisplayRequestMonitoringFilter filter);

	byte[] exportDashboardCsv(DisplayRequestMonitoringFilter filter);

	byte[] exportDashboardJson(DisplayRequestMonitoringFilter filter);

}