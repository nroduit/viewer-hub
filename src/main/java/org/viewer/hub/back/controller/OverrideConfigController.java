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

package org.viewer.hub.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.WeasisPropertyEntity;
import org.viewer.hub.back.entity.serializer.WeasisPropertyEntitySerializer;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.back.service.PackageService;
import org.viewer.hub.back.util.JacksonUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller managing override of Weasis configuration
 */
@RestController
@RequestMapping(EndPoint.OVERRIDE_CONFIG_PATH)
@Tag(name = "Override config", description = "API Endpoints for managing override of Weasis configuration")
@Slf4j
@Validated
@RefreshScope
public class OverrideConfigController {

	@Value("${weasis.config.filename.properties:config.properties}")
	private String weasisConfigFileNameProperties;

	@Value("${weasis.config.filename.json:base.json}")
	private String weasisConfigFileNameJson;

	// Services
	private final OverrideConfigService overrideConfigService;

	private final PackageService packageService;

	/**
	 * Autowired constructor
	 * @param overrideConfigService Service managing override of Weasis configuration
	 * @param packageService Service managing package versions
	 */
	@Autowired
	public OverrideConfigController(final OverrideConfigService overrideConfigService,
			final PackageService packageService) {
		this.overrideConfigService = overrideConfigService;
		this.packageService = packageService;
	}

	@Operation(summary = "Retrieve the configuration properties",
			description = "Retrieve the configuration properties in props or json format ")
	@GetMapping(value = "/properties", produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<byte[]> retrieveConfigurationProperties(@RequestParam @NotNull Long packageVersionId,
			@RequestParam(required = false) Long launchConfigId, @RequestParam(required = false) Long groupId) {
		ResponseEntity<byte[]> toReturn = null;

		// Retrieve the package version entity corresponding to the id in the request
		PackageVersionEntity packageVersionEntity = this.packageService.retrievePackageVersion(packageVersionId);

		if (packageVersionEntity != null) {
			boolean shouldUseJsonSerialization = this.packageService.shouldUseJsonParsing(packageVersionEntity);

			// In order to produce a file: add filename header
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(
					shouldUseJsonSerialization ? this.weasisConfigFileNameJson : this.weasisConfigFileNameProperties));

			// Call service to retrieve the corresponding properties, build properties or
			// json bytes and return the response with headers
			List<WeasisPropertyEntity> weasisPropertyEntities = this.overrideConfigService
				.retrieveProperties(packageVersionId, launchConfigId, groupId)
				.getWeasisPropertyEntities();
			toReturn = new ResponseEntity<>(shouldUseJsonSerialization
					? JacksonUtil
						.serializeIntoJson(
								OverrideConfigEntity.builder().weasisPropertyEntities(weasisPropertyEntities).build())
						.getBytes()
					: weasisPropertyEntities.stream()
						.map(p -> JacksonUtil.customPropertiesSerializer(p,
								new WeasisPropertyEntitySerializer(WeasisPropertyEntity.class),
								WeasisPropertyEntity.class))
						.collect(Collectors.joining())
						// modify String encoding to ISO-8859-1 for Weasis versions using
						// properties file
						.getBytes(StandardCharsets.ISO_8859_1),
					responseHeaders, HttpStatus.OK);
		}
		return toReturn;
	}

}
