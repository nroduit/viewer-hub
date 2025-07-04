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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.ArchiveSearchCriteria;
import org.viewer.hub.back.model.IHESearchCriteria;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.SecurityService;

import java.util.LinkedHashSet;
import java.util.List;

class ManifestServiceImplTest {

	private final CacheService cacheServiceMock = Mockito.mock(CacheService.class);

	private final ConnectorQueryService connectorQueryServiceMock = Mockito.mock(ConnectorQueryService.class);

	@Mock

	private SecurityService securityServiceMock;

	private ManifestServiceImpl manifestService;

	AutoCloseable openMocks;

	@BeforeEach
	public void setUp() {

		openMocks = MockitoAnnotations.openMocks(this);

		this.manifestService = new ManifestServiceImpl(this.cacheServiceMock, this.connectorQueryServiceMock,
				this.securityServiceMock);
	}

	@AfterEach
	void tearDown() throws Exception {
		openMocks.close();
	}

	@Test
	void when_buildingManifestWithoutIHE_with_weasisSearchCriteria_should_putManifestInCache() {

		// Init data
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
			.putManifestIfAbsent(Mockito.anyString(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_sopInstanceUids_should_callCorrespondingMethod() {

		// Init data
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();
		weasisSearchCriteria.setObjectUID(new LinkedHashSet<>(List.of("objectUid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromSopInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_serieInstanceUids_should_callCorrespondingMethod() {

		// Init data
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();
		weasisSearchCriteria.setSeriesUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromSeriesInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_studyAccessionsNumber_should_callCorrespondingMethod() {

		// Init data
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();
		weasisSearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_studyUids_should_callCorrespondingMethod() {

		// Init data
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_patientIds_should_callCorrespondingMethod() {

		// Init data
		ArchiveSearchCriteria weasisSearchCriteria = new ArchiveSearchCriteria();
		weasisSearchCriteria.setPatientID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_weasisIHESearchCriteria_should_putManifestInCache() {

		// Init data
		IHESearchCriteria weasisIHESearchCriteria = new IHESearchCriteria();

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
			.putManifestIfAbsent(Mockito.anyString(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndAccessionNumber_should_callCorrespondingMethod() {

		// Init data
		IHESearchCriteria weasisIHESearchCriteria = new IHESearchCriteria();
		weasisIHESearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndStudyInstanceUids_should_callCorrespondingMethod() {

		// Init data
		IHESearchCriteria weasisIHESearchCriteria = new IHESearchCriteria();
		weasisIHESearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndStudyInstanceUidsDeactivated_should_callCorrespondingMethod() {

		// Init data
		IHESearchCriteria weasisIHESearchCriteria = new IHESearchCriteria();
		weasisIHESearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.never())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypePatientAndPatientIds_should_callCorrespondingMethod() {

		// Init data
		IHESearchCriteria weasisIHESearchCriteria = new IHESearchCriteria();
		weasisIHESearchCriteria.setPatientID("uid");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryServiceMock, Mockito.atLeastOnce())
			.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_retrievingManifest_should_putGetManifestFromCache() {

		// Call service
		this.manifestService.retrieveManifest("testWithoutIHE");

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce()).getManifest(Mockito.anyString());
	}

}
