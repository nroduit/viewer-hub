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

import java.time.LocalDateTime;

/**
 * Statistics of the requests of one viewer
 */
public record ViewerStatistics(String viewer, long requests, long errors, double averageDurationMs, long maxDurationMs,
		long distinctUsers, LocalDateTime lastRequest) {

	public double errorRatePercent() {
		return requests == 0 ? 0 : errors * 100.0 / requests;
	}
}