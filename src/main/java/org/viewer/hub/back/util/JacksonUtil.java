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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.WeasisPropertyEntity;
import org.viewer.hub.back.model.version.MinimalReleaseVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public class JacksonUtil {

	/**
	 * Transform object in Json String
	 * @param objectToSerialize object to serialize
	 * @return String built
	 */
	public static String serializeIntoJson(Object objectToSerialize) {
		String objectSerialized = null;
		try {
			objectSerialized = new ObjectMapper().writeValueAsString(objectToSerialize);
		}
		catch (JsonProcessingException e) {
			LOG.error("Issue when serializing:%s".formatted(e.getMessage()));
		}
		return objectSerialized;
	}

	/**
	 * Transform object in Properties String
	 * @param objectToSerialize object to serialize
	 * @return String built
	 */
	public static String serializeIntoProperties(Object objectToSerialize) {
		String objectSerialized = null;
		try {
			objectSerialized = new JavaPropsMapper().writeValueAsString(objectToSerialize);
		}
		catch (JsonProcessingException e) {
			LOG.error("Issue when serializing:%s".formatted(e.getMessage()));
		}
		return objectSerialized;
	}

	/**
	 * Transform object using a custom serializer in Properties String
	 * @param objectToSerialize object to serialize
	 * @return String built
	 */
	public static <T> String customPropertiesSerializer(Object objectToSerialize, StdSerializer<T> stdSerializer,
			Class<T> classToSerialize) {
		String objectSerialized = null;
		try {
			SimpleModule module = new SimpleModule();
			module.addSerializer(classToSerialize, stdSerializer);
			JavaPropsMapper javaPropsMapper = new JavaPropsMapper();
			javaPropsMapper.registerModule(module);
			objectSerialized = javaPropsMapper.writeValueAsString(objectToSerialize);
		}
		catch (JsonProcessingException e) {
			LOG.error("Issue when serializing:%s".formatted(e.getMessage()));
		}
		return objectSerialized;
	}

	/**
	 * Build an OverrideConfigEntity based on a json InputStream
	 * @param inputStream InputStream to evaluate
	 * @return OverrideConfigEntity built
	 */
	public static OverrideConfigEntity deserializeJsonOverrideConfigEntity(InputStream inputStream) {
		OverrideConfigEntity overrideConfigEntity;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			overrideConfigEntity = objectMapper.readValue(inputStream, new TypeReference<>() {
			});

			// The default value corresponds to the value of the property
			overrideConfigEntity.getWeasisPropertyEntities().forEach(p -> p.setDefaultValue(p.getValue()));
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when deserializing Json OverrideConfigEntity InputStream:" + e.getMessage());
		}
		return overrideConfigEntity;
	}

	/**
	 * Build an OverrideConfigEntity and fill with the WeasisProperties of this entity
	 * based on inputStream of a property config file
	 * @param inputStream InputStream to evaluate
	 * @return OverrideConfigEntity built filled
	 */
	public static OverrideConfigEntity deserializePropertiesOverrideConfigEntity(InputStream inputStream) {
		OverrideConfigEntity overrideConfigEntity;
		JavaPropsSchema schema = JavaPropsSchema.emptySchema().withoutPathSeparator();
		try {
			Map<String, String> map = new JavaPropsMapper().readerFor(HashMap.class)
				.with(schema)
				.readValue(inputStream);
			overrideConfigEntity = OverrideConfigEntity.builder()
				.weasisPropertyEntities(map.keySet()
					.stream()
					.map(k -> WeasisPropertyEntity.builder().code(k).value(map.get(k)).defaultValue(map.get(k)).build())
					.toList())
				.build();
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when deserializing Properties OverrideConfigEntity InputStream:" + e.getMessage());
		}

		return overrideConfigEntity;
	}

	/**
	 * Read mapping minimal version from InputStream
	 * @param inputStream InputStream
	 * @return mapping minimal version from InputStream
	 */
	public static List<MinimalReleaseVersion> deserializeMinimalReleaseVersionsFromInputStream(
			InputStream inputStream) {
		try (inputStream) {
			ObjectMapper objectMapper = new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

			// Retrieve the minimal release versions
			List<MinimalReleaseVersion> minimalReleaseVersions = objectMapper.readValue(inputStream,
					new TypeReference<>() {
					});

			// Clean versions without qualifier for release and minimal versions
			minimalReleaseVersions.forEach(MinimalReleaseVersion::cleaningQualifierForReleaseAndMinimalVersion);

			return minimalReleaseVersions;
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when trying to retrieve minimal release versions: %s".formatted(e.getMessage()));
		}
	}

}
