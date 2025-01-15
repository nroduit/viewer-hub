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
import org.springframework.test.util.ReflectionTestUtils;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.DisplayService;
import org.viewer.hub.back.service.ManifestService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DisplayServiceImplTest {

	private final CacheService cacheServiceMock = Mockito.mock(CacheService.class);

	private final ManifestService manifestServiceMock = Mockito.mock(ManifestService.class);

	private DisplayService displayService;

	@BeforeEach
	public void setUp() {
		this.displayService = new DisplayServiceImpl(this.cacheServiceMock, this.manifestServiceMock);
		ReflectionTestUtils.setField(this.displayService, "weasisManagerServerUrl", "http://test.com");
	}

	@Test
	void when_retrievingWeasisLaunchUrl_should_callMethodToBuildKey() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();

		// Call service
		this.displayService.retrieveWeasisLaunchUrl(weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.cacheServiceMock, Mockito.times(1))
			.constructManifestKeyDependingOnSearchParameters(Mockito.any());
	}

	@Test
	void when_retrievingWeasisLaunchUrl_withKeyAlreadyExistingInCache_should_notCallServiceToBuildManifest() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();

		// Mock
		Mockito.when(this.cacheServiceMock.constructManifestKeyDependingOnSearchParameters(weasisSearchCriteria))
			.thenReturn(null);

		// Call service
		this.displayService.retrieveWeasisLaunchUrl(weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.manifestServiceMock, Mockito.never())
			.buildManifest(Mockito.anyString(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_retrievingWeasisLaunchUrl_withKeyNotExistingInCache_should_callServiceToBuildManifest() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();

		// Mock
		Mockito.when(this.cacheServiceMock.constructManifestKeyDependingOnSearchParameters(weasisSearchCriteria))
			.thenReturn("key");

		// Call service
		this.displayService.retrieveWeasisLaunchUrl(weasisSearchCriteria, null);

		// Test results
		Mockito.verify(this.manifestServiceMock, Mockito.times(1))
			.buildManifest(Mockito.anyString(), Mockito.any(), Mockito.any());
	}

	@Test
	void when_retrievingWeasisLaunchUrl_with_noArgumentCommand_should_buildValidLaunchUrl() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setPro(List.of("pro"));
		weasisSearchCriteria.setUser("user");
		weasisSearchCriteria.setHost("host");
		weasisSearchCriteria.setExtCfg("extCfg");

		// Mock
		Mockito.when(this.cacheServiceMock.constructManifestKeyDependingOnSearchParameters(weasisSearchCriteria))
			.thenReturn("key");

		// Call service
		String launchUrl = this.displayService.retrieveWeasisLaunchUrl(weasisSearchCriteria, null);

		// Test results
		assertEquals(
				"weasis://%24dicom%3Aget+-w+%22http%3A%2F%2Ftest.com%2Fmanifest%3Fkey%3Dkey%22+%24weasis%3Aconfig+wcfg%3D%22http%3A%2F%2Ftest.com%2Fweasisconfig%2Fws%2FlaunchConfig%3Fpro%3Dpro%26user%3Duser%26host%3Dhost%26ext-cfg%3DextCfg%22",
				launchUrl);
	}

	@Test
	void when_retrievingWeasisLaunchUrl_with_argumentCommand_should_buildValidLaunchUrl() {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setPro(List.of("pro"));
		weasisSearchCriteria.setUser("user");
		weasisSearchCriteria.setHost("host");
		weasisSearchCriteria.setExtCfg("extCfg");
		weasisSearchCriteria.setArg(List.of("$acquire:patient -s H4s"));

		// Mock
		Mockito.when(this.cacheServiceMock.constructManifestKeyDependingOnSearchParameters(weasisSearchCriteria))
			.thenReturn("key");

		// Call service
		String launchUrl = this.displayService.retrieveWeasisLaunchUrl(weasisSearchCriteria, null);

		// Test results
		assertEquals(
				"weasis://%24acquire%3Apatient+-s+H4s+%24weasis%3Aconfig+wcfg%3D%22http%3A%2F%2Ftest.com%2Fweasisconfig%2Fws%2FlaunchConfig%3Fpro%3Dpro%26user%3Duser%26host%3Dhost%26ext-cfg%3DextCfg%22",
				launchUrl);
	}

}
