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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.WeasisIHESearchCriteria;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ManifestService;
import org.viewer.hub.back.util.DateTimeUtil;

import java.util.Set;

/**
 * Service managing manifest
 */
@Service
@Slf4j
public class ManifestServiceImpl implements ManifestService {

	// Services
	private final CacheService cacheService;

	private final ConnectorQueryService connectorQueryService;

	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	/**
	 * Autowired constructor
	 * @param cacheService Cache service
	 * @param connectorQueryService Connector query service
	 */
	@Autowired
	public ManifestServiceImpl(final CacheService cacheService, final ConnectorQueryService connectorQueryService,
			final OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
		this.cacheService = cacheService;
		this.connectorQueryService = connectorQueryService;
		this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
	}

	@Override
	@Async
	public void buildManifest(String key, @Valid SearchCriteria searchCriteria, Authentication authentication) {
		// Build the manifest depending on the configured connectors
		Manifest manifest = searchCriteria instanceof WeasisIHESearchCriteria
				? this.buildManifestWithIHESearchCriteria((WeasisIHESearchCriteria) searchCriteria, authentication, key)
				: this.buildManifestWithoutIHESearchCriteria((WeasisSearchCriteria) searchCriteria, authentication,
						key);

		// Update the build duration
		manifest.setBuildDuration(DateTimeUtil.retrieveDurationFromDateTimeInMs(manifest.getStartManifestRequest()));

		// Construction of the manifest is over: set the manifest in the cache
		manifest.setBuildInProgress(false);
		this.cacheService.putManifest(key, manifest);
		LOG.info("Manifest built for key:" + key + " and search criteria:" + searchCriteria);
	}

	@Override
	public Manifest retrieveManifest(String key) {
		return this.cacheService.getManifest(key);
	}

	/**
	 * Build manifest with weasis search criteria
	 * @param searchCriteria Search criteria
	 * @param authentication Authentication: used to know depending on the connector if
	 * basic or oAuth2 wado parameters should be used
	 * @return Manifest built
	 */
	private Manifest buildManifestWithoutIHESearchCriteria(WeasisSearchCriteria searchCriteria,
			Authentication authentication, String key) {
		LOG.debug("Building manifest without IHE search criteria");
		Manifest manifest = new Manifest(authentication != null, searchCriteria);

		// Set flag manifest under construction and set it in the cache
		manifest.setBuildInProgress(true);
		this.cacheService.putManifestIfAbsent(key, manifest);

		// If authenticated: set access token to use it when building manifest
		manifest.setAccessToken(this.retrieveAccessToken(authentication));

		// TODO: addGeneralViewerMessage...
		// TODO: decrypt..
		// TODO: doBuildQuery...

		// Sop Instance Uid
		if (!searchCriteria.getObjectUID().isEmpty()) {
			this.connectorQueryService.buildFromSopInstanceUids(manifest, searchCriteria.getObjectUID(),
					searchCriteria.getArchive());
		}
		// Series Instance Uid
		if (!searchCriteria.getSeriesUID().isEmpty()) {
			this.connectorQueryService.buildFromSeriesInstanceUids(manifest, searchCriteria.getSeriesUID(),
					searchCriteria.getArchive());
		}
		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			this.connectorQueryService.buildFromStudyAccessionNumbers(manifest, searchCriteria.getAccessionNumber(),
					searchCriteria.getArchive());
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			this.connectorQueryService.buildFromStudyInstanceUids(manifest, searchCriteria.getStudyUID(),
					searchCriteria.getArchive());
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			this.connectorQueryService.buildFromPatientIds(manifest, searchCriteria.getPatientID(), searchCriteria);
		}

		// manifest built
		return manifest;
	}

	/**
	 * If authenticated: retrieve the access token to use it when building the manifest
	 * @param authentication Authentication
	 * @return access token found
	 */
	private String retrieveAccessToken(Authentication authentication) {
		String accessTokenFound = null;
		if (authentication != null) {
			OAuth2AuthorizedClient authorizedClient = this.oAuth2AuthorizedClientService
				.loadAuthorizedClient("keycloak", authentication.getName());
			accessTokenFound = authorizedClient != null && authorizedClient.getAccessToken() != null
					? authorizedClient.getAccessToken().getTokenValue() : null;
		}
		return accessTokenFound;
	}

	/**
	 * Build manifest with IHE search criteria
	 * @param searchCriteria Search criteria
	 * @param authentication Authentication: used to know depending on the connector if
	 * basic or oAuth2 wado parameters should be used
	 * @return Manifest built
	 */
	private Manifest buildManifestWithIHESearchCriteria(WeasisIHESearchCriteria searchCriteria,
			Authentication authentication, String key) {
		LOG.debug("Building manifest with IHE search criteria");

		// TODO: addGeneralViewerMessage...
		// TODO: decrypt..
		// TODO: doBuildQuery...

		Manifest manifest = new Manifest(authentication != null, searchCriteria);

		// Set flag manifest under construction and set it in the cache
		manifest.setBuildInProgress(true);
		this.cacheService.putManifestIfAbsent(key, manifest);

		// If authenticated: set access token to use it when building manifest
		manifest.setAccessToken(this.retrieveAccessToken(authentication));

		// Study level
		if (searchCriteria.getRequestType() == IHERequestType.STUDY) {
			if (!searchCriteria.getAccessionNumber().isEmpty()) {
				this.connectorQueryService.buildFromStudyAccessionNumbers(manifest, searchCriteria.getAccessionNumber(),
						searchCriteria.getArchive());
			}
			else if (!searchCriteria.getStudyUID().isEmpty()) {
				this.connectorQueryService.buildFromStudyInstanceUids(manifest, searchCriteria.getStudyUID(),
						searchCriteria.getArchive());
			}
		}
		// Patient level
		else if (searchCriteria.getRequestType() == IHERequestType.PATIENT) {
			this.connectorQueryService.buildFromPatientIds(manifest, Set.of(searchCriteria.getPatientID()),
					searchCriteria);
		}

		// Manifest built
		return manifest;
	}

}
