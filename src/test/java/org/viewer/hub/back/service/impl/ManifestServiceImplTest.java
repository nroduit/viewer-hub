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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.WeasisIHESearchCriteria;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ManifestService;

import java.util.LinkedHashSet;
import java.util.List;

class ManifestServiceImplTest {

	private final CacheService cacheServiceMock = Mockito.mock(CacheService.class);

	private final ConnectorQueryService connectorQueryService = Mockito.mock(ConnectorQueryService.class);

	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService = Mockito
		.mock(OAuth2AuthorizedClientService.class);

	private ManifestService manifestService;

	@BeforeEach
	public void setUp() {
		this.manifestService = new ManifestServiceImpl(this.cacheServiceMock, this.connectorQueryService,
				this.oAuth2AuthorizedClientService);
	}

	@Test
	void when_buildingManifestWithoutIHE_with_weasisSearchCriteria_should_putManifestInCache() {

		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce())
			.putManifestIfAbsent(Mockito.anyString(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_sopInstanceUids_should_callCorrespondingMethod() {

		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setObjectUID(new LinkedHashSet<>(List.of("objectUid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromSopInstanceUids(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_serieInstanceUids_should_callCorrespondingMethod() {

		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setSeriesUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromSeriesInstanceUids(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_studyAccessionsNumber_should_callCorrespondingMethod() {

		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_studyUids_should_callCorrespondingMethod() {

		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithoutIHE_with_patientIds_should_callCorrespondingMethod() {

		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setPatientID(new LinkedHashSet<>(List.of("uid")));

		// Call service
		this.manifestService.buildManifest("testWithoutIHE", weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_weasisIHESearchCriteria_should_putManifestInCache() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

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
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromStudyAccessionNumbers(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndStudyInstanceUids_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setStudyUID(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypeStudyAndStudyInstanceUidsDeactivated_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setAccessionNumber(new LinkedHashSet<>(List.of("uid")));
		weasisIHESearchCriteria.setRequestType(IHERequestType.STUDY);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.never())
			.buildFromStudyInstanceUids(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_buildingManifestWithIHE_with_iheRequestTypePatientAndPatientIds_should_callCorrespondingMethod() {

		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setPatientID("uid");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);

		// Call service
		this.manifestService.buildManifest("testWithIHE", weasisIHESearchCriteria, null);

		// Test results
		Mockito.verify(this.connectorQueryService, Mockito.atLeastOnce())
			.buildFromPatientIds(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_retrievingManifest_should_putGetManifestFromCache() {

		// Call service
		this.manifestService.retrieveManifest("testWithoutIHE");

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.atLeastOnce()).getManifest(Mockito.anyString());
	}

}
