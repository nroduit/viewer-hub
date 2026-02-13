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
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.SecurityService;
import org.viewer.hub.back.service.SlicerDisplayService;
import org.viewer.hub.back.util.PathUrlUtil;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class SlicerDisplayServiceImpl implements SlicerDisplayService {

	private final SlicerConfigurationProperties slicerConfigurationProperties;

	// Services
	private final ConnectorService connectorService;
	private final SecurityService securityService;

	@Autowired
	public SlicerDisplayServiceImpl(final ConnectorService connectorService,
									final SlicerConfigurationProperties slicerConfigurationProperties, final SecurityService securityService) {
		this.connectorService = connectorService;
		this.slicerConfigurationProperties = slicerConfigurationProperties;
		this.securityService = securityService;
	}

	@Override
	public String retrieveSlicerLaunchUrl(SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive, Authentication authentication) {
		String slicerLaunchUrl;

		// Retrieve first default or specific connector
		String archive = this.connectorService.retrieveFirstDefaultOrFirstSpecificConnector(searchCriteria);

		// Check search criteria depending on 3D Slicer limitations
		checkSearchCriteriaDueToSlicerLimitations(searchCriteria, patientsByArchive, archive);

		if (archive != null && patientsByArchive != null && patientsByArchive.containsKey(archive) && !patientsByArchive.get(archive).isEmpty()) {
			// Retrieve list of patients containing one StudyUid via viewer-hub connectors
			Set<Patient> patients = patientsByArchive.get(archive);

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
		else {
			throw new ParameterException("No patient found for determined first archive: %s and search criteria: %s".formatted(archive, searchCriteria));
		}

		return slicerLaunchUrl;
	}

	/**
	 * Due to 3D Slicer limitations check:
	 * - only one archive
	 * - only one study
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param patientsByArchive Map of patients grouped by archive (archiveId, Set of patients found from this archive)
	 */
	private void checkSearchCriteriaDueToSlicerLimitations(SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive, String firstDefaultOrFirstSpecificArchive) {
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
			Set<Patient> patients = patientsByArchive.get(firstDefaultOrFirstSpecificArchive);

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
		return uriComponentsBuilder.queryParam(ParamName.SLICER_TOKEN,  securityService.retrieveAccessToken(authentication));
	}

}