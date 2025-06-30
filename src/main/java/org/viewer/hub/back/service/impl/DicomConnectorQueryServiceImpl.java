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

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.Message;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.enums.ConnectorAuthType;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.enums.HeaderType;
import org.viewer.hub.back.enums.SearchCriteriaType;
import org.viewer.hub.back.enums.WeasisManifestDicomWebLevelLimitType;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.DicomPatientSex;
import org.viewer.hub.back.model.manifest.Instance;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.manifest.Patient;
import org.viewer.hub.back.model.manifest.Serie;
import org.viewer.hub.back.model.manifest.Study;
import org.viewer.hub.back.model.property.ConnectorAuthenticationProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.DicomConnectorQueryService;
import org.viewer.hub.back.service.DicomWebClientService;
import org.viewer.hub.back.util.ConnectorUtil;
import org.viewer.hub.back.util.DateTimeUtil;
import org.viewer.hub.back.util.JsonUtil;
import org.viewer.hub.back.util.MonoUtil;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Service
public class DicomConnectorQueryServiceImpl implements DicomConnectorQueryService {

	private static final int DICOM_WEB_PAGE_SIZE = 1000;

	private final DicomWebClientService dicomWebClientService;

	private final ConnectorConfigurationProperties connectorConfigurationProperties;

	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	private final ClientRegistrationRepository clientRegistrationRepository;

	@Value("${timeout.dicom-web}")
	private String dicomWebTimeoutDuration;

	@Value("${connector.weasis-manifest-dicom-web-level-limit}")
	private WeasisManifestDicomWebLevelLimitType weasisManifestDicomWebLevelLimitType;

	/**
	 * Autowired constructor.
	 */

