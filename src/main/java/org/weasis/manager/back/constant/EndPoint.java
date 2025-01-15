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

package org.weasis.manager.back.constant;

/**
 * Paths for endpoints
 */
public class EndPoint {

	// Paths
	private static final String PREFIX_PATH = "/weasisconfig/ws";

	// Group
	public static final String GROUP_PATH = PREFIX_PATH + "/group";

	// LaunchConfig
	public static final String LAUNCH_CONFIG_PATH = PREFIX_PATH + "/launchConfig";

	// LaunchPreference and Preference
	public static final String PREFERENCES_PATH = PREFIX_PATH + "/preferences";

	// Module
	public static final String MODULES_PATH = PREFIX_PATH + "/weasismodules";

	// Target
	public static final String TARGET_PATH = PREFIX_PATH + "/target";

	// Statistics
	public static final String STATISTIC_PATH = PREFIX_PATH + "/statistic";

	// Spring doc
	public static final String SPRING_DOC_PATH = "/swagger-ui/index.html?url=/weasisconfig/v3/api-docs";

	// Manifest
	public static final String MANIFEST_PATH = "/manifest";

	// Display
	public static final String DISPLAY_PATH = "/display";

	public static final String WEASIS_PATH = "/weasis";

	public static final String AUTH_WEASIS_PATH = "/auth/weasis";

	public static final String IHE_INVOKE_IMAGE_DISPLAY_PATH = "/IHEInvokeImageDisplay";

	public static final String AUTH_IHE_INVOKE_IMAGE_DISPLAY_PATH = "/auth/IHEInvokeImageDisplay";

	// Override package configuration
	public static final String OVERRIDE_CONFIG_PATH = "/overrideConfig";

	public static final String ALL_REMAINING_PATH = "/**";

}
