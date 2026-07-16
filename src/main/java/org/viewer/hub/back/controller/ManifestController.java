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

package org.viewer.hub.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.viewer.hub.back.constant.ApiVersion;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.searchcriteria.WeasisArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisIHESearchCriteria;
import org.viewer.hub.back.service.WeasisService;
import org.viewer.hub.back.util.DateTimeUtil;
import org.viewer.hub.back.util.JacksonUtil;
import org.viewer.hub.back.util.Retryable;

import java.time.LocalDateTime;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Controller managing the manifest
 */
@RestController
@RequestMapping(EndPoint.MANIFEST_PATH)
@Tag(name = "Manifest", description = "API Endpoints for Manifest")
@Slf4j
@Validated
public class ManifestController {

	// Services
	private final WeasisService weasisService;

	/**
	 * Autowired constructor
	 * @param weasisService manifest service
	 */
	@Autowired
	public ManifestController(final WeasisService weasisService) {
		this.weasisService = weasisService;
	}

	/**
	 * Retrieve manifest corresponding to the key and produce it as xml format
	 * @param key key of the manifest to retrieve
	 * @return xml manifest found
	 */
	@Operation(summary = "Retrieve manifest",
			description = "Retrieve the XML manifest for Weasis and log kv for Kibana regarding request and manifest creation/retrieval")
	@GetMapping(produces = { ApiVersion.V1_APPLICATION_XML_VALUE })
	// @PreAuthorize("hasAuthority('viewerhub_search')")
	// TODO temporary deactivate security: wait for Weasis to make secured calls
	public Manifest retrieveXmlManifest(HttpServletRequest request, @Valid @NotBlank String key) {
		LocalDateTime startTimeRetrieveManifest = LocalDateTime.now();

		// currently quick and dirty: TODO: with spring-retry on condition instead of
		// exception if possible ?
		Manifest manifest = Retryable.of(() -> this.weasisService.retrieveManifest(key))
			// => Retry occurs when build of manifest is currently in progress
			// => Retry should not be launch when the manifest has been evicted from cache
			// after ttl (= manifest is null)
			.successIs((m) -> m == null || !m.isBuildInProgress())
			// Retry during 30 seconds: 300 x 100 ms
			.retries(300)
			.delay(100L)
			.orElse(() -> null)
			.execute();

		// StructuredArguments used for building dashboard
		logManifestRetrieval(request, manifest, key, startTimeRetrieveManifest);

		// Manifest found
		return manifest;
	}

	/**
	 * Log used to build dashboard in kibana in order to follow the time taken by the
	 * client to retrieve the manifest
	 * @param request request
	 * @param manifest manifest containing the info to display
	 */
	private static void logManifestRetrieval(HttpServletRequest request, Manifest manifest, String key,
			LocalDateTime startRetrieveManifest) {
		if (manifest != null && manifest.getStartManifestRequest() != null) {
			// Config
			String config = (manifest.getSearchCriteria() instanceof WeasisIHESearchCriteria weasisIHE)
					? weasisIHE.getConfig()
					: (manifest.getSearchCriteria() instanceof WeasisArchiveSearchCriteria weasisArchive)
							? weasisArchive.getConfig() : null;

			LOG.info("Manifest with key %s has been retrieved".formatted(key),
					kv("weasis.manifest.launch.time",
							DateTimeUtil.retrieveDurationFromDateTimeInMs(manifest.getStartManifestRequest())),
					kv("manifest.build.time", manifest.getBuildDuration()),
					kv("manifest.retrieve.time", DateTimeUtil.retrieveDurationFromDateTimeInMs(startRetrieveManifest)),
					kv("request.host", manifest.getSearchCriteria().getHost()),
					kv("request.user",
							(manifest.getSearchCriteria().getUser() != null)
									? manifest.getSearchCriteria().getUser().toUpperCase() : null),
					kv("request.client", manifest.getSearchCriteria().getClient()),
					kv("request.component", request.getHeader("User-Agent")), kv("request.config", config),
					kv("request.parameters", JacksonUtil.serializeIntoJson(manifest.getSearchCriteria())),
					kv("manifest.size", manifest.toString().length()));
		}
	}

}
