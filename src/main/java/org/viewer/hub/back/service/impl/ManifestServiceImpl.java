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
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.ArchiveSearchCriteria;
import org.viewer.hub.back.model.IHESearchCriteria;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ManifestService;
import org.viewer.hub.back.service.SecurityService;
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

	private final SecurityService securityService;

	/**
	 * Autowired constructor
	 * @param cacheService Cache service
	 * @param connectorQueryService Connector query service
	 */
	@Autowired
	public ManifestServiceImpl(final CacheService cacheService, final ConnectorQueryService connectorQueryService,
			final SecurityService securityService) {
		this.cacheService = cacheService;
		this.connectorQueryService = connectorQueryService;
		this.securityService = securityService;
	}

	@Override
	@Async
	// TODO currently security context propagation not working in @ASync methods call
	// Normally should work like this
	// https://www.baeldung.com/spring-security-async-principal-propagation
	// instead of propagating the authentication parameter in the methods calls
	// When working should use SecurityContextHolder.getContext().getAuthentication()
	public void buildManifest(String key, @Valid SearchCriteria searchCriteria, Authentication authentication) {
		// Build the manifest depending on the configured connectors
		Manifest manifest = searchCriteria instanceof IHESearchCriteria
				? this.buildManifestWithIHESearchCriteria((IHESearchCriteria) searchCriteria, authentication, key)
				: this.buildManifestWithoutIHESearchCriteria((ArchiveSearchCriteria) searchCriteria, authentication,
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
	private Manifest buildManifestWithoutIHESearchCriteria(ArchiveSearchCriteria searchCriteria,
														   Authentication authentication, String key) {
		LOG.debug("Building manifest without IHE search criteria");
		Manifest manifest = new Manifest(authentication != null, searchCriteria);

		// Set flag manifest under construction and set it in the cache
		manifest.setBuildInProgress(true);
		this.cacheService.putManifestIfAbsent(key, manifest);

		// TODO: addGeneralViewerMessage...
		// TODO: decrypt..
		// TODO: doBuildQuery...

		// Sop Instance Uid
		if (!searchCriteria.getObjectUID().isEmpty()) {
			this.connectorQueryService.buildFromSopInstanceUids(manifest, searchCriteria.getObjectUID(),
					searchCriteria.getArchive(), authentication);
		}
		// Series Instance Uid
		if (!searchCriteria.getSeriesUID().isEmpty()) {
			this.connectorQueryService.buildFromSeriesInstanceUids(manifest, searchCriteria.getSeriesUID(),
					searchCriteria.getArchive(), authentication);
		}
		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			this.connectorQueryService.buildFromStudyAccessionNumbers(manifest, searchCriteria.getAccessionNumber(),
					searchCriteria.getArchive(), authentication);
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			this.connectorQueryService.buildFromStudyInstanceUids(manifest, searchCriteria.getStudyUID(),
					searchCriteria.getArchive(), authentication);
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			this.connectorQueryService.buildFromPatientIds(manifest, searchCriteria.getPatientID(), searchCriteria,
					authentication);
		}

		// Handle authentication
		this.securityService.handleManifestAuthentication(manifest, authentication);

		// manifest built
		return manifest;
	}

	/**
	 * Build manifest with IHE search criteria
	 * @param searchCriteria Search criteria
	 * @param authentication Authentication: used to know depending on the connector if
	 * basic or oAuth2 wado parameters should be used
	 * @return Manifest built
	 */
	private Manifest buildManifestWithIHESearchCriteria(IHESearchCriteria searchCriteria,
                                                        Authentication authentication, String key) {
		LOG.debug("Building manifest with IHE search criteria");

		// TODO: addGeneralViewerMessage...
		// TODO: decrypt..
		// TODO: doBuildQuery...

		Manifest manifest = new Manifest(authentication != null, searchCriteria);

		// Set flag manifest under construction and set it in the cache
		manifest.setBuildInProgress(true);
		this.cacheService.putManifestIfAbsent(key, manifest);

		// Study level
		if (searchCriteria.getRequestType() == IHERequestType.STUDY) {
			if (!searchCriteria.getAccessionNumber().isEmpty()) {
				this.connectorQueryService.buildFromStudyAccessionNumbers(manifest, searchCriteria.getAccessionNumber(),
						searchCriteria.getArchive(), authentication);
			}
			else if (!searchCriteria.getStudyUID().isEmpty()) {
				this.connectorQueryService.buildFromStudyInstanceUids(manifest, searchCriteria.getStudyUID(),
						searchCriteria.getArchive(), authentication);
			}
		}
		// Patient level
		else if (searchCriteria.getRequestType() == IHERequestType.PATIENT) {
			this.connectorQueryService.buildFromPatientIds(manifest, Set.of(searchCriteria.getPatientID()),
					searchCriteria, authentication);
		}

		// Handle authentication
		this.securityService.handleManifestAuthentication(manifest, authentication);

		// Manifest built
		return manifest;
	}

}
