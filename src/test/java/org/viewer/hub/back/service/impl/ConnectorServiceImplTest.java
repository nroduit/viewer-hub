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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.property.ConnectorDicomWebProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.property.ConnectorWadoProperty;
import org.viewer.hub.back.model.property.DbConnectorProperty;
import org.viewer.hub.back.model.property.DbConnectorQueryProperty;
import org.viewer.hub.back.model.property.DicomConnectorDimseProperty;
import org.viewer.hub.back.model.property.DicomConnectorProperty;
import org.viewer.hub.back.model.property.DicomWebConnectorProperty;
import org.viewer.hub.back.model.property.SearchCriteriaProperty;
import org.viewer.hub.back.model.property.WeasisConnectorProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ConnectorServiceImplTest {

	private final ConnectorConfigurationProperties connectorConfigurationPropertiesMock = Mockito
		.mock(ConnectorConfigurationProperties.class);

	private ConnectorServiceImpl connectorService;

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

		ConnectorProperty connectorPropertyDbA = ConnectorProperty.builder()
			.id("idDbA")
			.type(ConnectorType.DB)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		ConnectorProperty connectorPropertyDbb = ConnectorProperty.builder()
			.id("idDbB")
			.type(ConnectorType.DB)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		ConnectorProperty connectorPropertyDicomA = ConnectorProperty.builder()
			.id("idDicomA")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		ConnectorProperty connectorPropertyDicomB = ConnectorProperty.builder()
			.id("idDicomB")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder().build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		ConnectorProperty connectorPropertyDicomWebA = ConnectorProperty.builder()
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
		this.connectorService = new ConnectorServiceImpl(this.connectorConfigurationPropertiesMock);
	}

	@Test
	void when_retrievingConnectors_with_emptyArchivesRequested_should_returnDefaultOrderedConnectorsConfig() {

		// Init data
		LinkedHashSet<String> archives = new LinkedHashSet<>();

		// Call service
		LinkedHashSet<ConnectorProperty> connectorProperties = this.connectorService.retrieveConnectors(archives);

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
		LinkedHashSet<ConnectorProperty> connectorProperties = this.connectorService.retrieveConnectors(archives);

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
		assertThrows(TechnicalException.class, () -> this.connectorService.retrieveConnectors(archives));
	}

}
