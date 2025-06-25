/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
