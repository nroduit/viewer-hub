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
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.model.manifest.DicomPatientSex;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.AuthenticationProperty;
import org.viewer.hub.back.model.property.BasicAuthenticationProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.property.DbConnectorProperty;
import org.viewer.hub.back.model.property.DbConnectorQueryProperty;
import org.viewer.hub.back.model.property.DicomConnectorProperty;
import org.viewer.hub.back.model.property.OAuth2AuthenticationProperty;
import org.viewer.hub.back.model.property.SearchCriteriaProperty;
import org.viewer.hub.back.model.property.WadoConnectorProperty;
import org.viewer.hub.back.service.DicomConnectorQueryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DicomConnectorQueryServiceImplTest {

	private DicomConnectorQueryService dicomConnectorQueryService;

	private ConnectorProperty connectorProperty;

	private MockedStatic<CFind> cFindMock;

	@BeforeEach
	public void setUp() {
		// Mock
		DbConnectorQueryProperty dbConnectorQueryProperty = new DbConnectorQueryProperty("select",
				"accessionNumberColumn", "patientIdColumn", "studyInstanceUidColumn", "serieInstanceUidColumn",
				"sopInstanceUidColumn");
		DbConnectorProperty dbConnectorProperty = new DbConnectorProperty("user", "password", "uri", "driver",
				dbConnectorQueryProperty);
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, new HashMap<>());
		DicomConnectorProperty dicomConnectorProperty = new DicomConnectorProperty("callingAet", "aet", "host", 1, null,
				null, null);
		this.connectorProperty = new ConnectorProperty("idDicom", ConnectorType.DICOM,
				new SearchCriteriaProperty(new HashSet<>()), wadoConnectorProperty, dbConnectorProperty,
				dicomConnectorProperty);
		this.cFindMock = Mockito.mockStatic(CFind.class);
		DicomState dicomState = new DicomState();
		Attributes attributes = new Attributes();
		attributes.setValue(Tag.PatientID, VR.LO, "patientId");
		attributes.setValue(Tag.PatientSex, VR.CS, "O");
		dicomState.getDicomRSP().add(attributes);
		this.cFindMock.when(() -> CFind.process(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(0),
				Mockito.any(), Mockito.any(DicomParam[].class)))
			.thenReturn(dicomState);

		// Create mocked service
		this.dicomConnectorQueryService = new DicomConnectorQueryServiceImpl();
	}

	@AfterEach
	void tearDown() {
		// Close static mock
		if (this.cFindMock != null) {
			this.cFindMock.close();
		}
	}

	@Test
	void when_buildingFromStudyAccessionNumbers_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyAccessionNumbers = new HashSet<>();
		studyAccessionNumbers.add("studyAccessionNumber");

		// Call service
		this.dicomConnectorQueryService.buildFromStudyAccessionNumbersDicomConnector(manifest, studyAccessionNumbers,
				this.connectorProperty);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromPatientIds_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> patientIds = new HashSet<>();
		patientIds.add("patientId");
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();

		// Call service
		this.dicomConnectorQueryService.buildFromPatientIdsDicomConnector(manifest, patientIds, this.connectorProperty,
				weasisSearchCriteria);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromStudyInstanceUids_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyInstanceUids = new HashSet<>();
		studyInstanceUids.add("studyInstanceUid");

		// Call service
		this.dicomConnectorQueryService.buildFromStudyInstanceUidsDicomConnector(manifest, studyInstanceUids,
				this.connectorProperty);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromSeriesInstanceUids_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> seriesInstanceUids = new HashSet<>();
		seriesInstanceUids.add("seriesInstanceUid");

		// Call service
		this.dicomConnectorQueryService.buildFromSeriesInstanceUidsDicomConnector(manifest, seriesInstanceUids,
				this.connectorProperty);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromSopInstanceUids_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> sopInstanceUids = new HashSet<>();
		sopInstanceUids.add("sopInstanceUid");

		// Call service
		this.dicomConnectorQueryService.buildFromSopInstanceUidsDicomConnector(manifest, sopInstanceUids,
				this.connectorProperty);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

}
