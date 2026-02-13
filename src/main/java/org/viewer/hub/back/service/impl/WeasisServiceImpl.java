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

package org.viewer.hub.back.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisIHESearchCriteria;
import org.viewer.hub.back.service.*;
import org.viewer.hub.back.util.DateTimeUtil;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service managing Weasis manifest
 */
@Service
@Slf4j
public class WeasisServiceImpl implements WeasisService {

    // Services
    private final CacheService cacheService;

    private final WeasisConnectorQueryService weasisConnectorQueryService;

    private final SecurityService securityService;

    private final ConnectorService connectorService;

    /**
     * Autowired constructor
     *
     * @param cacheService                Cache service
     * @param weasisConnectorQueryService Connector query service
     * @param securityService              Security service
     * @param connectorService             Connector service
     */
    @Autowired
    public WeasisServiceImpl(final CacheService cacheService,
                             final WeasisConnectorQueryService weasisConnectorQueryService,
                             final SecurityService securityService, final ConnectorService connectorService) {
        this.cacheService = cacheService;
        this.weasisConnectorQueryService = weasisConnectorQueryService;
        this.securityService = securityService;
        this.connectorService = connectorService;
    }

    @Override
    @Async
    // TODO currently security context propagation not working in @ASync methods call
    // Normally should work like this
    // https://www.baeldung.com/spring-security-async-principal-propagation
    // instead of propagating the authentication parameter in the methods calls
    // When working should use SecurityContextHolder.getContext().getAuthentication()
    public void buildManifest(String key, @Valid SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive, Authentication authentication) {
        // Initialize manifest and set it in the cache with the flag build in progress to true
        Manifest manifest = initializeManifest(key, searchCriteria, authentication);

        // Build manifest depending on the presence of patientsByArchive in parameter and the type of search criteria (IHE or non-IHE)
        if (patientsByArchive == null) {
            if (searchCriteria instanceof IHESearchCriteria) {
                // Build manifest with IHE search criteria
                this.buildManifestWithIHESearchCriteria(manifest, (WeasisIHESearchCriteria) searchCriteria, authentication);
            } else {
                // Build manifest with non-IHE search criteria
                this.buildManifestWithoutIHESearchCriteria(manifest, (WeasisArchiveSearchCriteria) searchCriteria, authentication);
            }
        } else {
            // Build manifest with provided patients by archive map.
            // Case when patients have already been fetched from  the connectors based on the search criteria
            // Happens when viewer=WEASIS has not been specified in the search criteria
            this.buildManifestWithPatientsByArchiveMap(manifest, patientsByArchive);
        }

        // Finalize manifest by applying filters, handling authentication, setting build duration,
        // set the flag build in progress to false and updating cache
        this.finalizeBuildingManifest(key, manifest, searchCriteria, authentication);
    }

    @Override
    public Manifest retrieveManifest(String key) {
        return this.cacheService.getManifest(key);
    }

    /**
     * Initialize manifest and set it in the cache with the flag build in progress to true
     *
     * @param key Cache key
     * @param searchCriteria Search criteria
     * @param authentication Authentication: used to know depending on the connector if
     *                       basic or oAuth2 wado parameters should be used
     * @return Manifest initialized and set in the cache
     */
    private Manifest initializeManifest(String key, SearchCriteria searchCriteria, Authentication authentication) {
        Manifest manifest = new Manifest(authentication != null, searchCriteria);
        manifest.setBuildInProgress(true);
        this.cacheService.putManifestIfAbsent(key, manifest);
        return manifest;
    }

    /**
     * Finalize manifest by applying filters, handling authentication, setting build duration,
     * set the flag build in progress to false and updating cache
     *
     * @param key Cache key
     * @param manifest Manifest to finalize
     * @param searchCriteria Search criteria used for filtering
     * @param authentication Authentication
     */
    private void finalizeBuildingManifest(String key, Manifest manifest, SearchCriteria searchCriteria, Authentication authentication) {
        // Apply search criteria filters
        manifest.getArcQueries().forEach(aq ->
                Optional.ofNullable(aq.getPatients())
                        .filter(p -> !p.isEmpty())
                        .ifPresent(p ->
                                aq.setPatients(searchCriteria.applyPatientRequestSearchCriteriaFilters(p))));

        // Handle authentication
        this.securityService.handleManifestAuthentication(manifest, authentication);

        // Update the build duration
        manifest.setBuildDuration(DateTimeUtil.retrieveDurationFromDateTimeInMs(manifest.getStartManifestRequest()));

        // Construction of the manifest is over: set the manifest in the cache
        manifest.setBuildInProgress(false);
        this.cacheService.putManifest(key, manifest);
        LOG.info("Manifest built for key:" + key + " and search criteria:" + searchCriteria);
    }



