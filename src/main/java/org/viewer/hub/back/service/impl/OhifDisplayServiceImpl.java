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


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.config.properties.OhifConfigurationProperties;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.OhifDisplayService;
import org.viewer.hub.back.util.StringUtil;

import java.util.Set;

@Service
@Slf4j
public class OhifDisplayServiceImpl implements OhifDisplayService {

    // Services
    private final ConnectorService connectorService;

    private final ConnectorQueryService connectorQueryService;

    private final OhifConfigurationProperties ohifConfigurationProperties;

    @Autowired
    public OhifDisplayServiceImpl(final ConnectorService connectorService, final OhifConfigurationProperties ohifConfigurationProperties, final ConnectorQueryService connectorQueryService) {
        this.connectorService = connectorService;
        this.ohifConfigurationProperties = ohifConfigurationProperties;
        this.connectorQueryService = connectorQueryService;
    }

    @Override
    public String retrieveOhifLaunchUrl(SearchCriteria searchCriteria, Authentication authentication) {
        String ohifLaunchUrl = null;

        // Check request: Ohif supports only one archive
        if (searchCriteria.getArchive() != null && searchCriteria.getArchive().size() > 1){
            throw new ParameterException("Ohif supports only one archive parameter");
        }

        // Retrieve first default or specific connector
        String archive = this.connectorService.retrieveFirstDefaultOrFirstSpecificConnector(searchCriteria);

        if (archive != null) {
            // Base + context
            UriComponentsBuilder uriComponentsBuilder = buildOhifBaseUrlAndContext();

            // Archive
            uriComponentsBuilder.path("/%s".formatted(archive));

            // Retrieve list of patients containing StudyUids + SeriesUids via viewer-hub connectors
            Set<Patient> patients = searchCriteria instanceof ArchiveSearchCriteria ?
                    this.connectorQueryService.retrievePatientsWithoutIHESearchCriteria((ArchiveSearchCriteria) searchCriteria, Set.of(archive), authentication)
                    : this.connectorQueryService.retrievePatientsWithIHESearchCriteria((IHESearchCriteria) searchCriteria, Set.of(archive), authentication);

            // Uids to request
            uriComponentsBuilder = fillUriWithUidsFromPatientsFound(uriComponentsBuilder, patients, searchCriteria);

            // Ohif initial search criteria
            uriComponentsBuilder = fillOhifInitialCriteria(searchCriteria, uriComponentsBuilder);

            // Build the url
            ohifLaunchUrl = uriComponentsBuilder.build().toString();
        }

        return ohifLaunchUrl;
    }

    /**
     * Build base url corresponding to the ohif viewer or viewer-hub gateway depending on the Ohif configuration (secure by viewer-hub gateway or not)
     * @return UriComponentsBuilder
     */
    private UriComponentsBuilder buildOhifBaseUrlAndContext() {
        // Ohif server or Gateway uri + context
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromUriString(ohifConfigurationProperties.getServer().getUrl());
        if (StringUtils.isNotBlank(ohifConfigurationProperties.getServer().getPort())) {
            uriComponentsBuilder.port(ohifConfigurationProperties.getServer().getPort());
        }
        if (StringUtils.isNotBlank(ohifConfigurationProperties.getServer().getContext())) {
            uriComponentsBuilder.path(ohifConfigurationProperties.getServer().getContext());
        }
        uriComponentsBuilder.path("/viewer");
        return uriComponentsBuilder;
    }

    /**
     * Fill the uri with query params containing the study/serie uids found from the connectors
     *
     * @param uriComponentsBuilder uri builder
     * @param patients             Patient found
     * @param searchCriteria SearchCriteria
     * @return UriComponentsBuilder
     */
    private UriComponentsBuilder fillUriWithUidsFromPatientsFound(UriComponentsBuilder uriComponentsBuilder, Set<Patient> patients, SearchCriteria searchCriteria) {
        if (!patients.isEmpty() && patients.stream().anyMatch(p -> !p.getStudies().isEmpty())){
            // Fill StudyInstanceUIDs url
            uriComponentsBuilder = uriComponentsBuilder.queryParam("StudyInstanceUIDs", String.join(StringUtil.COMMA, patients.stream()
                    .flatMap(patient -> patient.getStudies().stream())
                    .map(Study::getStudyInstanceUID)
                    .toList()));

            // Only for ArchiveSearchCriteria because IHE is at minimum at study level and only if we are looking for series uids or sop instance uids
            if (searchCriteria instanceof ArchiveSearchCriteria
                    && (((ArchiveSearchCriteria) searchCriteria).getSeriesUID() != null && !((ArchiveSearchCriteria) searchCriteria).getSeriesUID().isEmpty()
                            || ((ArchiveSearchCriteria) searchCriteria).getObjectUID() != null && !((ArchiveSearchCriteria) searchCriteria).getObjectUID().isEmpty())) {
                // Fill SeriesInstanceUIDs url
                uriComponentsBuilder = uriComponentsBuilder.queryParam("SeriesInstanceUIDs", String.join(StringUtil.COMMA,  patients.stream()
                        .flatMap(patient -> patient.getStudies().stream())
                        .flatMap(study -> study.getSeries().stream())
                        .map(Serie::getSeriesInstanceUID)
                        .toList()));
            }
        }

        return uriComponentsBuilder;
    }

    /**
     * Ohif initial search criteria:  not for IHE request as no such search criteria
     * @param searchCriteria Search Criteria
     * @param uriComponentsBuilder uri builder
     * @return UriComponentsBuilder
     */
    private UriComponentsBuilder fillOhifInitialCriteria(SearchCriteria searchCriteria, UriComponentsBuilder uriComponentsBuilder) {
        if (searchCriteria instanceof ArchiveSearchCriteria) {
            // If available set Ohif initialSeriesInstanceUID: only if search criteria has only one SeriesInstanceUID requested
            Set<String> seriesUIDs = ((ArchiveSearchCriteria) searchCriteria).getSeriesUID();
            // Priority is given to the serie uid (vs the sop instance uid)
            if (seriesUIDs.size() == 1){
                uriComponentsBuilder = uriComponentsBuilder.queryParam("initialSeriesInstanceUID", seriesUIDs.stream().findFirst().get());
            }
            else {
                // If available set Ohif initialSopInstanceUID: only if search criteria has only one SopInstanceUID requested
                Set<String> sopInstanceUIDs = ((ArchiveSearchCriteria) searchCriteria).getObjectUID();
                if (sopInstanceUIDs.size() == 1){
                    uriComponentsBuilder = uriComponentsBuilder.queryParam("initialSopInstanceUID", sopInstanceUIDs.stream().findFirst().get());
                }
            }

        }
        return uriComponentsBuilder;
    }

}
