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
import org.viewer.hub.back.config.properties.MicroDicomConfigurationProperties;
import org.viewer.hub.back.controller.exception.NoContentException;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.property.Command;
import org.viewer.hub.back.model.property.ConnectorServerProperty;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.service.ConnectorService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
class MicroDicomDisplayServiceImplTest {

	@Mock
	private ConnectorService connectorService;

	@Mock
	private MicroDicomConfigurationProperties microDicomConfigurationProperties;

	@InjectMocks
	private MicroDicomDisplayServiceImpl microDicomDisplayService;

	private Map<String, ConnectorServerProperty> archivesConfig;

	@BeforeEach
	void setUp() {
		Command command = new Command();
		command.setProtocol("microdicom://");
		command.setContext("");

		ConnectorServerProperty archiveProperty = ConnectorServerProperty.builder()
				.context("PACS")
				.url("localhost")
				.port("11112")
				.build();

		archivesConfig = new HashMap<>();
		archivesConfig.put("test-archive", archiveProperty);

		when(microDicomConfigurationProperties.getCommand()).thenReturn(command);
		when(microDicomConfigurationProperties.getArchives()).thenReturn(archivesConfig);
	}

	// ========== Tests with ArchiveSearchCriteria and PatientID ==========

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_archiveSearchCriteria_and_patientId_should_returnValidUrl() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result, "Result should not be null - check that connectorService mock returns test-archive");
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"), "Result should start with microdicom:// but was: " + result);
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_archiveSearchCriteria_and_studyUid_should_returnValidUrl() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"));
		assertTrue(result.contains("1.2.3.4.5"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_archiveSearchCriteria_and_accessionNumber_should_returnValidUrl() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> accessionNumbers = new LinkedHashSet<>();
		accessionNumbers.add("ACC001");
		searchCriteria.setAccessionNumber(accessionNumbers);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"));
		assertTrue(result.contains("ACC001"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_archiveSearchCriteria_and_seriesUid_should_returnValidUrlWithStudyUid() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		searchCriteria.setSeriesUID(seriesUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"));
		assertTrue(result.contains("1.2.3.4.5"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_archiveSearchCriteria_and_sopInstanceUid_should_returnValidUrlWithStudyUid() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> objectUIDs = new LinkedHashSet<>();
		objectUIDs.add("1.2.3.4.5.6.7");
		searchCriteria.setObjectUID(objectUIDs);

		Patient patient = createPatientWithStudy("P001", "1.2.3.4.5");
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"));
		assertTrue(result.contains("1.2.3.4.5"));
	}

	// ========== Tests with IHESearchCriteria ==========

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_iheSearchCriteria_and_patientId_should_returnValidUrl() {
		// Given
		IHESearchCriteria searchCriteria = new IHESearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);
		searchCriteria.setPatientID("P001");

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"));
		assertTrue(result.contains("P001"));
	}


	// ========== Tests with archive without port ==========

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_archiveWithoutPort_should_returnValidUrl() {
		// Given
		ConnectorServerProperty archivePropertyNoPort = ConnectorServerProperty.builder()
				.context("PACS")
				.url("localhost")
				.port(null)
				.build();

		archivesConfig.put("test-archive-no-port", archivePropertyNoPort);

		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive-no-port");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive-no-port");

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.startsWith("microdicom://"));
		assertTrue(result.contains("PACS"));
		assertTrue(result.contains("localhost"));
		assertFalse(result.contains("11112")); // No port in URL
	}

	// ========== Tests with null archive ==========

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_nullArchive_should_returnNull() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn(null);

		// When
		String result = microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);

		// Then
		assertNull(result);
	}

	// ========== Tests for exceptions ==========

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_multipleArchives_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive-1");
		archives.add("test-archive-2");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive));

		assertTrue(exception.getMessage().contains("Micro Dicom supports only one archive parameter"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_multiplePatientIds_should_throwParameterException() {
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

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive));

		assertTrue(exception.getMessage().contains("MicroDicom supports only one PatientId"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_patientIdAndStudyUid_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> patientIds = new LinkedHashSet<>();
		patientIds.add("P001");
		searchCriteria.setPatientID(patientIds);

		LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
		studyUIDs.add("1.2.3.4.5");
		searchCriteria.setStudyUID(studyUIDs);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive));

		assertTrue(exception.getMessage().contains("MicroDicom supports only one PatientId"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_seriesUidButNoStudyFound_should_throwNoContentException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		searchCriteria.setSeriesUID(seriesUIDs);

		Patient patient = new Patient();
		patient.setPatientID("P001");
		patient.setStudies(new HashSet<>()); // Empty studies

		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", patients);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		NoContentException exception = assertThrows(NoContentException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive));

		assertTrue(exception.getMessage().contains("Study UID not found"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_seriesUidButNoPatientsInArchive_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		searchCriteria.setSeriesUID(seriesUIDs);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("test-archive", new HashSet<>()); // Empty patients

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_seriesUidButArchiveNotInMap_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		searchCriteria.setSeriesUID(seriesUIDs);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		// Archive not in map

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	@Test
	void when_retrieveMicroDicomLaunchUrl_with_seriesUidAndNullPatientsByArchive_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("test-archive");
		searchCriteria.setArchive(archives);

		LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
		seriesUIDs.add("1.2.3.4.5.6");
		searchCriteria.setSeriesUID(seriesUIDs);

		when(connectorService.retrieveFirstDefaultOrFirstSpecificConnector(any())).thenReturn("test-archive");

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, null));

		assertTrue(exception.getMessage().contains("No patient found"));
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
}

