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

import jakarta.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONReader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class JsonUtil {

	/** Private constructor to hide the implicit one */
	private JsonUtil() {
	}

	/**
	 * Transform json string to list of dcm4chee attributes
	 * @param json Json to transform
	 * @return List of Attributes
	 */
	public static List<Attributes> transformJsonToAttributes(String json) {
		List<Attributes> attributes = new ArrayList<>();
		if (json != null) {
			JSONReader jsonReader = new JSONReader(
					Json.createParser(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))));
			jsonReader.readDatasets((fmi, dataset) -> {
				if (dataset != null) {
					if (fmi != null) {
						dataset.addAll(fmi);
					}
					attributes.add(dataset);
				}
			});
		}
		return attributes;
	}

}
