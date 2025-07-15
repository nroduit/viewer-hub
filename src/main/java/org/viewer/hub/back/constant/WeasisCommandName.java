/*
 *  Copyright (c) 2022-2025 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.constant;

public final class WeasisCommandName {

	public static final String WEASIS_CONFIG_COMMAND = "$weasis:config wcfg=";

	public static final String WEASIS_DICOM_GET_MANIFEST_COMMAND = "$dicom:get -w";
	
	public static final String WEASIS_DICOM_RS_COMMAND = "$dicom:rs";

	// Protocol for launching weasis
	public static final String LAUNCH_URL_WEASIS_COMMANDS_CONFIG = "weasis://%s";

	private WeasisCommandName() {
		// Private constructor to hide implicit one
	}

}
