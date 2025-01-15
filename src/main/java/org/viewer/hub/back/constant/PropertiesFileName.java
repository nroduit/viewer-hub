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

public class PropertiesFileName {

	public static final String PATH_CONF_FOLDER = "/conf";

	public static final String CONFIG_PROPERTIES_FILENAME = "config.properties";

	public static final String BASE_JSON_FILENAME = "base.json";

	public static final String EXT_CONFIG_PROPERTIES_FILENAME = "ext-config.properties";

	public static final String EXT_PATTERN_NAME = "ext-";

	public static final String EXTENSION_PROPERTIES_FILE = ".properties";

	public static final String EXTENSION_JSON_FILE = ".json";

	public static final String I18N_PATTERN_NAME = "weasis-i18n-dist-";

	public static final String ZIP_EXTENSION = ".zip";

	public static final String PACKAGE_PATTERN_NAME = "weasis-native";

	public static final String RESOURCES_FOLDER_NAME = "resources";

	public static final String CONF_FOLDER_NAME = "conf";

	public static final String RESOURCES_ZIP_FILE_NAME = "resources.zip";

	public static final String VERSION_COMPATIBILITY_FILE_NAME = "version-compatibility.json";

	public static final String BIN_DIST_WEASIS_PATH = "bin-dist/weasis/";

	public static final String BIN_DIST_WEASIS_RESOURCES_PATH = "bin-dist/weasis/resources/";

	public static final String BIN_DIST_WEASIS_CONF_CONFIG_PROPERTIES_FILE_PATH = "%s%s/%s"
		.formatted(BIN_DIST_WEASIS_PATH, CONF_FOLDER_NAME, CONFIG_PROPERTIES_FILENAME);

	public static final String BIN_DIST_WEASIS_CONF_BASE_JSON_FILE_PATH = "%s%s/%s".formatted(BIN_DIST_WEASIS_PATH,
			CONF_FOLDER_NAME, BASE_JSON_FILENAME);

}
