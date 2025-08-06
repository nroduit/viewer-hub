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

package org.viewer.hub.back.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.enums.Viewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesLoader {

	private static final String WEASIS_LAUNCH_PROPERTIES_FILENAME = "weasisLaunchConfig.properties";

	private static final Properties launchProperties = new Properties();

	private static LinkedMultiValueMap<String, String> launchPropertyMap = null;

	public static void loadProperties() {
		LOG.debug("Load properties");

		try {
			loadConfigFile(Viewer.WEASIS);
		}
		catch (Exception e) {
			LOG.error("Error when loading properties");
		}
	}

	public static MultiValueMap<String, String> getNewLaunchPropertyMap() {
		if (launchPropertyMap == null) {
			loadProperties();
		}
		// Deep copy in order to not interfere with initial property map
		return launchPropertyMap.deepCopy();
	}

	private static void loadConfigFile(Viewer viewer) throws IOException {
		InputStream is = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(getConfigFile(viewer));
		launchProperties.load(is);

		launchPropertyMap = new LinkedMultiValueMap<>();

		launchProperties.forEach((key, value) -> {
			String[] splitProp = ((String) key).split("^property_");
			if (splitProp.length == 2) {
				key = "pro";
				value = String.format("%s %s", splitProp[1], value);
			}
			launchPropertyMap.add((String) key, (String) value);
		});
	}

	private static String getConfigFile(Viewer viewer) {
        return switch (viewer) {
            case WEASIS -> WEASIS_LAUNCH_PROPERTIES_FILENAME;
            default -> null;
        };
    }

}
