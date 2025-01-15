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

package org.viewer.hub.back.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.constant.CommandName;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.DisplayService;
import org.viewer.hub.back.service.ManifestService;
import org.viewer.hub.back.util.StringUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@Slf4j
public class DisplayServiceImpl implements DisplayService {

	// Services
	private final CacheService cacheService;

	private final ManifestService manifestService;

	@Value("${viewer-hub.server.url}")
	private String viewerHubServerUrl;

	@Autowired
	public DisplayServiceImpl(final CacheService cacheService, final ManifestService manifestService) {
		this.cacheService = cacheService;
		this.manifestService = manifestService;
	}

	@Override
	public String retrieveWeasisLaunchUrl(@Valid SearchCriteria searchCriteria, Authentication authentication) {
		// Hash parameters to build the key
		String key = this.cacheService.constructManifestKeyDependingOnSearchParameters(searchCriteria);

		// Retrieve Manifest in cache if existing else null
		Manifest manifest = this.cacheService.getManifest(key);
		// Check if a build of the manifest is in progress for this key
		boolean isBuildInProgress = manifest != null && manifest.isBuildInProgress();

		// If no build of manifest in progress for this key
		if (!isBuildInProgress) {
			// Case no manifest built yet: build the manifest asynchronously
			if (manifest == null) {
				this.manifestService.buildManifest(key, searchCriteria, authentication);
			}
			// Case manifest already built and in the cache: reset structured arguments
			// for monitoring
			else {
				manifest.setStartManifestRequest(LocalDateTime.now());
				manifest.setBuildDuration(0);
				this.cacheService.putManifest(key, manifest);
			}
		}

		// Build the launch url
		return this.buildWeasisLaunchUrl(key, searchCriteria);
	}

	/**
	 * Build the launch url
	 * @param key corresponding to the id to retrieve the manifest in the cache
	 * @param searchCriteria Search criteria
	 * @return launch url built
	 */
	private String buildWeasisLaunchUrl(String key, SearchCriteria searchCriteria) {
		// TODO: set other parameters ?..cf pacs connector invokeWeasis

		// Retrieve weasis dicom get command: $dicom:get
		String dicomGetManifestCommand = this.retrieveDicomGetManifestCommand(key);

		// Retrieve weasis argument commands if existing: {{argumentCommand}}
		// {{argumentCommand}}..
		String argumentCommands = this.retrieveArgumentCommands(searchCriteria);

		// Retrieve weasis config command: $weasis:config
		String weasisConfigCommand = this.retrieveWeasisConfigCommand(searchCriteria);

		// Build launch url which will depend on argument command existence
		String launchUrl = argumentCommands == null
				? buildWeasisProtocolCommand(dicomGetManifestCommand, weasisConfigCommand)
				: buildWeasisProtocolCommand(argumentCommands, weasisConfigCommand);

		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
	}

	/**
	 * Build encoded weasis protocol url with dicomGet or arguments command + weasisConfig
	 * command
	 * @param dicomGetOrArgumentCommand dicomGet or arguments command
	 * @param weasisConfigCommand Weasis Config command
	 * @return weasis protocol encoded url built
	 */
	private static String buildWeasisProtocolCommand(String dicomGetOrArgumentCommand, String weasisConfigCommand) {
		return CommandName.LAUNCH_URL_WEASIS_COMMANDS_CONFIG.formatted(URLEncoder
			.encode("%s %s".formatted(dicomGetOrArgumentCommand, weasisConfigCommand), StandardCharsets.UTF_8));
	}

	/**
	 * Retrieve arguments commands if existing: {{arg}} {{arg}}... Used for example for
	 * launching dicomizer: weasis://$acquire:patient + ext-cfg=dicomizer
	 * @param searchCriteria Search criteria
	 * @return weasis argument commands
	 */
	private String retrieveArgumentCommands(SearchCriteria searchCriteria) {
		return searchCriteria.getArg() != null && !searchCriteria.getArg().isEmpty()
				? String.join(StringUtil.SPACE, searchCriteria.getArg()) : null;
	}

	/**
	 * Retrieve weasis dicom get command: weasis://$dicom:get
	 * @param key Key used to build weasis dicom get command
	 * @return dicom get manifest command
	 */
	private String retrieveDicomGetManifestCommand(String key) {
		// Url to retrieve the manifest corresponding to the key
		UriComponentsBuilder uriBuilderRetrieveManifest = UriComponentsBuilder
			.fromHttpUrl("%s%s".formatted(this.viewerHubServerUrl, EndPoint.MANIFEST_PATH))
			// Manifest key
			.queryParam(ParamName.KEY, key);

		// Weasis dicom get command
		return "%s \"%s\"".formatted(CommandName.WEASIS_DICOM_GET_COMMAND, uriBuilderRetrieveManifest.toUriString());
	}

	/**
	 * Retrieve weasis config command: $weasis:config
	 * @param searchCriteria Search criteria
	 * @return weasis config command
	 */
	private String retrieveWeasisConfigCommand(SearchCriteria searchCriteria) {
		UriComponentsBuilder uriBuilderLaunchConfig = UriComponentsBuilder
			.fromHttpUrl("%s%s".formatted(this.viewerHubServerUrl, EndPoint.LAUNCH_CONFIG_PATH));
		// Preference Url
		// TODO: not necessary ? already handle in launch config endpoint ?
		// .queryParam("pro", "weasis.pref.url+" + viewerHubServerUrl +
		// EndPoint.PREFERENCES_PATH);

		// Add additional params if existing in initial request
		// Properties
		if (!searchCriteria.getPro().isEmpty()) {
			uriBuilderLaunchConfig.queryParam(ParamName.PRO, searchCriteria.getPro());
		}
		// User
		if (searchCriteria.getUser() != null && !searchCriteria.getUser().isBlank()) {
			uriBuilderLaunchConfig.queryParam(ParamName.USER, searchCriteria.getUser());
		}
		// Host
		if (searchCriteria.getHost() != null && !searchCriteria.getHost().isBlank()) {
			uriBuilderLaunchConfig.queryParam(ParamName.HOST, searchCriteria.getHost());
		}
		// Ext-cfg
		if (searchCriteria.getExtCfg() != null && !searchCriteria.getExtCfg().isBlank()) {
			uriBuilderLaunchConfig.queryParam(ParamName.EXT_CFG, searchCriteria.getExtCfg());
		}
		// Config
		if (searchCriteria.getConfig() != null && !searchCriteria.getConfig().isBlank()) {
			uriBuilderLaunchConfig.queryParam(ParamName.CONFIG, searchCriteria.getConfig());
		}

		// Weasis config command
		return "%s\"%s\"".formatted(CommandName.WEASIS_CONFIG_COMMAND, uriBuilderLaunchConfig.toUriString());
	}

}
