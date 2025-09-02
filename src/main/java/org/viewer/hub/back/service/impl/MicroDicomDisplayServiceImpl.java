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

import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.config.properties.MicroDicomConfigurationProperties;
import org.viewer.hub.back.controller.exception.NoContentException;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.MicroDicomDisplayService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MicroDicomDisplayServiceImpl implements MicroDicomDisplayService {

	private final MicroDicomConfigurationProperties microDicomConfigurationProperties;

	// Services
	private final ConnectorService connectorService;

	private final ConnectorQueryService connectorQueryService;

	@Autowired
	public MicroDicomDisplayServiceImpl(final ConnectorService connectorService,
                                        final ConnectorQueryService connectorQueryService,
                                        final MicroDicomConfigurationProperties microDicomConfigurationProperties) {
		this.connectorService = connectorService;
		this.connectorQueryService = connectorQueryService;
		this.microDicomConfigurationProperties = microDicomConfigurationProperties;
	}

	@Override
	public String retrieveMicroDicomLaunchUrl(SearchCriteria searchCriteria, Authentication authentication) {
		String microDicomLaunchUrl = null;

		// Check search criteria depending on MicroDicom limitations
		checkSearchCriteriaDueToMicroDicomLimitations(searchCriteria, authentication);

		// Retrieve first default or specific connector
		String archive = this.connectorService.retrieveFirstDefaultOrFirstSpecificConnector(searchCriteria);

		if (archive != null) {
			// Protocol + context
			UriComponentsBuilder uriComponentsBuilder = buildProtocolAndContext();

			// Query params to fill: MicroDicom limitation: we have to keep ordered query params
			List<Map.Entry<String, String>> queryParams = new LinkedList<>();

			// Pacs Server query params
			buildPacsServer(archive, queryParams);

			// Pacs Tag Value query params
			buildPacsTagValue(searchCriteria, authentication, archive, queryParams);

			// Build the url: MicroDicom limitation: we have to keep ordered query params
			microDicomLaunchUrl = "%s?%s".formatted(uriComponentsBuilder.toUriString(), queryParams.stream()
                    .map(p -> "%s=%s".formatted(URLEncoder.encode(p.getKey(), StandardCharsets.UTF_8),
							URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8)))
                    .collect(Collectors.joining("&")));
		}

		return microDicomLaunchUrl;
	}

	/**
	 * Determine query params for pacsTagValue
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param authentication Authentication
	 * @param archive Archive to request
	 * @param queryParams Params to fill
	 */
	private void buildPacsTagValue(SearchCriteria searchCriteria, Authentication authentication, String archive, List<Map.Entry<String, String>> queryParams) {
		queryParams.add(Map.entry("param", "pacsTagValue"));

		// Patient ID
		if (!(searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getPatientID()
				: Set.of(((IHESearchCriteria) searchCriteria).getPatientID())).isEmpty() ) {
			buildPacsTagValuePatientId(searchCriteria, queryParams);
		}
		// Study Uid
		else if (!(searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getStudyUID()
				: ((IHESearchCriteria) searchCriteria).getStudyUID()).isEmpty()) {
			buildPacsTagValueStudyUid(searchCriteria, queryParams);
		}
		// Accession Number
		else if (!(searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getAccessionNumber()
				: ((IHESearchCriteria) searchCriteria).getAccessionNumber()).isEmpty()) {
			buildPacsTagValueAccessionNumber(searchCriteria, queryParams);
		}
		// Sop Instance Uid or Series Instance Uid: find the study UID associated
		else {
			buildPacsTagValueSerieUidOrSopInstanceUid(searchCriteria, authentication, archive, queryParams);
		}
	}

	/**
	 * Determine query params for pacsTagValue for the serie/sop instance uid part
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param authentication Authentication
	 * @param archive Archive to request
	 * @param queryParams Params to fill
	 */
	private void buildPacsTagValueSerieUidOrSopInstanceUid(SearchCriteria searchCriteria, Authentication authentication, String archive, List<Map.Entry<String, String>> queryParams) {
		// Retrieve list of patients containing one StudyUid via viewer-hub connectors
		Set<Patient> patients = searchCriteria instanceof ArchiveSearchCriteria ?
				this.connectorQueryService.retrievePatientsWithoutIHESearchCriteria((ArchiveSearchCriteria) searchCriteria, Set.of(archive), authentication)
				: this.connectorQueryService.retrievePatientsWithIHESearchCriteria((IHESearchCriteria) searchCriteria, Set.of(archive), authentication);

		// Study UID
		String studyUIDFound = patients.stream()
				.findFirst()
				.flatMap(p -> p.getStudies().stream().findFirst())
				.map(Study::getStudyInstanceUID)
				.orElse(null);

		if (StringUtils.isNotBlank(studyUIDFound)) {
			queryParams.add(Map.entry("value", String.format("%08X", Tag.StudyInstanceUID)));
			queryParams.add(Map.entry("value", studyUIDFound));
		}
		else {
			throw new NoContentException("Study UID not found for MicroDicom Launch Url, Search criteria: %s".formatted(searchCriteria));
		}
	}

	/**
	 * Determine query params for pacsTagValue for accession number
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param queryParams Params to fill
	 */
	private static void buildPacsTagValueAccessionNumber(SearchCriteria searchCriteria, List<Map.Entry<String, String>> queryParams) {
		queryParams.add(Map.entry("value", String.format("%08X", Tag.AccessionNumber)));
		queryParams.add(Map.entry("value", searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getAccessionNumber().stream().findFirst().orElseThrow(BadRequestException::new)
				: ((IHESearchCriteria) searchCriteria).getAccessionNumber().stream().findFirst().orElseThrow(BadRequestException::new)));
	}

	/**
	 * Determine query params for pacsTagValue for study uid
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param queryParams Params to fill
	 */
	private static void buildPacsTagValueStudyUid(SearchCriteria searchCriteria, List<Map.Entry<String, String>> queryParams) {
		queryParams.add(Map.entry("value", String.format("%08X", Tag.StudyInstanceUID)));
		queryParams.add(Map.entry("value", searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getStudyUID().stream().findFirst().orElseThrow(BadRequestException::new)
				: ((IHESearchCriteria) searchCriteria).getStudyUID().stream().findFirst().orElseThrow(BadRequestException::new)));
	}

	/**
	 * Determine query params for pacsTagValue for patient id
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param queryParams Params to fill
	 */
	private static void buildPacsTagValuePatientId(SearchCriteria searchCriteria, List<Map.Entry<String, String>> queryParams) {
		queryParams.add(Map.entry("value", String.format("%08X", Tag.PatientID)));
		queryParams.add(Map.entry("value", searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).getPatientID().stream().findFirst().orElseThrow(BadRequestException::new)
				: ((IHESearchCriteria) searchCriteria).getPatientID()));
	}

	/**
	 * Determine query params for pacsServer
	 * @param archive Archive to request
	 * @param queryParams Params to fill
	 */
	private void buildPacsServer(String archive, List<Map.Entry<String, String>> queryParams) {
		queryParams.add(Map.entry("param", "pacsServer"));
		queryParams.add(Map.entry("value", "%s:%s%s"
				.formatted(microDicomConfigurationProperties.getArchives().get(archive).getContext(),
						microDicomConfigurationProperties.getArchives().get(archive).getUrl(),
						StringUtils.isNotBlank(microDicomConfigurationProperties.getArchives().get(archive).getPort())
								? ":%s".formatted(microDicomConfigurationProperties.getArchives().get(archive).getPort()) : "")));
	}

	/**
	 * Build protocol url
	 * @return UriComponentsBuilder
	 */
	private UriComponentsBuilder buildProtocolAndContext() {
        return UriComponentsBuilder
                .fromUriString("%s%s".formatted(microDicomConfigurationProperties.getCommand().getProtocol(),
                        microDicomConfigurationProperties.getCommand().getContext()));
	}

	/**
	 * Due to MicroDicom limitations check:
	 * - only one archive
	 * - only one PatientId or one Accession Number or one Study UID
	 * @param searchCriteria SearchCriteria to evaluate
	 * @param authentication Authentication
	 */
	private void checkSearchCriteriaDueToMicroDicomLimitations(SearchCriteria searchCriteria, Authentication authentication) {
		// Check request: MicroDicom supports only one archive
		if (searchCriteria.getArchive() != null && searchCriteria.getArchive().size() > 1){
			throw new ParameterException("Micro Dicom supports only one archive parameter, Search criteria: %s".formatted(searchCriteria));
		}

		// Check request: MicroDicom supports only one PatientId or one Accession Number or one Study UID: check has only one value in search criteria
		if (!(searchCriteria instanceof ArchiveSearchCriteria ?
				((ArchiveSearchCriteria) searchCriteria).hasOnlyOneSearchCriteriaValue()
				: ((IHESearchCriteria) searchCriteria).hasOnlyOneSearchCriteriaValue())){
			throw new ParameterException("MicroDicom supports only one PatientId or one Accession Number or one Study UID parameter, Search criteria: %s".formatted(searchCriteria));
		}
	}

}