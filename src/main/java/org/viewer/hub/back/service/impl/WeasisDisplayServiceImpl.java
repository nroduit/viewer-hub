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
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.constant.WeasisCommandName;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.searchcriteria.*;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.WeasisDisplayService;
import org.viewer.hub.back.service.WeasisService;
import org.viewer.hub.back.util.ConnectorUtil;
import org.viewer.hub.back.util.StringUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WeasisDisplayServiceImpl implements WeasisDisplayService {

	// Services
	private final CacheService cacheService;

	private final WeasisService weasisService;

	private final ConnectorService connectorService;

	@Value("${viewer-hub.server.url}")
	private String viewerHubServerUrl;

	@Autowired
	public WeasisDisplayServiceImpl(final CacheService cacheService,
									final WeasisService weasisService,
									final ConnectorService connectorService) {
		this.cacheService = cacheService;
		this.weasisService = weasisService;
		this.connectorService = connectorService;
	}

	@Override
	public String retrieveWeasisManifestLaunchUrl(@Valid SearchCriteria searchCriteria, Authentication authentication) {
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
				this.weasisService.buildManifest(key, searchCriteria, authentication);
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
		return this.buildWeasisGetManifestUrl(key, searchCriteria);
	}

	/**
	 * Build the launch url
	 * @param key corresponding to the id to retrieve the manifest in the cache
	 * @param searchCriteria Search criteria
	 * @return launch url built
	 */
	private String buildWeasisGetManifestUrl(String key, SearchCriteria searchCriteria) {
		// TODO: set other parameters ?..cf pacs connector invokeWeasis

		// Retrieve weasis dicom get command: $dicom:get
		String dicomGetManifestCommand = this.retrieveDicomGetManifestCommand(key);

		// Retrieve weasis argument commands if existing: {{argumentCommand}}
		// {{argumentCommand}}..
		String argumentCommands = this.retrieveArgumentCommands(searchCriteria);

		// Retrieve weasis config command: $weasis:config
		String weasisConfigCommand = this.retrieveWeasisConfigCommand(searchCriteria);

		// Build launch url which will depend on argument command existence
		String launchUrl = buildWeasisProtocolCommand(dicomGetManifestCommand, argumentCommands, weasisConfigCommand);

		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
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

		return "%s \"%s\"".formatted(WeasisCommandName.WEASIS_DICOM_GET_MANIFEST_COMMAND, uriBuilderRetrieveManifest.toUriString());
	}

	/**
	 * Retrieve arguments commands if existing: {{arg}} {{arg}}... Used for example for
	 * launching dicomizer: weasis://$acquire:patient + ext-cfg=dicomizer
	 * @param searchCriteria Search criteria
	 * @return weasis argument commands
	 */
	private String retrieveArgumentCommands(SearchCriteria searchCriteria) {
		List<String> args = searchCriteria instanceof IHESearchCriteria
				? ((WeasisIHESearchCriteria) searchCriteria).getArg()
				: ((WeasisArchiveSearchCriteria) searchCriteria).getArg();
		return args != null ? String.join(StringUtil.SPACE, args) : "";
	}

	/**
	 * Retrieve arguments commands if existing: {{arg}} {{arg}}... Used for example for
	 * launching dicomizer: weasis://$acquire:patient + ext-cfg=dicomizer
	 * @param searchCriteria Search criteria
	 * @return weasis argument commands
	 */
	private String retrieveQidoArgumentCommands(SearchCriteria searchCriteria) {
		String commonArguments = retrieveArgumentCommands(searchCriteria);
		List<String> args = new ArrayList<>();
		String dicomRsUrl = connectorService.getDicomRsUrl(searchCriteria);
		if (dicomRsUrl != null) {
			args.add("--url \"" + dicomRsUrl + "\"");
		}
		String[] credentials = connectorService.getCredentials(searchCriteria);
		if (credentials != null) {
			args.add("-H \"Authorization: Basic " + ConnectorUtil.toBase64(credentials) + "\"");
		}
		return commonArguments + " " + String.join(StringUtil.SPACE, args);
	}

	/**
	 * Build encoded weasis protocol url with dicomGet or arguments command + weasisConfig
	 * command
	 * @param dicomCommand dicom command
	 * @param argumentCommands arguments command
	 * @param weasisConfigCommand Weasis Config command
	 * @return weasis protocol encoded url built
	 */
	private static String buildWeasisProtocolCommand(String dicomCommand, String argumentCommands, String weasisConfigCommand) {
		return WeasisCommandName.LAUNCH_URL_WEASIS_COMMANDS_CONFIG.formatted(URLEncoder
				.encode("%s %s %s".formatted(dicomCommand, argumentCommands, weasisConfigCommand).trim().replaceAll(" +", " "), StandardCharsets.UTF_8));
	}

	@Override
	public String retrieveWeasisQidoLaunchUrl(@Valid ArchiveSearchCriteria searchCriteria) {
		String dicomCommand = this.retrieveDicomQidoCommand(searchCriteria);

		// Retrieve weasis argument commands if existing: {{argumentCommand}}
		// {{argumentCommand}}..
		String argumentCommands = this.retrieveQidoArgumentCommands(searchCriteria);

		// Retrieve weasis config command: $weasis:config
		String weasisConfigCommand = this.retrieveWeasisConfigCommand(searchCriteria);

		// Build launch url which will depend on argument command existence
		String launchUrl = buildWeasisProtocolCommand(dicomCommand, argumentCommands, weasisConfigCommand);

		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
	}

	private String retrieveDicomQidoCommand(ArchiveSearchCriteria searchCriteria) {
		// Url to retrieve the manifest corresponding to the key
		List<String> query = new ArrayList<>();
		if (!searchCriteria.getObjectUID().isEmpty()) {
			query.add("objectUID=" + String.join(",", searchCriteria.getObjectUID()));
		}
		// Series Instance Uid
		if (!searchCriteria.getSeriesUID().isEmpty()) {
			query.add("seriesUID=" + String.join(",", searchCriteria.getSeriesUID()));
		}
		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			query.add("accessionNumber=" + String.join(",", searchCriteria.getAccessionNumber()));
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			query.add("studyUID=" + String.join(",", searchCriteria.getStudyUID()));
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			query.add("patientID=" + String.join(",", searchCriteria.getPatientID()));
		}

		return "%s -r \"%s\"".formatted(WeasisCommandName.WEASIS_DICOM_RS_COMMAND, String.join("&", query));
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

		// Pro
		List<String> props = searchCriteria instanceof IHESearchCriteria
				? ((WeasisIHESearchCriteria) searchCriteria).getPro()
				: ((WeasisArchiveSearchCriteria) searchCriteria).getPro();

		// Ext-cfg
		String extCfg = searchCriteria instanceof IHESearchCriteria
				? ((WeasisIHESearchCriteria) searchCriteria).getExtCfg()
				: ((WeasisArchiveSearchCriteria) searchCriteria).getExtCfg();

		// Config
		String config = searchCriteria instanceof IHESearchCriteria
				? ((WeasisIHESearchCriteria) searchCriteria).getConfig()
				: ((WeasisArchiveSearchCriteria) searchCriteria).getConfig();

		// Add additional params if existing in initial request
		// Properties
		if (!props.isEmpty()) {
			uriBuilderLaunchConfig.queryParam(ParamName.PRO, props);
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
		if (extCfg != null && !extCfg.isBlank()) {
			uriBuilderLaunchConfig.queryParam(ParamName.EXT_CFG, extCfg);
		}
		// Config
		if (config != null && !config.isBlank()) {
			uriBuilderLaunchConfig.queryParam(ParamName.CONFIG, config);
		}

		// Weasis config command
		return "%s\"%s\"".formatted(WeasisCommandName.WEASIS_CONFIG_COMMAND, uriBuilderLaunchConfig.toUriString());
	}

}
