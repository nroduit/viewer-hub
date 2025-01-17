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

package org.viewer.hub.back.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.viewer.hub.back.config.s3.S3ClientConfigurationProperties;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.service.ManifestService;

@WithMockUser
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ManifestController.class)
class ManifestControllerIntegrationTests {

	private MockMvc mockMvc;

	@MockBean
	private ManifestService manifestService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	void when_retrieveXmlManifest_with_validData_should_beOk() throws Exception {
		// Get and test results
		this.retrieveXmlManifestShouldBeOk("key");
	}

	@Test
	void when_retrieveXmlManifest_with_key_blank_should_beBadRequest() throws Exception {
		// Get and test results
		this.retrieveXmlManifestShouldBeBadRequest(" ");
	}

	@Test
	void when_retrieveXmlManifest_with_key_null_should_beBadRequest() throws Exception {
		// Get and test results
		this.retrieveXmlManifestShouldBeBadRequest(null);
	}

	private void retrieveXmlManifestShouldBeBadRequest(String key) throws Exception {
		// Mock service
		Mockito.when(this.manifestService.retrieveManifest(Mockito.anyString())).thenReturn(new Manifest());
		// Call controller and check status
		this.mockMvc.perform(MockMvcRequestBuilders.get(EndPoint.MANIFEST_PATH).param(ParamName.KEY, key))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	private void retrieveXmlManifestShouldBeOk(String key) throws Exception {
		// Mock service
		Mockito.when(this.manifestService.retrieveManifest(Mockito.anyString())).thenReturn(new Manifest());
		// Call controller and check status
		this.mockMvc.perform(MockMvcRequestBuilders.get(EndPoint.MANIFEST_PATH).param(ParamName.KEY, key))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
