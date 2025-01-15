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

package org.weasis.manager.back.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesLoader {

	private static final String LAUNCH_PROPERTIES_FILENAME = "launchConfig.properties";

	private static final Properties launchProperties = new Properties();

	private static LinkedMultiValueMap<String, String> launchPropertyMap = null;

	public static void loadProperties() {
		LOG.debug("Load properties");

		try (InputStream is = Thread.currentThread()
			.getContextClassLoader()
			.getResourceAsStream(LAUNCH_PROPERTIES_FILENAME)) {
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

}
