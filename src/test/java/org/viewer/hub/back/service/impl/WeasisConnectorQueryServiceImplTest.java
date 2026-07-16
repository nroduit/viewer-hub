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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.enums.QueryLevelType;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.*;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.DbConnectorQueryService;
import org.viewer.hub.back.service.DicomConnectorQueryService;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@Slf4j
class WeasisConnectorQueryServiceImplTest {

	private final ConnectorConfigurationProperties connectorConfigurationPropertiesMock = Mockito
		.mock(ConnectorConfigurationProperties.class);

	private final DbConnectorQueryService dbConnectorQueryServiceMock = Mockito.mock(DbConnectorQueryService.class);

	private final DicomConnectorQueryService dicomConnectorQueryServiceMock = Mockito
		.mock(DicomConnectorQueryService.class);

	private final ConnectorService connectorServiceMock = Mockito.mock(ConnectorService.class);

	private WeasisConnectorQueryServiceImpl connectorQueryService;

	private ConnectorProperty connectorPropertyDbA;

	private ConnectorProperty connectorPropertyDbb;

	private ConnectorProperty connectorPropertyDicomA;

	private ConnectorProperty connectorPropertyDicomB;

	private ConnectorProperty connectorPropertyDicomWebA;

	@BeforeEach
	public void setUp() {

		// Mock connectorConfigurationProperties
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		DbConnectorQueryProperty dbConnectorQueryProperty = new DbConnectorQueryProperty("select",
				"accessionNumberColumn", "patientIdColumn", "studyInstanceUidColumn", "serieInstanceUidColumn",
				"sopInstanceUidColumn");
		DbConnectorProperty dbConnectorProperty = DbConnectorProperty.builder()
			.user("user")
			.password("password")
			.uri("uri")
			.driver("driver")
			.query(dbConnectorQueryProperty)
			.build();

		DicomConnectorProperty dicomConnectorProperty = DicomConnectorProperty.builder()
			.dimse(DicomConnectorDimseProperty.builder()
				.callingAet("callingAet")
				.aet("aet")
				.host("host")
				.port(1)
				.build())
			.wado(ConnectorWadoProperty.builder().build())
			.build();

		DicomWebConnectorProperty dicomWebConnectorProperty = DicomWebConnectorProperty.builder()
			.wadoRs(ConnectorDicomWebProperty.builder().build())
			.qidoRs(ConnectorDicomWebProperty.builder().build())
			.build();

		SearchCriteriaProperty searchCriteria = new SearchCriteriaProperty(new HashSet<>());

		connectorPropertyDbA = ConnectorProperty.builder()
			.id("idDbA")
			.type(ConnectorType.DB)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		connectorPropertyDbb = ConnectorProperty.builder()
			.id("idDbB")
			.type(ConnectorType.DB)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		connectorPropertyDicomB = ConnectorProperty.builder()
			.id("idDicomB")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		connectorPropertyDicomWebA = ConnectorProperty.builder()
			.id("idDicomWebA")
			.type(ConnectorType.DICOM_WEB)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		config.put("idDbA", connectorPropertyDbA);
		config.put("idDbB", connectorPropertyDbb);
		config.put("idDicomA", connectorPropertyDicomA);
		config.put("idDicomB", connectorPropertyDicomB);
		config.put("idDicomWebA", connectorPropertyDicomWebA);

		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Create mocked service
		this.connectorQueryService = new WeasisConnectorQueryServiceImpl(this.dbConnectorQueryServiceMock,
				this.dicomConnectorQueryServiceMock, this.connectorServiceMock);
	}

	@Test
	void when_fillingManifestFromPatientIds_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		ArchiveSearchCriteria archiveSearchCriteria = new ArchiveSearchCriteria();
		archiveSearchCriteria.getArchive().add("idDbA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDbA)));

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), archiveSearchCriteria, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromPatientIdsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromPatientIdsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromPatientIds_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		ArchiveSearchCriteria archiveSearchCriteria = new ArchiveSearchCriteria();
		archiveSearchCriteria.getArchive().add("idDicomA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), archiveSearchCriteria, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromPatientIdsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromPatientIdsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromPatientIds_with_dicomWebArchive_should_callCorrectConnectorService() {
		// Init data
		ArchiveSearchCriteria archiveSearchCriteria = new ArchiveSearchCriteria();
		archiveSearchCriteria.getArchive().add("idDicomWebA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomWebA)));

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), archiveSearchCriteria, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromPatientIdsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromPatientIdsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromPatientIds_with_deactivatedPatientIdSearchCriteria_should_notCallConnectorService() {
		// Init data
		ArchiveSearchCriteria archiveSearchCriteria = new ArchiveSearchCriteria();
		archiveSearchCriteria.getArchive().add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		ConnectorProperty connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(Set.of(QueryLevelType.PATIENT_ID)))
			.weasis(WeasisConnectorProperty.builder().build())
			.build();
		config.put("idDicomA", connectorPropertyDicomA);

		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), archiveSearchCriteria, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromPatientIdsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromPatientIdsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDbA)));

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromStudyInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromStudyInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_dicomWebArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomWebA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomWebA)));

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromStudyInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_deactivatedStudyInstanceUidSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		ConnectorProperty connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(Set.of(QueryLevelType.STUDY_INSTANCE_UID)))
			.weasis(WeasisConnectorProperty.builder().build())
			.build();

		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDbA)));

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromStudyAccessionNumbersDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyAccessionNumbersDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyAccessionNumbersDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromStudyAccessionNumbersDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_dicomWebArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomWebA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomWebA)));

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyAccessionNumbersDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromStudyAccessionNumbersDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_deactivatedAccessionNumberSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		ConnectorProperty connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(Set.of(QueryLevelType.STUDY_ACCESSION_NUMBER)))
			.weasis(WeasisConnectorProperty.builder().build())
			.build();
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyAccessionNumbersDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromStudyAccessionNumbersDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDbA)));

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromSeriesInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSeriesInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSeriesInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromSeriesInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_dicomWebArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomWebA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomWebA)));

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSeriesInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromSeriesInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_deactivatedSeriesInstanceUidSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		ConnectorProperty connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(Set.of(QueryLevelType.SERIE_INSTANCE_UID)))
			.weasis(WeasisConnectorProperty.builder().build())
			.build();

		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSeriesInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSeriesInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDbA)));

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromSopInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSopInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSopInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromSopInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_dicomWebArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomWebA");
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomWebA)));

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSopInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.retrievePatientsFromSopInstanceUidsDicomConnector(any(), any(), any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_deactivatedSopInstanceUidSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		ConnectorProperty connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(Set.of(QueryLevelType.SOP_INSTANCE_UID)))
			.weasis(WeasisConnectorProperty.builder().build())
			.build();
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);
		Mockito.when(this.connectorServiceMock.retrieveConnectors(any()))
			.thenReturn(new LinkedHashSet<>(List.of(connectorPropertyDicomA)));

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives, null);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSopInstanceUidsDbConnector(any(), any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.retrievePatientsFromSopInstanceUidsDicomConnector(any(), any(), any());
	}

}
