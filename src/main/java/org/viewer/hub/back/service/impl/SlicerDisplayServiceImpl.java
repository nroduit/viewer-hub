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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.config.properties.SlicerConfigurationProperties;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.controller.exception.NoContentException;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.SecurityService;
import org.viewer.hub.back.service.SlicerDisplayService;
import org.viewer.hub.back.util.PathUrlUtil;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class SlicerDisplayServiceImpl implements SlicerDisplayService {

	private final SlicerConfigurationProperties slicerConfigurationProperties;

	// Services
	private final ConnectorService connectorService;

	private final ConnectorQueryService connectorQueryService;

	private final SecurityService securityService;


	@Autowired
	public SlicerDisplayServiceImpl(final ConnectorService connectorService,
									final ConnectorQueryService connectorQueryService,
									final SlicerConfigurationProperties slicerConfigurationProperties, final SecurityService securityService) {
		this.connectorService = connectorService;
		this.connectorQueryService = connectorQueryService;
		this.slicerConfigurationProperties = slicerConfigurationProperties;
		this.securityService = securityService;
	}

	@Override
	public String retrieveSlicerLaunchUrl(SearchCriteria searchCriteria, Authentication authentication) {
		String slicerLaunchUrl = null;

		// Check search criteria depending on 3D Slicer limitations
		checkSearchCriteriaDueToSlicerLimitations(searchCriteria, authentication);

		// Retrieve first default or specific connector
		String archive = this.connectorService.retrieveFirstDefaultOrFirstSpecificConnector(searchCriteria);

		if (archive != null) {
			// Retrieve list of patients containing one StudyUid via viewer-hub connectors
			Set<Patient> patients = searchCriteria instanceof ArchiveSearchCriteria ?
					this.connectorQueryService.retrievePatientsWithoutIHESearchCriteria((ArchiveSearchCriteria) searchCriteria, Set.of(archive), authentication)
					: this.connectorQueryService.retrievePatientsWithIHESearchCriteria((IHESearchCriteria) searchCriteria, Set.of(archive), authentication);

			// Protocol + context
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
					.fromUriString("%s%s".formatted(slicerConfigurationProperties.getCommand().getProtocol(), slicerConfigurationProperties.getCommand().getContext()));;

			// Study UID
			String studyUIDFound = patients.stream()
					.findFirst()
					.flatMap(p -> p.getStudies().stream().findFirst())
					.map(Study::getStudyInstanceUID)
					.orElse(null);

			if (StringUtils.isNotBlank(studyUIDFound)) {
				uriComponentsBuilder = uriComponentsBuilder.queryParam(ParamName.SLICER_STUDY_INSTANCE_UID, studyUIDFound);
			}
			else {
				throw new NoContentException("Study UID not found for Slicer Launch Url, Search criteria: %s".formatted(searchCriteria));
			}

			// Dicom endpoint of the archive to query (or viewer gateway for authorization header)
			uriComponentsBuilder = uriComponentsBuilder.queryParam(ParamName.SLICER_DICOM_WEB_ENDPOINT,
					PathUrlUtil.buildUrlFromServerProperty(this.slicerConfigurationProperties.getArchives().get(archive)));

			// If requested is authenticated, add the token in the url in order for Slicer to request the gateway
			if(authentication != null && authentication.isAuthenticated() && authentication instanceof OAuth2AuthenticationToken){
				uriComponentsBuilder = fillSlicerOAuth2Token(uriComponentsBuilder, (OAuth2AuthenticationToken) authentication);
			}

			// Build the url
			slicerLaunchUrl = uriComponentsBuilder.build().toString();
		}

		return slicerLaunchUrl;
	}

	/**
	 * Due to 3D Slicer limitations check:
	 * - only one archive
	 * - only one study
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param authentication Authentication
	 */
	private void checkSearchCriteriaDueToSlicerLimitations(SearchCriteria searchCriteria, Authentication authentication) {
		// Check request: Slicer supports only one archive
		if (searchCriteria.getArchive() != null && searchCriteria.getArchive().size() > 1){
			throw new ParameterException("3D Slicer supports only one archive parameter, Search criteria: %s".formatted(searchCriteria));
		}

		// Check request: Slicer supports only one study UID: check has only one value in search criteria
		if (!(searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).hasOnlyOneSearchCriteriaValue()
				: ((IHESearchCriteria) searchCriteria).hasOnlyOneSearchCriteriaValue())){
			throw new ParameterException("3D Slicer supports only one study UID parameter, Search criteria: %s".formatted(searchCriteria));
		}

		// Case patient ID is filled: check if it has only one study
		if (searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getPatientID().size() == 1
				: !((IHESearchCriteria) searchCriteria).getPatientID().isEmpty()) {
			Set<Patient> patients = new HashSet<>();

			this.connectorQueryService.buildFromPatientIds(patients, searchCriteria instanceof ArchiveSearchCriteria ?
					((ArchiveSearchCriteria) searchCriteria).getPatientID()
					: Set.of(((IHESearchCriteria) searchCriteria).getPatientID()), searchCriteria.getArchive(), authentication);

			if (patients.isEmpty()){
				throw new ParameterException("No studies found, Search criteria: %s".formatted(searchCriteria));
			}
			else {
				// Should have only one patient
				patients.stream().findFirst().ifPresent(patient -> {
					if (patient.getStudies().isEmpty()){
						throw new ParameterException("No studies found, Search criteria: %s".formatted(searchCriteria));
					}
					else if (patient.getStudies().size() != 1){
						throw new ParameterException("3D Slicer only supports to display one study, Search criteria: %s".formatted(searchCriteria));
					}
				});
			}
		}
	}

	/**
	 * Fill the query param token in order for 3D Slicer to connect to the secured gateway/archive
	 * @param authentication Authentication to evaluate
	 * @return UriComponentsBuilder
	 */
	private UriComponentsBuilder fillSlicerOAuth2Token(UriComponentsBuilder uriComponentsBuilder, OAuth2AuthenticationToken authentication) {
		return uriComponentsBuilder.queryParam("access_token",  securityService.retrieveAccessToken(authentication));
	}

}