	@Autowired
	public DicomConnectorQueryServiceImpl(final ConnectorConfigurationProperties connectorConfigurationProperties,
			final DicomWebClientService dicomWebClientService,
			final OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
			final ClientRegistrationRepository clientRegistrationRepository) {
		this.dicomWebClientService = dicomWebClientService;
		this.connectorConfigurationProperties = connectorConfigurationProperties;
		this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Override
	public void buildFromStudyAccessionNumbersDicomConnector(Manifest manifest, Set<String> studyAccessionNumbers,
			@Valid ConnectorProperty connector, Authentication authentication) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, studyAccessionNumbers,
				SearchCriteriaType.ACCESSION_NUMBER, authentication);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromPatientIdsDicomConnector(Manifest manifest, Set<String> patientIds,
			@Valid ConnectorProperty connector, @Valid SearchCriteria searchCriteria, Authentication authentication) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, patientIds,
				SearchCriteriaType.PATIENT_ID, authentication);

		// Apply patient request filters
		patientsFound = searchCriteria.applyPatientRequestSearchCriteriaFilters(patientsFound);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromStudyInstanceUidsDicomConnector(Manifest manifest, Set<String> studyInstanceUids,
			@Valid ConnectorProperty connector, Authentication authentication) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, studyInstanceUids,
				SearchCriteriaType.STUDY_INSTANCE_UID, authentication);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromSeriesInstanceUidsDicomConnector(Manifest manifest, Set<String> seriesInstanceUids,
			@Valid ConnectorProperty connector, Authentication authentication) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, seriesInstanceUids,
				SearchCriteriaType.SERIE_INSTANCE_UID, authentication);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromSopInstanceUidsDicomConnector(Manifest manifest, Set<String> sopInstanceUids,
			@Valid ConnectorProperty connector, Authentication authentication) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, sopInstanceUids,
				SearchCriteriaType.SOP_INSTANCE_UID, authentication);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@PostConstruct
	private void populateDicomWebWebClients() {
		// Populate webClients for OAuth2 dicom-web connectors
		this.connectorConfigurationProperties.getConnectors().forEach(((key, connectorProperty) -> {
			if (Objects.equals(connectorProperty.getType(), ConnectorType.DICOM_WEB)) {
				connectorProperty.getDicomWebConnector()
					.setWebClientWadoRs(this.dicomWebClientService.buildWebClientWadoRs(connectorProperty));
				connectorProperty.getDicomWebConnector()
					.setWebClientQidoRs(this.dicomWebClientService.buildWebClientQidoRs(connectorProperty));
			}
		}));
	}

	/**
	 * Retrieve the list of patients found from dicom requests
	 * @param connector Connector
	 * @param searchValues Search criteria
	 * @param searchCriteriaType Level of search
	 * @return Set of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResults(ConnectorProperty connector, Set<String> searchValues,
			SearchCriteriaType searchCriteriaType, Authentication authentication) {
		Set<Patient> patientsFound = new HashSet<>();
		if (Objects.equals(SearchCriteriaType.ACCESSION_NUMBER, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromAccessionNumbers(connector, searchValues,
					authentication);
		}
		else if (Objects.equals(SearchCriteriaType.PATIENT_ID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromPatientIds(connector, searchValues, authentication);
		}
		else if (Objects.equals(SearchCriteriaType.STUDY_INSTANCE_UID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromStudyInstanceUids(connector, searchValues,
					authentication);
		}
		else if (Objects.equals(SearchCriteriaType.SERIE_INSTANCE_UID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromSerieInstanceUids(connector, searchValues,
					authentication);
		}
		else if (Objects.equals(SearchCriteriaType.SOP_INSTANCE_UID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromSopInstanceUids(connector, searchValues,
					authentication);
		}
		return patientsFound;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with accession number
	 * criteria
	 * @param connector Connector
	 * @param accessionNumbers Accession numbers to look for
	 * @return Set of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromAccessionNumbers(ConnectorProperty connector,
			Set<String> accessionNumbers, Authentication authentication) {
		Set<Patient> patients = new HashSet<>();
		accessionNumbers.stream()
			// Retrieve studies and patient
			.map(accessionNumber -> this.retrieveDicomPatientWithStudiesFromAccessionNumber(accessionNumber, connector,
					authentication))
			.filter(Objects::nonNull)
			// Retrieve series and sop instances
			.forEach(patient -> this.retrieveDicomSeriesSopInstancesAndUpdatePatients(connector, patients, patient,
					authentication));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with patient ids criteria
	 * @param connector Connector
	 * @param patientIds Patient ids to look for
	 * @return Set of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromPatientIds(ConnectorProperty connector,
			Set<String> patientIds, Authentication authentication) {
		Set<Patient> patients = new HashSet<>();
		patientIds.stream()
			// Retrieve studies and patient
			.map(patientId -> this.retrieveDicomPatientStudiesFromPatientId(patientId, connector, authentication))
			.filter(Objects::nonNull)
			// Retrieve series and sop instances
			.forEach(patient -> this.retrieveDicomSeriesSopInstancesAndUpdatePatients(connector, patients, patient,
					authentication));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with study instance uids
	 * criteria
	 * @param connector Connector
	 * @param studyInstanceUids Study instance uids to look for
	 * @return Set of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromStudyInstanceUids(ConnectorProperty connector,
			Set<String> studyInstanceUids, Authentication authentication) {
		Set<Patient> patients = new HashSet<>();
		studyInstanceUids.stream()
			// Retrieve studies and patient
			.map(studyInstanceUid -> this.retrieveDicomPatientStudiesFromStudyInstanceUid(studyInstanceUid, connector,
					authentication))
			.filter(Objects::nonNull)
			// Retrieve series and sop instances
			.forEach(patient -> this.retrieveDicomSeriesSopInstancesAndUpdatePatients(connector, patients, patient,
					authentication));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with serie instance uids
	 * criteria
	 * @param connector Connector
	 * @param serieInstanceUids Serie instance uids to look for
	 * @return Set of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromSerieInstanceUids(ConnectorProperty connector,
			Set<String> serieInstanceUids, Authentication authentication) {
		Set<Patient> patients = new HashSet<>();
		serieInstanceUids.stream()
			// Retrieve serie, study and patient
			.map(serieInstanceUid -> this.retrieveDicomPatientStudySerieFromSerieInstanceUid(serieInstanceUid,
					connector, authentication))
			.filter(Objects::nonNull)
			// Retrieve sop instances
			.forEach(patient -> this.retrieveDicomSopInstancesAndUpdatePatients(connector, patients, patient,
					authentication));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with sop instance uids
	 * criteria
	 * @param connector Connector
	 * @param sopInstanceUids Sop instance uids to look for
	 * @return Set of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromSopInstanceUids(ConnectorProperty connector,
			Set<String> sopInstanceUids, Authentication authentication) {
		Set<Patient> patients = new HashSet<>();
		sopInstanceUids.stream()
			// Retrieve serie, study and patient
			.map(sopInstanceUid -> this.retrieveDicomPatientStudySerieSopInstanceUidFromSopInstanceUid(sopInstanceUid,
					connector, authentication))
			.filter(Objects::nonNull)
			// Merge in existing patient or add in list of patients
			.forEach(patient -> mergeOrAddInPatients(patients, patient));
		return patients;
	}

	/**
	 * Retrieve Sop Instances From Study/Serie Instance Uids and update patients If
	 * DICOM_WEB connector and a limitation is set on the weasis manifest configuration to
	 * limit to serie, the request is not made
	 * @param connector Connector
	 * @param patients Patients to update
	 * @param patient Patient result to add/merge
	 */
	private void retrieveDicomSopInstancesAndUpdatePatients(ConnectorProperty connector, Set<Patient> patients,
			Patient patient, Authentication authentication) {
		if (!(Objects.equals(connector.getType(), ConnectorType.DICOM_WEB) && (Objects
			.equals(weasisManifestDicomWebLevelLimitType, WeasisManifestDicomWebLevelLimitType.SERIE)
				|| Objects.equals(weasisManifestDicomWebLevelLimitType, WeasisManifestDicomWebLevelLimitType.STUDY)))) {
			// Retrieve sop instances
			this.retrieveDicomSopInstancesFromStudySerieInstanceUids(patient, connector, authentication);
		}
		// Merge in existing patient or add in list of patients
		mergeOrAddInPatients(patients, patient);
	}

	/**
	 * Retrieve Series and Sop Instances and update patients. If DICOM_WEB connector and a
	 * limitation is set on the weasis manifest configuration to limit to serie or study,
	 * the requests are not made
	 * @param connector Connector
	 * @param patients Patients to update
	 * @param patient Patient result to add/merge
	 */
	private void retrieveDicomSeriesSopInstancesAndUpdatePatients(ConnectorProperty connector, Set<Patient> patients,
			Patient patient, Authentication authentication) {
		if (!(Objects.equals(connector.getType(), ConnectorType.DICOM_WEB)
				&& Objects.equals(weasisManifestDicomWebLevelLimitType, WeasisManifestDicomWebLevelLimitType.STUDY))) {
			// Retrieve series
			this.retrieveDicomSeriesFromStudyInstanceUid(patient, connector, authentication);

			if (!(Objects.equals(connector.getType(), ConnectorType.DICOM_WEB) && Objects
				.equals(weasisManifestDicomWebLevelLimitType, WeasisManifestDicomWebLevelLimitType.SERIE))) {
				// Retrieve sop instances
				this.retrieveDicomSopInstancesFromStudySerieInstanceUids(patient, connector, authentication);
			}
		}

		// Merge in existing patient or add in list of patients
		mergeOrAddInPatients(patients, patient);
	}

	/**
	 * Retrieve Patient/Study/Serie From Serie Instance Uids and create patient
	 * @param serieInstanceUid Serie instance uid
	 * @param connector Connector
	 */
	private Patient retrieveDicomPatientStudySerieFromSerieInstanceUid(String serieInstanceUid,
			ConnectorProperty connector, Authentication authentication) {
		List<Attributes> patientStudySerieAttributes = new ArrayList<>();

		if (Objects.equals(connector.getType(), ConnectorType.DICOM)) {
			// Define query to retrieve patient,studies, serie from serie instance uid and
			// process dicom query
			patientStudySerieAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.SERIES,
					this.definePatientStudySerieDicomParamsFromSerieInstanceUid(serieInstanceUid), true);
		}
		else {
			// Dicom-web query
			// Qido-rs to retrieve Study UID associated to the serie UID
			Attributes serieAttributes = retrieveQidoRsSerieAttributesFromSerieUid(serieInstanceUid, connector,
					authentication);

			if (serieAttributes.getString(Tag.StudyInstanceUID) != null) {
				// Qido-rs to retrieve Study attributes from studyUID
				Attributes studyAttributes = retrieveQidoRsStudyAttributesFromStudyUid(connector, authentication,
						serieAttributes.getString(Tag.StudyInstanceUID));
				serieAttributes.addAll(studyAttributes);
			}

			patientStudySerieAttributes.add(serieAttributes);
		}

		return this.createPatientFromPatientStudySerieAttributes(patientStudySerieAttributes, connector);
	}

	/**
	 * Retrieve serie attributes from serieUid by using Qido-Rs request
	 * @param serieInstanceUid Serie Instance Uid
	 * @param connector Connector to use
	 * @param authentication Authentication
	 * @return Attributes found
	 */
	private Attributes retrieveQidoRsSerieAttributesFromSerieUid(String serieInstanceUid, ConnectorProperty connector,
			Authentication authentication) {
		return this
			.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
					uriBuilder -> uriBuilder.path(EndPoint.SERIES_PATH)
						.queryParam(ParamName.DICOM_WEB_SERIES_INSTANCE_UID, serieInstanceUid)
						.queryParam(ParamName.INCLUDE_FIELD, ParamName.INCLUDE_FIELD_SERIE_ATTRIBUTES)
						.build(),
					connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication)
			.stream()
			.findFirst()
			.orElse(new Attributes());
	}

	/**
	 * Retrieve patient with studies from accession number and create patient
	 * @param accessionNumber Accession number
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientWithStudiesFromAccessionNumber(String accessionNumber,
			ConnectorProperty connector, Authentication authentication) {
		// Define query to retrieve patient,studies from accession number and process
		// dicom query
		List<Attributes> patientStudiesAttributes = Objects.equals(connector.getType(), ConnectorType.DICOM) ?
		// Define query to retrieve patient,studies from accession number and process
		// dicom query
				this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.STUDY,
						this.definePatientStudiesDicomParamsFromAccessionNumber(accessionNumber), false)
				:
				// Dicom-web query
				// Qido-rs to retrieve Patient Id and Study UIDs
				this.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
						uriBuilder -> uriBuilder.path(EndPoint.STUDIES_PATH)
							.queryParam(ParamName.DICOM_WEB_ACCESSION_NUMBER, accessionNumber)
							.build(),
						connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication);

		return createPatientFromPatientStudiesAttributes(patientStudiesAttributes);
	}

	/**
	 * Retrieve patient with studies from patient id and create patient
	 * @param patientId Patient id
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientStudiesFromPatientId(String patientId, ConnectorProperty connector,
			Authentication authentication) {
		List<Attributes> patientStudiesAttributes;
		// Dicom request
		if (Objects.equals(connector.getType(), ConnectorType.DICOM)) {
			patientStudiesAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.STUDY,
					this.definePatientStudiesDicomParamsFromPatientId(patientId), false);
		}
		else {
			// Dicom-web request
			// Qido-rs to retrieve studies metadata
			Function<UriBuilder, URI> uriBuilderURIFunction = ConnectorUtil
				.determineIssuerPatientIdDependingHl7Syntax(patientId) != null
						? (uriBuilder -> uriBuilder.path(EndPoint.STUDIES_PATH)
							.queryParam(ParamName.DICOM_WEB_PATIENT_ID,
									ConnectorUtil.determinePatientIdDependingHl7Syntax(patientId))
							.queryParam(ParamName.DICOM_WEB_ISSUER_OF_PATIENT_ID,
									ConnectorUtil.determineIssuerPatientIdDependingHl7Syntax(patientId))
							.build())
						: (uriBuilder -> uriBuilder.path(EndPoint.STUDIES_PATH)
							.queryParam(ParamName.DICOM_WEB_PATIENT_ID, patientId)
							.build());

			patientStudiesAttributes = this.retrieveDicomWebQueryResults(
					connector.getDicomWebConnector().getWebClientQidoRs(), uriBuilderURIFunction,
					connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication);
		}

		return createPatientFromPatientStudiesAttributes(patientStudiesAttributes);
	}

	/**
	 * Retrieve patient with studies from study instance uid and create patient
	 * @param studyInstanceUid Study instance uid
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientStudiesFromStudyInstanceUid(String studyInstanceUid,
			ConnectorProperty connector, Authentication authentication) {

		List<Attributes> patientStudiesAttributes = Objects.equals(connector.getType(), ConnectorType.DICOM) ?
		// Define query to retrieve patient,studies from study instance uid and process
		// dicom query
				this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.STUDY,
						this.definePatientStudiesDicomParamsFromStudyInstanceUid(studyInstanceUid), false)
				// Dicom-web query
				: List.of(retrieveQidoRsStudyAttributesFromStudyUid(connector, authentication, studyInstanceUid));

		return createPatientFromPatientStudiesAttributes(patientStudiesAttributes);
	}

	/**
	 * Retrieve patient,studies, serie, instance from sop instance uid uid and create
	 * patient
	 * @param sopInstanceUid Sop instance uid
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientStudySerieSopInstanceUidFromSopInstanceUid(String sopInstanceUid,
			ConnectorProperty connector, Authentication authentication) {
		List<Attributes> patientStudySerieSopInstanceAttributes = new ArrayList<>();

		// Define query to retrieve patient,studies, serie, instance from sop instance uid
		// and process dicom query
		if (Objects.equals(connector.getType(), ConnectorType.DICOM)) {
			patientStudySerieSopInstanceAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.IMAGE,
					this.definePatientStudySerieSopInstanceDicomParamsFromSopInstanceUid(sopInstanceUid), true);
		}
		else {
			// Dicom-web query

			// Qido-rs from SopInstanceUID to retrieve study/serie uids
			Attributes instanceAttributes = retrieveQidoRsInstanceAttributesFromSopInstanceUid(sopInstanceUid,
					connector, authentication);

			// Qido-rs to retrieve study/serie attributes
			if (instanceAttributes.getString(Tag.StudyInstanceUID) != null
					&& instanceAttributes.getString(Tag.SeriesInstanceUID) != null) {
				// Serie attributes
				Attributes serieAttributes = retrieveQidoRsSerieAttributesFromStudySerieUids(connector, authentication,
						instanceAttributes.getString(Tag.SeriesInstanceUID),
						instanceAttributes.getString(Tag.StudyInstanceUID));

				// Study attributes
				Attributes studyAttributes = retrieveQidoRsStudyAttributesFromStudyUid(connector, authentication,
						instanceAttributes.getString(Tag.StudyInstanceUID));

				instanceAttributes.addAll(serieAttributes);
				instanceAttributes.addAll(studyAttributes);
				patientStudySerieSopInstanceAttributes.add(instanceAttributes);
			}
		}

		return this.createPatientFromPatientStudySerieSopInstanceAttributes(patientStudySerieSopInstanceAttributes,
				connector);
	}

	/**
	 * Retrieve sop instance attributes from sop instance uid by using Qido-Rs request
	 * @param sopInstanceUid Sop Instance Uid
	 * @param connector Connector to use
	 * @param authentication Authentication
	 * @return Attributes found
	 */
	private Attributes retrieveQidoRsInstanceAttributesFromSopInstanceUid(String sopInstanceUid,
			ConnectorProperty connector, Authentication authentication) {
		return this
			.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
					uriBuilder -> uriBuilder.path(EndPoint.INSTANCES_PATH)
						.queryParam(ParamName.DICOM_WEB_SOP_INSTANCE_UID, sopInstanceUid)
						.queryParam(ParamName.INCLUDE_FIELD, ParamName.INCLUDE_FIELD_INSTANCE_ATTRIBUTES)
						.build(),
					connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication)
			.stream()
			.findFirst()
			.orElse(new Attributes());
	}

	/**
	 * Retrieve serie attributes from study/serie uids by using Qido-Rs request
	 * @param connector Connector to use
	 * @param authentication Authentication
	 * @param serieUID Serie Uid
	 * @param studyUID Study Uid
	 * @return Attributes found
	 */
	private Attributes retrieveQidoRsSerieAttributesFromStudySerieUids(ConnectorProperty connector,
			Authentication authentication, String serieUID, String studyUID) {
		return this
			.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
					uriBuilder -> uriBuilder.path(EndPoint.STUDIES_SERIES_PATH)
						.queryParam(ParamName.DICOM_WEB_SERIES_INSTANCE_UID, serieUID)
						.queryParam(ParamName.INCLUDE_FIELD, ParamName.INCLUDE_FIELD_SERIE_ATTRIBUTES)
						.build(studyUID),
					connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication)
			.stream()
			.findFirst()
			.orElse(new Attributes());
	}

	/**
	 * Retrieve study attributes from study uid by using Qido-Rs request
	 * @param connector Connector to use
	 * @param authentication Authentication
	 * @param studyUID Study Uid
	 * @return Attributes found
	 */
	private Attributes retrieveQidoRsStudyAttributesFromStudyUid(ConnectorProperty connector,
			Authentication authentication, String studyUID) {
		return this
			.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
					uriBuilder -> uriBuilder.path(EndPoint.STUDIES_PATH)
						.queryParam(ParamName.DICOM_WEB_STUDY_INSTANCE_UID, studyUID)
						.queryParam(ParamName.INCLUDE_FIELD, ParamName.INCLUDE_FIELD_STUDY_ATTRIBUTES)
						.build(),
					connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication)
			.stream()
			.findFirst()
			.orElse(new Attributes());
	}

	/**
	 * Execute dicom-web queries
	 * @param webClient WebClient
	 * @param uriBuilderURIFunction Function containing uri to apply
	 * @param connectorAuthenticationProperty ConnectorAuthenticationProperty
	 * @param authentication Authentication
	 * @return List<Attributes> found
	 */
	private List<Attributes> retrieveDicomWebQueryResults(WebClient webClient,
			Function<UriBuilder, URI> uriBuilderURIFunction,
			ConnectorAuthenticationProperty connectorAuthenticationProperty, Authentication authentication) {
		// Build Get Web Client
		WebClient.RequestHeadersUriSpec<?> webClientGet = webClient.get();

		// Case OAuth2 authentication with authorization code
		if (Objects.equals(connectorAuthenticationProperty.getType(), ConnectorAuthType.OAUTH2)
				&& authentication != null) {
			ClientRegistration clientRegistration = clientRegistrationRepository
				.findByRegistrationId(connectorAuthenticationProperty.getOauth2().getOidcId());
			if (clientRegistration != null && AuthorizationGrantType.AUTHORIZATION_CODE
				.equals(clientRegistration.getAuthorizationGrantType())) {
				webClientGet.attributes(oauth2AuthorizedClient(this.oAuth2AuthorizedClientService
					.loadAuthorizedClient(clientRegistration.getRegistrationId(), authentication.getName())));
			}
		}

		// Call pacs api in dicom-web to retrieve Attributes
		return retrieveDicomWebQueryResultsRequest(uriBuilderURIFunction, webClientGet, 0, new ArrayList<>()).block();
	}

	/**
	 * Execute dicom-web queries using pagination by calling recursive method
	 * @param uriBuilderURIFunction Function to determine uri
	 * @param webClientGet GET WeClient
	 * @param offset Pagination offset
	 * @param accumulator Used to store the intermediate results when calling recursive
	 * method
	 * @return attributes found
	 */
	private Mono<List<Attributes>> retrieveDicomWebQueryResultsRequest(Function<UriBuilder, URI> uriBuilderURIFunction,
			WebClient.RequestHeadersUriSpec<?> webClientGet, int offset, List<Attributes> accumulator) {
		// Recursive request
		// Modify the function to handle page
		return webClientGet
			.uri(uriBuilder -> uriBuilderURIFunction.apply(
					uriBuilder.queryParam(ParamName.LIMIT, DICOM_WEB_PAGE_SIZE).queryParam(ParamName.OFFSET, offset)))
			.header(HttpHeaders.ACCEPT, HeaderType.APPLICATION_DICOM_JSON.getCode())
			.retrieve()
			.onStatus(httpStatus -> Objects.equals(HttpStatus.FORBIDDEN.value(), httpStatus.value()),
					MonoUtil.buildMonoError(Message.PACS_API_NO_ACCESS))
			.onStatus(HttpStatusCode::is4xxClientError, MonoUtil.buildMonoError(Message.PACS_API_CLIENT_ERROR))
			.onStatus(HttpStatusCode::is5xxServerError, MonoUtil.buildMonoError(Message.PACS_API_SERVER_ERROR))
			.bodyToMono(String.class)
			.map(JsonUtil::transformJsonToAttributes)
			.flatMapIterable(Function.identity())
			.collectList()
			.flatMap(pageList -> {
				// Add current page in the accumulator
				accumulator.addAll(pageList);
				// If the retrieved page contains exactly the DICOM_WEB_PAGE_SIZE elements
				// that means that there is maybe another page
				// Otherwise last page, return the completed accumulator
				return pageList.size() == DICOM_WEB_PAGE_SIZE
						? retrieveDicomWebQueryResultsRequest(uriBuilderURIFunction, webClientGet,
								offset + DICOM_WEB_PAGE_SIZE, accumulator)
						: Mono.just(accumulator);
			})
			.timeout(Duration.ofSeconds(Long.parseLong(this.dicomWebTimeoutDuration)),
					Mono.error(new TechnicalException(Message.PACS_SERVER_NOT_AVAILABLE)));
	}

	/**
	 * Merge patient found in the list of existing patient list
	 * @param patients Patients to update
	 * @param patient Patient result to add/merge
	 */
	private static void mergeOrAddInPatients(Set<Patient> patients, Patient patient) {
		// Merge in existing patient or add in list of patients
		Optional<Patient> optionalPatient = patients.stream()
			.filter(p -> Objects.equals(p.getPatientID(), patient.getPatientID()))
			.findFirst();
		if (optionalPatient.isPresent()) {
			optionalPatient.get().merge(patient);
		}
		else {
			patients.add(patient);
		}
	}

	/**
	 * Create patient from Patient/Study Attributes
	 * @param patientStudiesAttributes Attributes to use
	 * @return Patient created
	 */
	private static Patient createPatientFromPatientStudiesAttributes(List<Attributes> patientStudiesAttributes) {
		Patient patient = null;

		// Retrieve studies
		Set<Study> studies = patientStudiesAttributes.stream()
			.map(studyFound -> new Study(studyFound.getString(Tag.StudyInstanceUID),
					studyFound.getString(Tag.StudyDescription),
					DateTimeUtil.toLocalDate(studyFound.getDate(Tag.StudyDate)),
					DateTimeUtil.toLocalTime(studyFound.getDate(Tag.StudyTime)),
					studyFound.getString(Tag.AccessionNumber), studyFound.getString(Tag.StudyID),
					studyFound.getString(Tag.ReferringPhysicianName)))
			.collect(Collectors.toSet());

		Optional<Attributes> optionalPatient = patientStudiesAttributes.stream().findFirst();
		if (optionalPatient.isPresent()) {
			Attributes attributesPatient = optionalPatient.get();
			// Build patient
			patient = buildPatientFromAttributes(attributesPatient);
			// Set studies found
			patient.setStudies(studies);
		}
		return patient;
	}

	/**
	 * Create patient from Patient/Study/Serie/Sop instance Attributes
	 * @param patientStudySerieSopInstanceAttributes Attributes to use
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient createPatientFromPatientStudySerieSopInstanceAttributes(
			List<Attributes> patientStudySerieSopInstanceAttributes, ConnectorProperty connector) {
		Patient patient = null;
		// Retrieve attribute
		Optional<Attributes> optionalPatientStudySerieSopInstanceAttributes = patientStudySerieSopInstanceAttributes
			.stream()
			.findFirst();

		if (optionalPatientStudySerieSopInstanceAttributes.isPresent()) {
			Attributes patientStudySerieSopInstanceAttribute = optionalPatientStudySerieSopInstanceAttributes.get();
			// Instance
			Instance instance = this.buildSopInstanceFromAttribute(patientStudySerieSopInstanceAttribute);
			// Serie
			Serie serie = buildSerieFromAttribute(connector, patientStudySerieSopInstanceAttribute);
			// Study
			Study study = buildStudyFromAttribute(patientStudySerieSopInstanceAttribute);
			// Patient
			patient = buildPatientFromAttributes(patientStudySerieSopInstanceAttribute);
			// Add in lists
			serie.getInstances().add(instance);
			study.getSeries().add(serie);
			patient.getStudies().add(study);
		}

		return patient;
	}

	/**
	 * Create patient from Patient/Study/Serie Attributes
	 * @param patientStudySerieAttributes Attributes to use
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient createPatientFromPatientStudySerieAttributes(List<Attributes> patientStudySerieAttributes,
			ConnectorProperty connector) {
		Patient patient = null;
		// Retrieve serie
		Optional<Attributes> optionalPatientStudySerieAttributes = patientStudySerieAttributes.stream().findFirst();

		if (optionalPatientStudySerieAttributes.isPresent()) {
			Attributes patientStudySerieAttribute = optionalPatientStudySerieAttributes.get();
			// Serie
			Serie serie = buildSerieFromAttribute(connector, patientStudySerieAttribute);
			// Study
			Study study = buildStudyFromAttribute(patientStudySerieAttribute);
			// Patient
			patient = buildPatientFromAttributes(patientStudySerieAttribute);
			// Add in lists
			study.getSeries().add(serie);
			patient.getStudies().add(study);
		}

		return patient;
	}

	/**
	 * Execute dicom requests depending on parameters Dicom Params
	 * @param connector Connector
	 * @param queryRetrieveLevel Query Level
	 * @param dicomParams Dicom params
	 * @param useQueryRelational Flag to know if relational queries should be used
	 * @return List of attributes found
	 */
	private List<Attributes> retrieveDicomQueryResults(ConnectorProperty connector,
			QueryRetrieveLevel queryRetrieveLevel, List<DicomParam> dicomParams, boolean useQueryRelational) {
		DicomState state = CFind.process(
				connector.getDicomConnector().getDimse().retrieveAdvancedParamsFromProperties(useQueryRelational),
				new DicomNode(connector.getDicomConnector().getDimse().getCallingAet()),
				connector.getDicomConnector().getDimse().retrieveDicomNodeFromProperties(), 0, queryRetrieveLevel,
				dicomParams.toArray(new DicomParam[] {}));

		// Retrieve query results from dicom
		return state.getDicomRSP();
	}

	/**
	 * Build new Patient from attributes in parameter
	 * @param attributesPatient Attributes to use
	 * @return Patient created
	 */
	private static Patient buildPatientFromAttributes(Attributes attributesPatient) {
		return new Patient(attributesPatient.getString(Tag.PatientID), attributesPatient.getString(Tag.PatientName),
				attributesPatient.getString(Tag.IssuerOfPatientID),
				attributesPatient.getDate(Tag.PatientBirthDate) != null
						? DateTimeUtil.toLocalDate(attributesPatient.getDate(Tag.PatientBirthDate)) : null,
				attributesPatient.getDate(Tag.PatientBirthTime) != null
						? DateTimeUtil.toLocalTime(attributesPatient.getDate(Tag.PatientBirthTime)) : null,
				attributesPatient.getString(Tag.PatientSex) != null
						? DicomPatientSex.valueOf(attributesPatient.getString(Tag.PatientSex)) : null);
	}

	/**
	 * Build new Instance from attributes in parameter
	 * @param patientStudySerieSopInstanceAttribute Attributes to use
	 * @return Instance created
	 */
	private Instance buildSopInstanceFromAttribute(Attributes patientStudySerieSopInstanceAttribute) {
		return new Instance(patientStudySerieSopInstanceAttribute.getString(Tag.SOPInstanceUID),
				patientStudySerieSopInstanceAttribute.getString(Tag.InstanceNumber) != null
						? Integer.parseInt(patientStudySerieSopInstanceAttribute.getString(Tag.InstanceNumber)) : null);
	}

	/**
	 * Build new Serie from attributes in parameter
	 * @param connector Connector
	 * @param patientStudySerieAttribute Attributes to use
	 * @return Serie created
	 */
	private static Serie buildSerieFromAttribute(ConnectorProperty connector, Attributes patientStudySerieAttribute) {
		return new Serie(patientStudySerieAttribute.getString(Tag.SeriesInstanceUID),
				patientStudySerieAttribute.getString(Tag.SeriesDescription),
				patientStudySerieAttribute.getString(Tag.SeriesNumber) != null
						? Integer.parseInt(patientStudySerieAttribute.getString(Tag.SeriesNumber)) : null,
				patientStudySerieAttribute.getString(Tag.Modality),
				connector.getWeasis().getManifest().getTransferSyntaxUid(),
				connector.getWeasis().getManifest().getCompressionRate());
	}

	/**
	 * Build new Study from attributes in parameter
	 * @param patientStudySerieAttribute Attributes to use
	 * @return Study created
	 */
	private static Study buildStudyFromAttribute(Attributes patientStudySerieAttribute) {
		return new Study(patientStudySerieAttribute.getString(Tag.StudyInstanceUID),
				patientStudySerieAttribute.getString(Tag.StudyDescription),
				DateTimeUtil.toLocalDate(patientStudySerieAttribute.getDate(Tag.StudyDate)),
				DateTimeUtil.toLocalTime(patientStudySerieAttribute.getDate(Tag.StudyTime)),
				patientStudySerieAttribute.getString(Tag.AccessionNumber),
				patientStudySerieAttribute.getString(Tag.StudyID),
				patientStudySerieAttribute.getString(Tag.ReferringPhysicianName));
	}

	/**
	 * Retrieve series from study instance uids and fill patient in parameter
	 * @param patient Patient to fill
	 * @param connector Connector
	 * @param authentication Authentication
	 */
	private void retrieveDicomSeriesFromStudyInstanceUid(Patient patient, ConnectorProperty connector,
			Authentication authentication) {
		patient.getStudies().forEach(study -> {

			List<Attributes> seriesAttributes = Objects.equals(connector.getType(), ConnectorType.DICOM) ?
			// Define and process dicom query to retrieve series from study instance uid
					this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.SERIES,
							this.defineSeriesDicomParamsFromStudyInstanceUid(study.getStudyInstanceUID()), false)
					:
			// Dicom-web query
					this.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
							uriBuilder -> uriBuilder.path(EndPoint.STUDIES_SERIES_PATH)
								.build(study.getStudyInstanceUID()),
							connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication);

			// Retrieve series from attributes and update study
			study.setSeries(seriesAttributes.stream()
				.map(s -> new Serie(s.getString(Tag.SeriesInstanceUID), s.getString(Tag.SeriesDescription),
						s.getString(Tag.SeriesNumber) == null ? null : Integer.parseInt(s.getString(Tag.SeriesNumber)),
						s.getString(Tag.Modality), connector.getWeasis().getManifest().getTransferSyntaxUid(),
						connector.getWeasis().getManifest().getCompressionRate()))
				.collect(Collectors.toSet()));
		});
	}

	/**
	 * Retrieve Sop instances from study/serie instance uids and fill patient in parameter
	 * @param patient Patient to fill
	 * @param connector Connector
	 * @param authentication Authentication
	 */
	private void retrieveDicomSopInstancesFromStudySerieInstanceUids(Patient patient, ConnectorProperty connector,
			Authentication authentication) {
		patient.getStudies().forEach(study -> study.getSeries().forEach(serie -> {
			// Define and process dicom query to retrieve sop instances from serie
			// instance uid and study instance uid
			List<Attributes> sopInstancesAttributes = Objects.equals(connector.getType(), ConnectorType.DICOM) ?
			// Define and process dicom query to retrieve sop instances from serie
			// instance uid and study instance uid
					this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.IMAGE,
							this.defineSopInstanceDicomParamsFromStudySerieInstanceUids(study.getStudyInstanceUID(),
									serie.getSeriesInstanceUID()),
							false)
					:
			// Dicom-web query
					this.retrieveDicomWebQueryResults(connector.getDicomWebConnector().getWebClientQidoRs(),
							uriBuilder -> uriBuilder.path(EndPoint.STUDIES_SERIES_INSTANCES_PATH)
								.build(study.getStudyInstanceUID(), serie.getSeriesInstanceUID()),
							connector.getDicomWebConnector().getQidoRs().getAuthentication(), authentication);

			// Retrieve instances from attributes and update serie
			serie.setInstances(sopInstancesAttributes.stream()
				.map(s -> new Instance(s.getString(Tag.SOPInstanceUID),
						s.getString(Tag.InstanceNumber) == null ? null
								: Integer.parseInt(s.getString(Tag.InstanceNumber))))
				.collect(Collectors.toSet()));
		}));
	}

	/**
	 * Define dicom params with StudyInstanceUID and SeriesInstanceUID as search criteria
	 * @param studyInstanceUID search criteria
	 * @param seriesInstanceUID search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> defineSopInstanceDicomParamsFromStudySerieInstanceUids(String studyInstanceUID,
			String seriesInstanceUID) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.StudyInstanceUID, studyInstanceUID),
				new DicomParam(Tag.SeriesInstanceUID, seriesInstanceUID),
				// Return Keys
				CFind.SOPInstanceUID, CFind.InstanceNumber);
	}

	/**
	 * Define dicom params with AccessionNumber as search criteria
	 * @param accessionNumber search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudiesDicomParamsFromAccessionNumber(String accessionNumber) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.AccessionNumber, accessionNumber),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime,
				CFind.StudyInstanceUID, CFind.StudyID);
	}

	/**
	 * Define dicom params with StudyInstanceUID as search criteria
	 * @param studyInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> defineSeriesDicomParamsFromStudyInstanceUid(String studyInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.StudyInstanceUID, studyInstanceUid),
				// Return Keys
				CFind.SeriesInstanceUID, CFind.Modality, CFind.SeriesNumber, CFind.SeriesDescription);
	}

	/**
	 * Define dicom params with PatientID and IssuerOfPatientID as search criteria
	 * @param patientId search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudiesDicomParamsFromPatientId(String patientId) {
		// Determine dicom params patient id / issuer of patient id depending on the HL7
		// syntax
		DicomParam dicomParamPatientID = new DicomParam(Tag.PatientID,
				ConnectorUtil.determinePatientIdDependingHl7Syntax(patientId));
		DicomParam dicomParamIssuerOfPatientID = new DicomParam(Tag.IssuerOfPatientID,
				ConnectorUtil.determineIssuerPatientIdDependingHl7Syntax(patientId));

		return Arrays.asList(
				// Matching Keys
				dicomParamPatientID, dicomParamIssuerOfPatientID,
				// Return Keys
				CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex, CFind.ReferringPhysicianName,
				CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime, CFind.StudyInstanceUID, CFind.StudyID);
	}

	/**
	 * Define dicom params with StudyInstanceUID as search criteria
	 * @param studyInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudiesDicomParamsFromStudyInstanceUid(String studyInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.StudyInstanceUID, studyInstanceUid),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime, CFind.StudyID);
	}

	/**
	 * Define dicom params with SeriesInstanceUID as search criteria
	 * @param serieInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudySerieDicomParamsFromSerieInstanceUid(String serieInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.SeriesInstanceUID, serieInstanceUid),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime,
				CFind.AccessionNumber, CFind.StudyInstanceUID, CFind.StudyID, CFind.Modality, CFind.SeriesNumber,
				CFind.SeriesDescription);
	}

	/**
	 * Define dicom params with SOPInstanceUID as search criteria
	 * @param sopInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudySerieSopInstanceDicomParamsFromSopInstanceUid(String sopInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.SOPInstanceUID, sopInstanceUid),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime,
				CFind.AccessionNumber, CFind.StudyInstanceUID, CFind.StudyID, CFind.Modality, CFind.SeriesNumber,
				CFind.SeriesDescription, CFind.InstanceNumber);
	}

}
