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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.viewer.hub.back.config.s3.S3ClientConfigurationProperties;
import org.viewer.hub.back.enums.OperationType;
import org.viewer.hub.back.service.ApplicationPreferenceService;

/**
 * Tests for PreferenceController
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = PreferenceController.class)
@WebMvcTest(controllers = PreferenceController.class)
class PreferenceControllerIntegrationTests {

	private MockMvc mockMvc;

	// @Autowired
	// private WebTestClient webTestClient;

	@MockBean
	private ApplicationPreferenceService applicationPreferenceService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Test getWeasisPreferences for user/profile
	 * <p>
	 * Expected: - mocked preference has been found for the user/profile in parameter and
	 * has been set in the body - status is OK
	 * @throws Exception thrown
	 */
	@Test
	void shouldGetPreferencesForUserProfile() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.readWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.any(), Mockito.eq(false)))
			.thenReturn("weasis");

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "user")
		// .queryParam("profile", "default")
		// .build())
		// .header("Accept", "text/x-java-properties")
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("weasis");
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences")
				.param("user", "user")
				.param("profile", "default")
				.accept("text/x-java-properties"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("weasis"));
	}

	/**
	 * Test getWeasisPreferences for user/profile with empty user/profile
	 * <p>
	 * Expected: - status is BadRequest
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotGetPreferencesForUserProfile() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.readWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.any(), Mockito.eq(false)))
			.thenReturn("weasis");

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "")
		// .queryParam("profile", "")
		// .build())
		// .header("Accept", "text/x-java-properties")
		// .exchange()
		// .expectStatus().isBadRequest();
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences")
				.param("user", "")
				.param("profile", "")
				.accept("text/x-java-properties"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test getWeasisPreferences for user/profile/module
	 * <p>
	 * Expected: - mocked preference has been found for the user/profile/module in
	 * parameter and has been set in the body - status is OK
	 * @throws Exception thrown
	 */
	@Test
	void shouldGetPreferencesForUserProfileModule() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.readWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.anyString(), Mockito.eq(true)))
			.thenReturn("weasis");

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "user")
		// .queryParam("profile", "default")
		// .queryParam("module", "weasis")
		// .build())
		// .accept(MediaType.APPLICATION_XML)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("weasis");
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences")
				.param("user", "user")
				.param("profile", "default")
				.param("module", "weasis")
				.accept(MediaType.TEXT_PLAIN_VALUE))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("weasis"));
	}

	/**
	 * Test getWeasisPreferences for user/profile/module with empty user/profile/module
	 * <p>
	 * Expected: - status is BadRequest
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotGetPreferencesForUserProfileModule() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.readWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.anyString(), Mockito.eq(true)))
			.thenReturn("weasis");

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "")
		// .queryParam("profile", "")
		// .queryParam("module", "")
		// .build())
		// .accept(MediaType.APPLICATION_XML)
		// .exchange()
		// .expectStatus().isBadRequest();
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences")
				.param("user", "")
				.param("profile", "")
				.param("module", "")
				.accept(MediaType.TEXT_PLAIN_VALUE))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test updateWeasisPreferences for user/profile
	 * <p>
	 * Expected: - mocked result of update for the user/profile in parameter and has been
	 * set in the body - status is Created
	 * @throws Exception thrown
	 */
	@Test
	void shouldPostPreferencesForUserProfile() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.updateWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.any(), Mockito.anyString()))
			.thenReturn(OperationType.CREATION);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "user")
		// .queryParam("profile", "default")
		// .build())
		// .bodyValue("test")
		// .header(HttpHeaders.CONTENT_TYPE, "text/x-java-properties")
		// .exchange()
		// .expectStatus().isCreated();
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences")
				.param("user", "user")
				.param("profile", "default")
				.contentType("text/x-java-properties")
				.content("test"))
			.andExpect(MockMvcResultMatchers.status().isCreated());
	}

	/**
	 * Test updateWeasisPreferences for user/profile with user and profile empty
	 * <p>
	 * Expected: - status is BadRequest
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotPostPreferencesForUserProfile() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.updateWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.any(), Mockito.anyString()))
			.thenReturn(OperationType.CREATION);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "")
		// .queryParam("profile", "")
		// .build())
		// .bodyValue("test")
		// .header(HttpHeaders.CONTENT_TYPE, "text/x-java-properties")
		// .exchange()
		// .expectStatus().isBadRequest();
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences")
				.param("user", "")
				.param("profile", "")
				.contentType("text/x-java-properties"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test updateWeasisPreferences for user/profile/module
	 * <p>
	 * Expected: - mocked result of update for the user/profile/module in parameter and
	 * has been set in the body - status is Created
	 * @throws Exception thrown
	 */
	@Test
	void shouldPostPreferencesForUserProfileModule() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.updateWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.any(), Mockito.anyString()))
			.thenReturn(OperationType.CREATION);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "user")
		// .queryParam("profile", "default")
		// .queryParam("module", "weasis")
		// .build())
		// .contentType(MediaType.APPLICATION_XML)
		// .bodyValue("test")
		// .exchange()
		// .expectStatus().isCreated();
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences")
				.param("user", "user")
				.param("profile", "default")
				.param("module", "weasis")
				.contentType(MediaType.TEXT_PLAIN_VALUE)
				.content("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><preferences></preferences>"))
			.andExpect(MockMvcResultMatchers.status().isCreated());
	}

	/**
	 * Test updateWeasisPreferences for user/profile/module with params empty
	 * <p>
	 * Expected: - status is BadRequest
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotPostPreferencesForUserProfileModule() throws Exception {
		// Mock service
		Mockito
			.when(this.applicationPreferenceService.updateWeasisPreferences(Mockito.anyString(), Mockito.anyString(),
					Mockito.any(), Mockito.anyString()))
			.thenReturn(OperationType.CREATION);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences")
		// .queryParam("user", "")
		// .queryParam("profile", "")
		// .queryParam("module", "")
		// .build())
		// .contentType(MediaType.APPLICATION_XML)
		// .bodyValue("test")
		// .exchange()
		// .expectStatus().isBadRequest();
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences")
				.param("user", "")
				.param("profile", "")
				.param("module", "")
				.contentType(MediaType.TEXT_PLAIN_VALUE))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

}
