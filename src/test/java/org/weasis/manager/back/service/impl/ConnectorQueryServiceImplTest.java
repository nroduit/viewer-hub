/*
 *
 *  * Copyright (c) 2022-20xx Weasis Team and other contributors.
 *  *
 *  * This program and the accompanying materials are made available under the terms of the Eclipse
 *  * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.weasis.manager.back.config.properties.ConnectorConfigurationProperties;
import org.weasis.manager.back.controller.exception.TechnicalException;
import org.weasis.manager.back.enums.ConnectorType;
import org.weasis.manager.back.enums.QueryLevelType;
import org.weasis.manager.back.model.WeasisSearchCriteria;
import org.weasis.manager.back.model.manifest.Manifest;
import org.weasis.manager.back.model.property.AuthenticationProperty;
import org.weasis.manager.back.model.property.BasicAuthenticationProperty;
import org.weasis.manager.back.model.property.ConnectorProperty;
import org.weasis.manager.back.model.property.DbConnectorProperty;
import org.weasis.manager.back.model.property.DbConnectorQueryProperty;
import org.weasis.manager.back.model.property.DicomConnectorProperty;
import org.weasis.manager.back.model.property.OAuth2AuthenticationProperty;
import org.weasis.manager.back.model.property.SearchCriteriaProperty;
import org.weasis.manager.back.model.property.WadoConnectorProperty;
import org.weasis.manager.back.service.DbConnectorQueryService;
import org.weasis.manager.back.service.DicomConnectorQueryService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ConnectorQueryServiceImplTest {

	private final ConnectorConfigurationProperties connectorConfigurationPropertiesMock = Mockito
		.mock(ConnectorConfigurationProperties.class);

	private final DbConnectorQueryService dbConnectorQueryServiceMock = Mockito.mock(DbConnectorQueryService.class);

	private final DicomConnectorQueryService dicomConnectorQueryServiceMock = Mockito
		.mock(DicomConnectorQueryService.class);

	private ConnectorQueryServiceImpl connectorQueryService;

	@BeforeEach
	public void setUp() {

		// Mock connectorConfigurationProperties
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();

		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, null);

		DbConnectorQueryProperty dbConnectorQueryProperty = new DbConnectorQueryProperty("select",
				"accessionNumberColumn", "patientIdColumn", "studyInstanceUidColumn", "serieInstanceUidColumn",
				"sopInstanceUidColumn");
		DbConnectorProperty dbConnectorProperty = new DbConnectorProperty("user", "password", "uri", "driver",
				dbConnectorQueryProperty);

		DicomConnectorProperty dicomConnectorProperty = new DicomConnectorProperty("callingAet", "aet", "host", 1, null,
				null, null);

		SearchCriteriaProperty searchCriteria = new SearchCriteriaProperty(new HashSet<>());

		ConnectorProperty connectorPropertyDbA = new ConnectorProperty("idDbA", ConnectorType.DB,
				new SearchCriteriaProperty(new HashSet<>()), wadoConnectorProperty, dbConnectorProperty,
				dicomConnectorProperty);
		ConnectorProperty connectorPropertyDbb = new ConnectorProperty("idDbB", ConnectorType.DB,
				new SearchCriteriaProperty(new HashSet<>()), wadoConnectorProperty, dbConnectorProperty,
				dicomConnectorProperty);
		ConnectorProperty connectorPropertyDicomA = new ConnectorProperty("idDicomA", ConnectorType.DICOM,
				new SearchCriteriaProperty(new HashSet<>()), wadoConnectorProperty, dbConnectorProperty,
				dicomConnectorProperty);
		ConnectorProperty connectorPropertyDicomB = new ConnectorProperty("idDicomB", ConnectorType.DICOM,
				new SearchCriteriaProperty(new HashSet<>()), wadoConnectorProperty, dbConnectorProperty,
				dicomConnectorProperty);

		config.put("idDbA", connectorPropertyDbA);
		config.put("idDbB", connectorPropertyDbb);
		config.put("idDicomA", connectorPropertyDicomA);
		config.put("idDicomB", connectorPropertyDicomB);

		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Create mocked service
		this.connectorQueryService = new ConnectorQueryServiceImpl(this.connectorConfigurationPropertiesMock,
				this.dbConnectorQueryServiceMock, this.dicomConnectorQueryServiceMock);
	}

	@Test
	void when_retrievingConnectors_with_emptyArchivesRequested_should_returnDefaultOrderedConnectorsConfig() {

		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();

		// Call service
		LinkedHashSet<ConnectorProperty> connectorProperties = this.connectorQueryService.retrieveConnectors(archives);

		// Test results
		ArrayList<ConnectorProperty> connectorPropertiesList = new ArrayList<>(connectorProperties);
		assertEquals("idDbA", connectorPropertiesList.get(0).getId());
		assertEquals("idDbB", connectorPropertiesList.get(1).getId());
		assertEquals("idDicomA", connectorPropertiesList.get(2).getId());
		assertEquals("idDicomB", connectorPropertiesList.get(3).getId());
	}

	@Test
	void when_retrievingConnectors_with_existingArchives_should_returnRequestedConnectors() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbB");
		archives.add("idDbA");
		archives.add("idDicomB");

		// Call service
		LinkedHashSet<ConnectorProperty> connectorProperties = this.connectorQueryService.retrieveConnectors(archives);

		// Test results
		ArrayList<ConnectorProperty> connectorPropertiesList = new ArrayList<>(connectorProperties);
		assertEquals("idDbB", connectorPropertiesList.get(0).getId());
		assertEquals("idDbA", connectorPropertiesList.get(1).getId());
		assertEquals("idDicomB", connectorPropertiesList.get(2).getId());
	}

	@Test
	void when_retrievingConnectors_with_notExistingArchives_should_throwTechnicalException() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("notExisting");

		// Call service and test result
		assertThrows(TechnicalException.class, () -> this.connectorQueryService.retrieveConnectors(archives));
	}

	@Test
	void when_fillingManifestFromPatientIds_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.getArchive().add("idDbA");

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), weasisSearchCriteria);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.buildFromPatientIdsDbConnector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromPatientIdsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromPatientIds_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.getArchive().add("idDicomA");

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), weasisSearchCriteria);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromPatientIdsDbConnector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.buildFromPatientIdsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromPatientIds_with_deactivatedPatientIdSearchCriteria_should_notCallConnectorService() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.getArchive().add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, null);
		ConnectorProperty connectorPropertyDicomA = new ConnectorProperty("idDicomA", ConnectorType.DICOM,
				new SearchCriteriaProperty(Set.of(QueryLevelType.PATIENT_ID)), wadoConnectorProperty, null, null);
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Call service
		this.connectorQueryService.buildFromPatientIds(new Manifest(), Set.of("uid"), weasisSearchCriteria);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromPatientIdsDbConnector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromPatientIdsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.buildFromStudyInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.buildFromStudyInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromStudyInstanceUids_with_deactivatedStudyInstanceUidSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, null);
		ConnectorProperty connectorPropertyDicomA = new ConnectorProperty("idDicomA", ConnectorType.DICOM,
				new SearchCriteriaProperty(Set.of(QueryLevelType.STUDY_INSTANCE_UID)), wadoConnectorProperty, null,
				null);
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Call service
		this.connectorQueryService.buildFromStudyInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.buildFromStudyAccessionNumbersDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyAccessionNumbersDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyAccessionNumbersDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.buildFromStudyAccessionNumbersDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromAccessionNumbers_with_deactivatedAccessionNumberSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, null);
		ConnectorProperty connectorPropertyDicomA = new ConnectorProperty("idDicomA", ConnectorType.DICOM,
				new SearchCriteriaProperty(Set.of(QueryLevelType.STUDY_ACCESSION_NUMBER)), wadoConnectorProperty, null,
				null);
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Call service
		this.connectorQueryService.buildFromStudyAccessionNumbers(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyAccessionNumbersDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromStudyAccessionNumbersDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.buildFromSeriesInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromSeriesInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromSeriesInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.buildFromSeriesInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromSeriesInstanceUids_with_deactivatedSeriesInstanceUidSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, null);
		ConnectorProperty connectorPropertyDicomA = new ConnectorProperty("idDicomA", ConnectorType.DICOM,
				new SearchCriteriaProperty(Set.of(QueryLevelType.SERIE_INSTANCE_UID)), wadoConnectorProperty, null,
				null);
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Call service
		this.connectorQueryService.buildFromSeriesInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromSeriesInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromSeriesInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_dbArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDbA");

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.times(1))
			.buildFromSopInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromSopInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_dicomArchive_should_callCorrectConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromSopInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.times(1))
			.buildFromSopInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_fillingManifestFromSopInstanceUids_with_deactivatedSopInstanceUidSearchCriteria_should_notCallConnectorService() {
		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("idDicomA");

		// Mock behaviour
		LinkedHashMap<String, ConnectorProperty> config = new LinkedHashMap<>();
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, null);
		ConnectorProperty connectorPropertyDicomA = new ConnectorProperty("idDicomA", ConnectorType.DICOM,
				new SearchCriteriaProperty(Set.of(QueryLevelType.SOP_INSTANCE_UID)), wadoConnectorProperty, null, null);
		config.put("idDicomA", connectorPropertyDicomA);
		Mockito.when(this.connectorConfigurationPropertiesMock.getConnectors()).thenReturn(config);

		// Call service
		this.connectorQueryService.buildFromSopInstanceUids(new Manifest(), Set.of("uid"), archives);

		// Test results
		Mockito.verify(this.dbConnectorQueryServiceMock, Mockito.never())
			.buildFromSopInstanceUidsDbConnector(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.dicomConnectorQueryServiceMock, Mockito.never())
			.buildFromSopInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
	}

}
