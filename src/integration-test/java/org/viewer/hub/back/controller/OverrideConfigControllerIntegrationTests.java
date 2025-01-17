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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.viewer.hub.back.config.s3.S3ClientConfigurationProperties;
import org.viewer.hub.back.service.OverrideConfigService;

@WithMockUser
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = OverrideConfigController.class)
class OverrideConfigControllerIntegrationTests {

	private MockMvc mockMvc;

	@MockBean
	private OverrideConfigService overrideConfigService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	// TODO W-34: to set back and modify
	// @Test
	// void given_validParams_when_retrievingConfigurationProperties_then_shouldBeOk()
	// throws Exception {
	// // Init data
	// OverrideConfigEntity overrideConfig = new OverrideConfigEntity();
	// overrideConfig.setWeasisName("weasisName");
	//
	// // Mock service
	// Mockito.when(overrideConfigService.retrieveProperties(anyLong(), anyLong(),
	// anyLong()))
	// .thenReturn(overrideConfig);
	//
	// // Get and test results
	// this.mockMvc
	// .perform(MockMvcRequestBuilders.get(EndPoint.OVERRIDE_CONFIG_PATH + "/properties")
	// .contentType(MediaType.TEXT_PLAIN_VALUE)
	// .param("packageVersionId", "1")
	// .param("launchConfigId", "1")
	// .param("groupId", "1"))
	// .andExpect(MockMvcResultMatchers.status().isOk())
	// .andExpect(MockMvcResultMatchers.content().string(containsString("weasis.name=weasisName")));
	// }

}
