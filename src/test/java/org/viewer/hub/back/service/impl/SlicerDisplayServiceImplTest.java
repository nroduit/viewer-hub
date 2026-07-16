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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.viewer.hub.back.config.properties.SlicerConfigurationProperties;
import org.viewer.hub.back.controller.exception.NoContentException;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.property.Command;
import org.viewer.hub.back.model.property.ConnectorServerProperty;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.SecurityService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
class SlicerDisplayServiceImplTest {

	@Mock
	private ConnectorService connectorService;

	@Mock
	private SlicerConfigurationProperties slicerConfigurationProperties;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private SlicerDisplayServiceImpl slicerDisplayService;

	@Mock
	private Authentication authentication;

	@Mock
	private OAuth2AuthenticationToken oAuth2Authentication;

	private Command command;

	private Map<String, ConnectorServerProperty> archivesConfig;

	@BeforeEach
	void setUp() {
		command = new Command();
		command.setProtocol("slicer://");
		command.setContext("viewer");

		ConnectorServerProperty archiveProperty = ConnectorServerProperty.builder()
			.url("http://dicomweb-server.com")
			.port("8080")
			.context("/dicomweb")
			.build();

		archivesConfig = new HashMap<>();
		archivesConfig.put("test-archive", archiveProperty);

		when(slicerConfigurationProperties.getCommand()).thenReturn(command);
		when(slicerConfigurationProperties.getArchives()).thenReturn(archivesConfig);
	}

	// ========== Tests with ArchiveSearchCriteria and single study ==========

	@Test
	void when_retrieveSlicerLaunchUrl_with_archiveSearchCriteria_and_studyUid_should_returnValidUrl() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("slicer://viewer"));
		assertTrue(result.contains("studyUID=1.2.3.4.5"));
		assertTrue(result.contains("dicomweb_endpoint=http://dicomweb-server.com:8080/dicomweb"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_archiveSearchCriteria_and_patientId_with_oneStudy_should_returnValidUrl() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("studyUID=1.2.3.4.5"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_archiveSearchCriteria_and_accessionNumber_should_returnValidUrl() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> accessionNumbers = new LinkedHashSet<>();
		accessionNumbers.add("ACC001");
		searchCriteria.setAccessionNumber(accessionNumbers);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("studyUID=1.2.3.4.5"));
	}

	// ========== Tests with server without port ==========

	@Test
	void when_retrieveSlicerLaunchUrl_with_serverWithoutPort_should_returnValidUrl() {
		// Given
		ConnectorServerProperty serverPropertyNoPort = ConnectorServerProperty.builder()
			.url("http://dicomweb-server.com")
			.port(null)
			.context("/dicomweb")
			.build();

		archivesConfig.put("test-archive", serverPropertyNoPort);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("dicomweb_endpoint=http://dicomweb-server.com/dicomweb"));
		assertFalse(result.contains(":8080")); // No port in URL
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_serverWithoutContext_should_returnValidUrl() {
		// Given
		ConnectorServerProperty serverPropertyNoContext = ConnectorServerProperty.builder()
			.url("http://dicomweb-server.com")
			.port("8080")
			.context(null)
			.build();

		archivesConfig.put("test-archive", serverPropertyNoContext);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("dicomweb_endpoint=http://dicomweb-server.com:8080"));
	}

	// ========== Tests with OAuth2 authentication ==========

	@Test
	void when_retrieveSlicerLaunchUrl_with_oauth2Authentication_should_includeToken() {
		// Given
		when(oAuth2Authentication.isAuthenticated()).thenReturn(true);
		when(securityService.retrieveAccessToken(any())).thenReturn("test-access-token");

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive,
				oAuth2Authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("access_token=test-access-token"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_nonOAuth2Authentication_should_notIncludeToken() {
		// Given
		when(authentication.isAuthenticated()).thenReturn(true);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertFalse(result.contains("access_token="));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_nullAuthentication_should_notIncludeToken() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, null);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertFalse(result.contains("access_token="));
	}

	// ========== Tests for exceptions - archive validation ==========

	@Test
	void when_retrieveSlicerLaunchUrl_with_multipleArchives_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive-1");
		archives.add("test-archive-2");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("3D Slicer supports only one archive parameter"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_nullArchive_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn(null);

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertNotNull(exception.getMessage());
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_nullPatientsByArchive_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, null, authentication));

		assertNotNull(exception.getMessage());
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_archiveNotInMap_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("other-archive", new HashSet<>());

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertNotNull(exception.getMessage());
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_emptyPatientsSet_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", new HashSet<>()); // Empty set

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertNotNull(exception.getMessage());
	}

	// ========== Tests for exceptions - search criteria validation ==========

	@Test
	void when_retrieveSlicerLaunchUrl_with_multipleStudyUids_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		studyUIDs.add("1.2.3.4.6");
		searchCriteria.setStudyUID(studyUIDs);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("3D Slicer supports only one study UID parameter"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_multiplePatientIds_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		patientIds.add("P002");
		searchCriteria.setPatientID(patientIds);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("3D Slicer supports only one study UID parameter"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_patientIdButNoStudies_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Patient patient = new Patient();
		patient.setPatientID("P001");
		patient.setStudies(new HashSet<>()); // No studies

		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("No studies found"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_patientIdButMultipleStudies_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Patient patient = createPatientWithMultipleStudies("P001", Arrays.asList("1.2.3.4.5", "1.2.3.4.6"));
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("3D Slicer only supports to display one study"));
	}

	@Test
	void when_retrieveSlicerLaunchUrl_with_patientIdButEmptyPatientsSet_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", new HashSet<>()); // Empty patients

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("No studies found"));
	}

	// ========== Tests for exceptions - study UID not found ==========

	@Test
	void when_retrieveSlicerLaunchUrl_with_patientWithoutStudyUid_should_throwNoContentException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Patient patient = new Patient();
		patient.setPatientID("P001");
		patient.setStudies(new HashSet<>()); // No studies

		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		NoContentException exception = assertThrows(NoContentException.class,
				() -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("Study UID not found for Slicer Launch Url"));
	}

	// ========== Helper methods ==========

	private Patient createPatientWithStudy(String patientId, String studyInstanceUID) {
		Patient patient = new Patient();
		patient.setPatientID(patientId);

		Set<Serie> series = new HashSet<>();
		Serie serie = Serie.builder()
			.instances(new HashSet<>())
			.seriesInstanceUID("1.2.3.4.5.1")
			.seriesDescription("Test Series")
			.modality("CT")
			.build();
		series.add(serie);

		Study study = Study.builder()
			.studyInstanceUID(studyInstanceUID)
			.studyDescription("Test Study")
			.series(series)
			.build();

		Set<Study> studies = new HashSet<>();
		studies.add(study);
		patient.setStudies(studies);

		return patient;
	}

	private Patient createPatientWithMultipleStudies(String patientId, List<String> studyInstanceUIDs) {
		Patient patient = new Patient();
		patient.setPatientID(patientId);

		Set<Study> studies = new HashSet<>();
		for (String studyUID : studyInstanceUIDs) {
			Set<Serie> series = new HashSet<>();
			Serie serie = Serie.builder()
				.instances(new HashSet<>())
				.seriesInstanceUID("1.2.3.4.5.1")
				.seriesDescription("Test Series")
				.modality("CT")
				.build();
			series.add(serie);

			Study study = Study.builder()
				.studyInstanceUID(studyUID)
				.studyDescription("Test Study")
				.series(series)
				.build();
			studies.add(study);
		}
		patient.setStudies(studies);

		return patient;
	}

}
