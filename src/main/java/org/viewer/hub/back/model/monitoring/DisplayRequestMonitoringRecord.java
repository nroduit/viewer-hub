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

import lombok.Builder;
import lombok.Getter;

/**
 * Data of a request to record
 */
@Builder
@Getter
public class DisplayRequestMonitoringRecord {

	private final String viewer;

	private final String requestType;

	private final String queryLevel;

	private final String httpMethod;

	private final boolean authenticated;

	private final String jwtUser;

	private final String requestUser;

	private final String host;

	private final String client;

	private final String selectionRule;

	private final String archive;

	private final String patientId;

	private final String studyUid;

	private final String seriesUid;

	private final String objectUid;

	private final String accessionNumber;

	private final long durationMs;

	private final boolean success;

	private final String errorMessage;

}