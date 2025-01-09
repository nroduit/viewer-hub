/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.controller;

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
import org.weasis.manager.back.config.s3.S3ClientConfigurationProperties;
import org.weasis.manager.back.model.WeasisModule;
import org.weasis.manager.back.service.ModuleService;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;

/**
 * Tests for ModuleController
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = ModuleController.class)
@WebMvcTest(controllers = ModuleController.class)
class ModuleControllerIntegrationTests {

	private MockMvc mockMvc;

	// @Autowired
	// private WebTestClient webTestClient;

	@MockBean
	private ModuleService moduleService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Test getWeasisModules
	 * <p>
	 * Expected: - mocked WeasisModule has been found for the user/profile in parameter
	 * and has been set in the body - status is OK
	 * @throws Exception thrown
	 */
	@Test
	void shouldGetWeasisModule() throws Exception {
		// Init data
		WeasisModule weasisModule = new WeasisModule();
		weasisModule.setId(1L);
		weasisModule.setName("nameModule");

		// Mock service to return launch value
		Mockito.when(this.moduleService.readWeasisModules(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(Collections.singletonList(weasisModule));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/weasismodules")
		// .queryParam("user", "user")
		// .queryParam("profile", "default")
		// .build())
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .value(Matchers.containsString("<weasisModules><weasisModule><id>1</id><name>nameModule</name></weasisModule></weasisModules>"));

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/weasismodules")
				.param("user", "user")
				.param("profile", "default"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string(containsString(
						"<?xml version='1.0' encoding='UTF-8'?><weasisModules><weasisModule><id>1</id><name>nameModule</name></weasisModule></weasisModules>")));
	}

	/**
	 * Test getWeasisModules
	 * <p>
	 * Expected: - status is BadRequest
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotGetWeasisModuleBecauseNoUserNorProfile() throws Exception {
		// Init data
		WeasisModule weasisModule = new WeasisModule();
		weasisModule.setId(1L);
		weasisModule.setName("nameModule");

		// Mock service to return launch value
		Mockito.when(this.moduleService.readWeasisModules(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(Collections.singletonList(weasisModule));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/weasismodules")
		// .queryParam("user", "")
		// .queryParam("profile", "")
		// .build())
		// .exchange()
		// .expectStatus().isBadRequest();
		this.mockMvc
			.perform(
					MockMvcRequestBuilders.get("/weasisconfig/ws/weasismodules").param("user", "").param("profile", ""))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

}
