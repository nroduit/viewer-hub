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
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.constant.SlicerCommandName;
import org.viewer.hub.back.constant.WeasisCommandName;
import org.viewer.hub.back.model.searchcriteria.*;
import org.viewer.hub.back.service.*;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SlicerDisplayServiceImpl implements SlicerDisplayService {

	// Services
	private final ConnectorService connectorService;

	@Value("${viewer-hub.server.url}")
	private String viewerHubServerUrl;


	@Autowired
	public SlicerDisplayServiceImpl(final ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public String retrieveSlicerQidoLaunchUrl(@Valid ArchiveSearchCriteria searchCriteria, String archive) {
		String dicomCommand = this.retrieveDicomQidoCommand(searchCriteria, archive);

		// Retrieve weasis config command: $weasis:config
		String weasisConfigCommand = this.retrieveSlicerConfigCommand();

		String launchUrl = dicomCommand + weasisConfigCommand;

//		--launcher-additional-settings
		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
	}

	private String retrieveDicomQidoCommand(ArchiveSearchCriteria searchCriteria, String archive) {
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
		// Dicomweb url
		String dicomRsUrl = connectorService.getDicomRsUrlFromId(archive);
		if (dicomRsUrl != null) {
			query.add("dicomweb_endpoint=" + dicomRsUrl);
		}

		return "%s?%s".formatted(SlicerCommandName.LAUNCH_URL_SLICER_COMMAND, String.join("&", query));
	}

	/**
	 * Retrieve 3D Slicer config command
	 * @return 3D Slicer config command
	 */
	private String retrieveSlicerConfigCommand() {
		UriComponentsBuilder uriBuilderLaunchConfig = UriComponentsBuilder
				.fromHttpUrl("%s%s".formatted(this.viewerHubServerUrl, EndPoint.WEASIS_LAUNCH_CONFIG_PATH));
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
