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

package org.viewer.hub.back.model.monitoring;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Search criteria of the monitoring: all the criteria are optional and combined with AND
 */
@Getter
@Setter
public class DisplayRequestMonitoringFilter {

	private LocalDateTime from;

	private LocalDateTime to;

	private Set<String> viewers = new HashSet<>();

	/** Matches the user of the JWT or the user provided in the request (contains) */
	private String user;

	private String patientId;

	private String studyUid;

	private String seriesUid;

	private String objectUid;

	private String accessionNumber;

	/** PATIENT, STUDY, SERIES or INSTANCE */
	private String queryLevel;

	private Boolean success;

	private String archive;

	private String host;

	private String client;

	public DisplayRequestMonitoringFilter copy() {
		DisplayRequestMonitoringFilter c = new DisplayRequestMonitoringFilter();
		c.from = this.from;
		c.to = this.to;
		c.viewers = new HashSet<>(this.viewers);
		c.user = this.user;
		c.patientId = this.patientId;
		c.studyUid = this.studyUid;
		c.seriesUid = this.seriesUid;
		c.objectUid = this.objectUid;
		c.accessionNumber = this.accessionNumber;
		c.queryLevel = this.queryLevel;
		c.success = this.success;
		c.archive = this.archive;
		c.host = this.host;
		c.client = this.client;
		return c;
	}

}