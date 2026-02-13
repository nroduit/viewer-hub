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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.WeasisArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisIHESearchCriteria;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.SecurityService;
import org.viewer.hub.back.service.WeasisConnectorQueryService;

import java.util.*;

class WeasisServiceImplTest {

	private final CacheService cacheServiceMock = Mockito.mock(CacheService.class);

	private final WeasisConnectorQueryService connectorQueryServiceMock = Mockito
		.mock(WeasisConnectorQueryService.class);

	@Mock
	private ConnectorService connectorServiceMock;

	@Mock
	private SecurityService securityServiceMock;

	private WeasisServiceImpl weasisService;

	AutoCloseable openMocks;

	@BeforeEach
	public void setUp() {

		openMocks = MockitoAnnotations.openMocks(this);

		this.weasisService = new WeasisServiceImpl(this.cacheServiceMock, this.connectorQueryServiceMock,
				this.securityServiceMock, this.connectorServiceMock);
	}

	@AfterEach
	void tearDown() throws Exception {
		openMocks.close();
	}

	@Test
	void when_buildingManifestWithoutIHE_with_weasisSearchCriteria_should_putManifestInCache() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();

		// Call service
		this.weasisService.buildManifest("testWithoutIHE", weasisSearchCriteria, null,null);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
			.putManifestIfAbsent(Mockito.anyString(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_sopInstanceUids_should_callCorrespondingMethod() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setObjectUID(new LinkedHashSet<>(List.of("objectUid")));

		// Call service
		this.weasisService.buildManifest("testWithoutIHE", weasisSearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromSopInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_serieInstanceUids_should_callCorrespondingMethod() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setSeriesUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.weasisService.buildManifest("testWithoutIHE", weasisSearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromSeriesInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_studyAccessionsNumber_should_callCorrespondingMethod() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.weasisService.buildManifest("testWithoutIHE", weasisSearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_studyUids_should_callCorrespondingMethod() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.weasisService.buildManifest("testWithoutIHE", weasisSearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_patientIds_should_callCorrespondingMethod() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setPatientID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.weasisService.buildManifest("testWithoutIHE", weasisSearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_weasisIHESearchCriteria_should_putManifestInCache() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();

		// Call service
		this.weasisService.buildManifest("testWithIHE", weasisIHESearchCriteria, null,null);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
			.putManifestIfAbsent(Mockito.anyString(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndAccessionNumber_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.weasisService.buildManifest("testWithIHE", weasisIHESearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndStudyInstanceUids_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.weasisService.buildManifest("testWithIHE", weasisIHESearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndStudyInstanceUidsDeactivated_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.weasisService.buildManifest("testWithIHE", weasisIHESearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypePatientAndPatientIds_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setPatientID("uid");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);

		// Call service
		this.weasisService.buildManifest("testWithIHE", weasisIHESearchCriteria, null,null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_retrievingManifest_should_putGetManifestFromCache() {

		// Call service
		this.weasisService.retrieveManifest("testWithoutIHE");

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce()).getManifest(Mockito.anyString());
	}

	// ========== Tests with Authentication ==========

	@Test
	void when_buildingManifest_with_authentication_should_putManifestInCacheWithAuthentication() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));
		Authentication authentication = Mockito.mock(Authentication.class);

		// Call service
		this.weasisService.buildManifest("testWithAuth", weasisSearchCriteria, null, authentication);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
				.putManifestIfAbsent(Mockito.anyString(), Mockito.any());
		Mockito.verify(this.securityServiceMock, Mockito.atLeastOnce())
				.handleManifestAuthentication(Mockito.any(), Mockito.eq(authentication));
	}

	@Test
	void when_buildingManifestWithIHE_with_authentication_should_handleAuthentication() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setPatientID("P001");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);
		Authentication authentication = Mockito.mock(Authentication.class);

		// Call service
		this.weasisService.buildManifest("testWithAuthIHE", weasisIHESearchCriteria, null, authentication);

		// Test results
		Mockito.verify(this.securityServiceMock, Mockito.atLeastOnce())
				.handleManifestAuthentication(Mockito.any(), Mockito.eq(authentication));
	}

	// ========== Tests with empty patientsByArchive ==========

	@Test
	void when_buildingManifest_with_emptyPatientsByArchive_should_notCallConnectorService() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("dcm4chee", new HashSet<>());

		// Call service
		this.weasisService.buildManifest("testWithEmptyPatients", weasisSearchCriteria, patientsByArchive, null);

		// Test results
		Mockito.verify(this.connectorServiceMock, Mockito.never())
				.retrieveConnectorFromId(Mockito.anyString());
	}

	@Test
	void when_buildingManifest_with_nullPatientsInMap_should_notCallConnectorService() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("dcm4chee", null);

		// Call service
		this.weasisService.buildManifest("testWithNullPatients", weasisSearchCriteria, patientsByArchive, null);

		// Test results
		Mockito.verify(this.connectorServiceMock, Mockito.never())
				.retrieveConnectorFromId(Mockito.anyString());
	}

	// ========== Tests with empty criteria ==========

	@Test
	void when_buildingManifestWithoutIHE_with_emptyCriteria_should_notCallConnectorQueryService() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		// All criteria are empty

		// Call service
		this.weasisService.buildManifest("testWithEmptyCriteria", weasisSearchCriteria, null, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromSopInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromSeriesInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_emptyAccessionNumberAndStudyUID_should_notCallConnectorQueryService() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);
		// Both accessionNumber and studyUID are empty

		// Call service
		this.weasisService.buildManifest("testWithEmptyIHEStudy", weasisIHESearchCriteria, null, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
				.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	// ========== Tests with multiple criteria ==========

	@Test
	void when_buildingManifestWithoutIHE_with_multipleCriteria_should_callMultipleMethods() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("studyUid")));
		weasisSearchCriteria.setSeriesUID(new LinkedHashSet<>(List.of("seriesUid")));

		// Call service
		this.weasisService.buildManifest("testWithMultipleCriteria", weasisSearchCriteria, null, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromSeriesInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_allCriteria_should_callAllMethods() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setPatientID(new LinkedHashSet<>(List.of("patientId")));
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("studyUid")));
		weasisSearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("accessionNumber")));
		weasisSearchCriteria.setSeriesUID(new LinkedHashSet<>(List.of("seriesUid")));
		weasisSearchCriteria.setObjectUID(new LinkedHashSet<>(List.of("objectUid")));

		// Call service
		this.weasisService.buildManifest("testWithAllCriteria", weasisSearchCriteria, null, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromSeriesInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
				.buildFromSopInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	// ========== Tests for finalization ==========

	@Test
	void when_buildingManifest_should_callPutManifestAtEnd() {

		// Init data
		WeasisArchiveSearchCriteria weasisSearchCriteria = new WeasisArchiveSearchCriteria();
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.weasisService.buildManifest("testFinalization", weasisSearchCriteria, null, null);

		// Test results - should call putManifest (not just putManifestIfAbsent) at the end
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
				.putManifest(Mockito.anyString(), Mockito.any());
	}

}
