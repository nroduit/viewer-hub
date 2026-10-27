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
 * Projection of a display request limited to the columns needed by the dashboard.
 */
public interface DisplayRequestMonitoringRow {

	LocalDateTime getCreatedAt();

	String getViewer();

	String getArchive();

	long getDurationMs();

	boolean isSuccess();

	boolean isAuthenticated();

	String getJwtUser();

	String getRequestUser();

}
