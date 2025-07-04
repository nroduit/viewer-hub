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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.enums.ConnectorAuthType;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.ArchiveSearchCriteria;
import org.viewer.hub.back.model.manifest.DicomPatientSex;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.ConnectorAuthenticationProperty;
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
import org.viewer.hub.back.model.property.WeasisManifestConnectorProperty;
import org.viewer.hub.back.service.DicomConnectorQueryService;
import org.viewer.hub.back.service.DicomWebClientService;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DicomConnectorQueryServiceImplTest {

	private DicomConnectorQueryService dicomConnectorQueryService;

	private ConnectorProperty dicomConnectorProperty;

	private ConnectorProperty dicomWebConnectorProperty;

	private MockedStatic<CFind> cFindMock;

	@Mock
	private ConnectorConfigurationProperties connectorConfigurationProperties;

	@Mock
	private DicomWebClientService dicomWebClientService;

	@Mock
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@Mock
	private ClientRegistrationRepository clientRegistrationRepository;

	@Mock
	private WebClient webClientQidoRs;

	@Mock
	private WebClient webClientWadoRs;

	@Mock
	private WebClient.RequestHeadersUriSpec headerSpec;

	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	@BeforeEach
	public void setUp() {
		// Mock
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
			.wadoRs(ConnectorDicomWebProperty.builder()
				.authentication(ConnectorAuthenticationProperty.builder().type(ConnectorAuthType.BASIC).build())
				.build())
			.qidoRs(ConnectorDicomWebProperty.builder()
				.authentication(ConnectorAuthenticationProperty.builder().type(ConnectorAuthType.BASIC).build())
				.build())
			.webClientQidoRs(webClientQidoRs)
			.webClientWadoRs(webClientWadoRs)
			.build();

		this.dicomConnectorProperty = ConnectorProperty.builder()
			.id("idDicom")
			.type(ConnectorType.DICOM)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder()
				.manifest(WeasisManifestConnectorProperty.builder().build())
				.build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		this.dicomWebConnectorProperty = ConnectorProperty.builder()
			.id("idDicomWeb")
			.type(ConnectorType.DICOM_WEB)
			.searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
			.weasis(WeasisConnectorProperty.builder()
				.manifest(WeasisManifestConnectorProperty.builder().build())
				.build())
			.dbConnector(dbConnectorProperty)
			.dicomConnector(dicomConnectorProperty)
			.dicomWebConnector(dicomWebConnectorProperty)
			.build();

		// Mock CFind
		this.cFindMock = Mockito.mockStatic(CFind.class);
		DicomState dicomState = new DicomState();
		Attributes attributes = new Attributes();
		attributes.setValue(Tag.PatientID, VR.LO, "patientId");
		attributes.setValue(Tag.PatientSex, VR.CS, "O");
		dicomState.getDicomRSP().add(attributes);
		this.cFindMock.when(() -> CFind.process(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(0),
				Mockito.any(), Mockito.any(DicomParam[].class)))
			.thenReturn(dicomState);

		// Mock webClient
		Mockito.lenient().when(webClientQidoRs.get()).thenReturn(headerSpec);
		Mockito.lenient().when(webClientWadoRs.get()).thenReturn(headerSpec);
		Mockito.lenient().when(headerSpec.uri(Mockito.anyString(), Mockito.anyMap())).thenReturn(requestHeadersSpec);
		Mockito.lenient()
			.when(requestHeadersSpec.header(Mockito.anyString(), Mockito.any()))
			.thenReturn(requestHeadersSpec);
		Mockito.lenient().when(headerSpec.uri(Mockito.any(Function.class))).thenReturn(requestHeadersSpec);
		Mockito.lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.lenient().when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);

		// Create mocked service
		this.dicomConnectorQueryService = new DicomConnectorQueryServiceImpl(connectorConfigurationProperties,
				dicomWebClientService, oAuth2AuthorizedClientService, clientRegistrationRepository);

		ReflectionTestUtils.setField(this.dicomConnectorQueryService, "dicomWebTimeoutDuration", "30");
	}

	@AfterEach
	void tearDown() {
		// Close static mock
		if (this.cFindMock != null) {
			this.cFindMock.close();
		}
	}

	@Test
	void when_buildingFromStudyAccessionNumber_withDicomConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyAccessionNumbers = new HashSet<>();
		studyAccessionNumbers.add("studyAccessionNumber");

		// Call service
		this.dicomConnectorQueryService.buildFromStudyAccessionNumbersDicomConnector(manifest, studyAccessionNumbers,
				this.dicomConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromStudyAccessionNumber_withDicomWebConnector_with_validData_should_addCorrectValuesInManifest()
			throws JsonProcessingException {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyAccessionNumbers = new HashSet<>();
		studyAccessionNumbers.add("studyAccessionNumber");

		// Mock behaviour
		Mockito.when(responseSpec.bodyToMono(String.class))
			.thenReturn(Mono.just(
					"[{\"00100020\":{\"vr\":\"LO\",\"Value\":[\"patientId\"]},\"00100040\":{\"vr\":\"CS\",\"Value\":[\"O\"]}}]"));

		// Call service
		this.dicomConnectorQueryService.buildFromStudyAccessionNumbersDicomConnector(manifest, studyAccessionNumbers,
				this.dicomWebConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicomWeb", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM_WEB, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromPatientIds_withDicomConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> patientIds = new HashSet<>();
		patientIds.add("patientId");
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();

		// Call service
		this.dicomConnectorQueryService.buildFromPatientIdsDicomConnector(manifest, patientIds,
				this.dicomConnectorProperty, weasisSearchCriteria, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals(ConnectorType.DICOM, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromPatientIds_withDicomWebConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> patientIds = new HashSet<>();
		patientIds.add("patientId");
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();

		// Mock behaviour
		Mockito.when(responseSpec.bodyToMono(String.class))
			.thenReturn(Mono.just(
					"[{\"00100020\":{\"vr\":\"LO\",\"Value\":[\"patientId\"]},\"00100040\":{\"vr\":\"CS\",\"Value\":[\"O\"]}}]"));

		// Call service
		this.dicomConnectorQueryService.buildFromPatientIdsDicomConnector(manifest, patientIds,
				this.dicomWebConnectorProperty, weasisSearchCriteria, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicomWeb", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM_WEB, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromStudyInstanceUids_withDicomConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyInstanceUids = new HashSet<>();
		studyInstanceUids.add("studyInstanceUid");

		// Call service
		this.dicomConnectorQueryService.buildFromStudyInstanceUidsDicomConnector(manifest, studyInstanceUids,
				this.dicomConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromStudyInstanceUids_withDicomWebConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyInstanceUids = new HashSet<>();
		studyInstanceUids.add("studyInstanceUid");

		// Mock behaviour
		Mockito.when(responseSpec.bodyToMono(String.class))
			.thenReturn(Mono.just(
					"[{\"00100020\":{\"vr\":\"LO\",\"Value\":[\"patientId\"]},\"00100040\":{\"vr\":\"CS\",\"Value\":[\"O\"]}}]"));

		// Call service
		this.dicomConnectorQueryService.buildFromStudyInstanceUidsDicomConnector(manifest, studyInstanceUids,
				this.dicomWebConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicomWeb", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM_WEB, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromSeriesInstanceUids_withDicomConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> seriesInstanceUids = new HashSet<>();
		seriesInstanceUids.add("seriesInstanceUid");

		// Call service
		this.dicomConnectorQueryService.buildFromSeriesInstanceUidsDicomConnector(manifest, seriesInstanceUids,
				this.dicomConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromSeriesInstanceUids_withDicomWebConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> seriesInstanceUids = new HashSet<>();
		seriesInstanceUids.add("seriesInstanceUid");

		// Mock behaviour
		Mockito.when(responseSpec.bodyToMono(String.class))
			.thenReturn(Mono.just(
					"[{\"00100020\":{\"vr\":\"LO\",\"Value\":[\"patientId\"]},\"0020000D\":{\"vr\":\"UI\",\"Value\":[\"studyInstanceUID\"]},\"00100040\":{\"vr\":\"CS\",\"Value\":[\"O\"]}}]"));

		// Call service
		this.dicomConnectorQueryService.buildFromSeriesInstanceUidsDicomConnector(manifest, seriesInstanceUids,
				this.dicomWebConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicomWeb", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM_WEB, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromSopInstanceUids_withDicomConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> sopInstanceUids = new HashSet<>();
		sopInstanceUids.add("sopInstanceUid");

		// Call service
		this.dicomConnectorQueryService.buildFromSopInstanceUidsDicomConnector(manifest, sopInstanceUids,
				this.dicomConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicom", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

	@Test
	void when_buildingFromSopInstanceUids_withDicomWebConnector_with_validData_should_addCorrectValuesInManifest() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> sopInstanceUids = new HashSet<>();
		sopInstanceUids.add("sopInstanceUid");

		// Mock behaviour
		Mockito.when(responseSpec.bodyToMono(String.class))
			.thenReturn(Mono.just(
					"[{\"00100020\":{\"vr\":\"LO\",\"Value\":[\"patientId\"]},\"0020000D\":{\"vr\":\"UI\",\"Value\":[\"studyInstanceUID\"]},\"0020000E\":{\"vr\":\"UI\",\"Value\":[\"serieInstanceUID\"]},\"00100040\":{\"vr\":\"CS\",\"Value\":[\"O\"]}}]"));

		// Call service
		this.dicomConnectorQueryService.buildFromSopInstanceUidsDicomConnector(manifest, sopInstanceUids,
				this.dicomWebConnectorProperty, null);

		// Test results
		assertEquals("patientId", new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientID());
		assertEquals("idDicomWeb", manifest.getArcQueries().get(0).getArcId());
		assertEquals(ConnectorType.DICOM_WEB, manifest.getArcQueries().get(0).getQueryMode());
		assertEquals(DicomPatientSex.O,
				new ArrayList<>(manifest.getArcQueries().get(0).getPatients()).get(0).getPatientSex());
	}

}