    /**
     * Build manifest with weasis search criteria
     *
     * @param manifest Manifest to fill
     * @param searchCriteria Search criteria
     * @param authentication Authentication: used to know depending on the connector if
     *                       basic or oAuth2 wado parameters should be used
     */
    private void buildManifestWithoutIHESearchCriteria(Manifest manifest, WeasisArchiveSearchCriteria searchCriteria,
                                                           Authentication authentication) {
        LOG.debug("Building manifest without IHE search criteria");

        // TODO: addGeneralViewerMessage...
        // TODO: decrypt..
        // TODO: doBuildQuery...

        // Sop Instance Uid
        if (!searchCriteria.getObjectUID().isEmpty()) {
            this.weasisConnectorQueryService.buildFromSopInstanceUids(manifest, searchCriteria.getObjectUID(),
                    searchCriteria.getArchive(), authentication);
        }
        // Series Instance Uid
        if (!searchCriteria.getSeriesUID().isEmpty()) {
            this.weasisConnectorQueryService.buildFromSeriesInstanceUids(manifest, searchCriteria.getSeriesUID(),
                    searchCriteria.getArchive(), authentication);
        }
        // Accession Number
        if (!searchCriteria.getAccessionNumber().isEmpty()) {
            this.weasisConnectorQueryService.buildFromStudyAccessionNumbers(manifest,
                    searchCriteria.getAccessionNumber(), searchCriteria.getArchive(), authentication);
        }
        // Study Uid
        if (!searchCriteria.getStudyUID().isEmpty()) {
            this.weasisConnectorQueryService.buildFromStudyInstanceUids(manifest, searchCriteria.getStudyUID(),
                    searchCriteria.getArchive(), authentication);
        }
        // Patient Id
        if (!searchCriteria.getPatientID().isEmpty()) {
            this.weasisConnectorQueryService.buildFromPatientIds(manifest, searchCriteria.getPatientID(),
                    searchCriteria, authentication);
        }
    }

    /**
     * Build manifest with IHE search criteria
     *
     * @param manifest Manifest to fill
     * @param searchCriteria Search criteria
     * @param authentication Authentication: used to know depending on the connector if
     *                       basic or oAuth2 wado parameters should be used
     */
    private void buildManifestWithIHESearchCriteria(Manifest manifest, WeasisIHESearchCriteria searchCriteria,
                                                        Authentication authentication) {
        LOG.debug("Building manifest with IHE search criteria");

        // TODO: addGeneralViewerMessage...
        // TODO: decrypt..
        // TODO: doBuildQuery...

        // Study level
        if (searchCriteria.getRequestType() == IHERequestType.STUDY) {
            if (!searchCriteria.getAccessionNumber().isEmpty()) {
                this.weasisConnectorQueryService.buildFromStudyAccessionNumbers(manifest,
                        searchCriteria.getAccessionNumber(), searchCriteria.getArchive(), authentication);
            } else if (!searchCriteria.getStudyUID().isEmpty()) {
                this.weasisConnectorQueryService.buildFromStudyInstanceUids(manifest, searchCriteria.getStudyUID(),
                        searchCriteria.getArchive(), authentication);
            }
        }
        // Patient level
        else if (searchCriteria.getRequestType() == IHERequestType.PATIENT) {
            this.weasisConnectorQueryService.buildFromPatientIds(manifest, Set.of(searchCriteria.getPatientID()),
                    searchCriteria, authentication);
        }
    }

    /**
     * Build manifest with provided patients by archive map
     *
     * @param manifest Manifest to fill
     * @param patientsByArchive Map of patients grouped by archive
     */
    private void buildManifestWithPatientsByArchiveMap(Manifest manifest, Map<String, Set<Patient>> patientsByArchive) {
        LOG.debug("Building manifest with patients by archive map");
        // Populate manifest directly from the map
        patientsByArchive.forEach((archiveId, patients) -> {
            if (patients != null && !patients.isEmpty()) {
                // Retrieve connector from archive id and update manifest
                manifest.update(patients, this.connectorService.retrieveConnectorFromId(archiveId));
            }
        });
    }

}

