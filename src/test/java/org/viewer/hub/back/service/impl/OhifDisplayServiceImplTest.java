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
import org.viewer.hub.back.config.properties.OhifConfigurationProperties;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.property.ConnectorServerProperty;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.SecurityService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
class OhifDisplayServiceImplTest {

	@Mock
	private ConnectorService connectorService;

	@Mock
	private OhifConfigurationProperties ohifConfigurationProperties;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private OhifDisplayServiceImpl ohifDisplayService;

	@Mock
	private Authentication authentication;

	@Mock
	private OAuth2AuthenticationToken oAuth2Authentication;

	private ConnectorServerProperty serverProperty;

	@BeforeEach
	void setUp() {
		serverProperty = ConnectorServerProperty.builder()
			.url("http://ohif-server.com")
			.port("8080")
			.context("/ohif")
			.build();

		when(ohifConfigurationProperties.getServer()).thenReturn(serverProperty);
		when(ohifConfigurationProperties.isTokenAuthQueryParam()).thenReturn(false);
	}

	// ========== Tests with ArchiveSearchCriteria and single archive ==========

	@Test
	void when_retrieveOhifLaunchUrl_with_archiveSearchCriteria_and_patientId_should_returnValidUrl() {
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
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("http://ohif-server.com:8080/ohif/viewer/test-archive"));
		assertTrue(result.contains("StudyInstanceUIDs=1.2.3.4.5"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_archiveSearchCriteria_and_studyUid_should_returnValidUrl() {
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
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("StudyInstanceUIDs=1.2.3.4.5"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_archiveSearchCriteria_and_seriesUid_should_returnValidUrlWithSeriesParam() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		searchCriteria.setSeriesUID(seriesUIDs);

		Patient patient = createPatientWithStudyAndSeries("P001", "1.2.3.4.5", "1.2.3.4.5.6");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("StudyInstanceUIDs=1.2.3.4.5"));
		assertTrue(result.contains("SeriesInstanceUIDs=1.2.3.4.5.6"));
		assertTrue(result.contains("initialSeriesInstanceUID=1.2.3.4.5.6"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_archiveSearchCriteria_and_sopInstanceUid_should_returnValidUrlWithSopParam() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> objectUIDs = new LinkedHashSet<>();
		objectUIDs.add("1.2.3.4.5.6.7");
		searchCriteria.setObjectUID(objectUIDs);

		Patient patient = createPatientWithStudyAndSeries("P001", "1.2.3.4.5", "1.2.3.4.5.6");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("StudyInstanceUIDs=1.2.3.4.5"));
		assertTrue(result.contains("SeriesInstanceUIDs=1.2.3.4.5.6"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_archiveSearchCriteria_and_multipleSeriesUids_should_notIncludeInitialSeries() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		seriesUIDs.add("1.2.3.4.5.7");
		searchCriteria.setSeriesUID(seriesUIDs);

		Patient patient = createPatientWithStudyAndMultipleSeries("P001", "1.2.3.4.5",
				Arrays.asList("1.2.3.4.5.6", "1.2.3.4.5.7"));
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("StudyInstanceUIDs=1.2.3.4.5"));
		assertTrue(result.contains("SeriesInstanceUIDs="));
		assertFalse(result.contains("initialSeriesInstanceUID=")); // Should not be
																	// present for
																	// multiple series
	}

	// ========== Tests with IHESearchCriteria ==========

	@Test
	void when_retrieveOhifLaunchUrl_with_iheSearchCriteria_and_patientId_should_returnValidUrl() {
		// Given
		IHESearchCriteria searchCriteria = new IHESearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);
		searchCriteria.setPatientID("P001");

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("StudyInstanceUIDs=1.2.3.4.5"));
		assertFalse(result.contains("initialSeriesInstanceUID=")); // IHE should not have
																	// initial series
		assertFalse(result.contains("initialSOPInstanceUID=")); // IHE should not have
																// initial SOP
	}

	// ========== Tests with server without port ==========

	@Test
	void when_retrieveOhifLaunchUrl_with_serverWithoutPort_should_returnValidUrl() {
		// Given
		ConnectorServerProperty serverPropertyNoPort = ConnectorServerProperty.builder()
			.url("http://ohif-server.com")
			.port(null)
			.context("/ohif")
			.build();

		when(ohifConfigurationProperties.getServer()).thenReturn(serverPropertyNoPort);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("http://ohif-server.com/ohif/viewer/test-archive"));
		assertFalse(result.contains(":8080")); // No port in URL
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_serverWithoutContext_should_returnValidUrl() {
		// Given
		ConnectorServerProperty serverPropertyNoContext = ConnectorServerProperty.builder()
			.url("http://ohif-server.com")
			.port("8080")
			.context(null)
			.build();

		when(ohifConfigurationProperties.getServer()).thenReturn(serverPropertyNoContext);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("http://ohif-server.com:8080/viewer/test-archive"));
	}

	// ========== Tests with OAuth2 authentication ==========

	@Test
	void when_retrieveOhifLaunchUrl_with_oauth2Authentication_and_tokenAuthEnabled_should_includeToken() {
		// Given
		when(ohifConfigurationProperties.isTokenAuthQueryParam()).thenReturn(true);
		when(oAuth2Authentication.isAuthenticated()).thenReturn(true);
		when(securityService.retrieveAccessToken(any())).thenReturn("test-access-token");

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive,
				oAuth2Authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("token=test-access-token"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_oauth2Authentication_and_tokenAuthDisabled_should_notIncludeToken() {
		// Given
		when(ohifConfigurationProperties.isTokenAuthQueryParam()).thenReturn(false);
		when(oAuth2Authentication.isAuthenticated()).thenReturn(true);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive,
				oAuth2Authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertFalse(result.contains("token="));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_nullAuthentication_should_notIncludeToken() {
		// Given
		when(ohifConfigurationProperties.isTokenAuthQueryParam()).thenReturn(true);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, null);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertFalse(result.contains("token="));
	}

	// ========== Tests for exceptions ==========

	@Test
	void when_retrieveOhifLaunchUrl_with_multipleArchives_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive-1");
		archives.add("test-archive-2");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("Ohif supports only one archive parameter"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_nullArchive_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn(null);

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("No patient found for determined first archive"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_nullPatientsByArchive_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class,
				() -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, null, authentication));

		assertTrue(exception.getMessage().contains("No patient found for determined first archive"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_archiveNotInMap_should_throwParameterException() {
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
				() -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("No patient found for determined first archive"));
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_emptyPatientsSet_should_throwParameterException() {
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
				() -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication));

		assertTrue(exception.getMessage().contains("No patient found for determined first archive"));
	}

	// ========== Tests with patients without studies ==========

	@Test
	void when_retrieveOhifLaunchUrl_with_patientsWithoutStudies_should_returnUrlWithoutStudyParams() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient = new Patient();
		patient.setPatientID("P001");
		patient.setStudies(new HashSet<>()); // No studies

		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertFalse(result.contains("StudyInstanceUIDs=")); // Should not have study
															// params
	}

	@Test
	void when_retrieveOhifLaunchUrl_with_multiplePatients_should_includeAllStudyUids() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		Patient patient1 = createPatientWithStudy("P001", "1.2.3.4.5");
		Patient patient2 = createPatientWithStudy("P002", "1.2.3.4.6");

		Set<Patient> patients = new HashSet<>();
		patients.add(patient1);
		patients.add(patient2);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains("StudyInstanceUIDs="));
		assertTrue(result.contains("1.2.3.4.5"));
		assertTrue(result.contains("1.2.3.4.6"));
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

	private Patient createPatientWithStudyAndSeries(String patientId, String studyInstanceUID,
			String seriesInstanceUID) {
		Patient patient = new Patient();
		patient.setPatientID(patientId);

		Set<Serie> series = new HashSet<>();
		Serie serie = Serie.builder()
			.instances(new HashSet<>())
			.seriesInstanceUID(seriesInstanceUID)
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

	private Patient createPatientWithStudyAndMultipleSeries(String patientId, String studyInstanceUID,
			List<String> seriesInstanceUIDs) {
		Patient patient = new Patient();
		patient.setPatientID(patientId);

		Set<Serie> series = new HashSet<>();
		for (String seriesUID : seriesInstanceUIDs) {
			Serie serie = Serie.builder()
				.instances(new HashSet<>())
				.seriesInstanceUID(seriesUID)
				.seriesDescription("Test Series")
				.modality("CT")
				.build();
			series.add(serie);
		}

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

}
