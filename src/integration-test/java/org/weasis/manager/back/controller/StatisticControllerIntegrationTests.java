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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.weasis.manager.back.config.s3.S3ClientConfigurationProperties;
import org.weasis.manager.back.model.PerformanceModel;

/**
 * Tests for TargetController
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = StatisticController.class)
@WebMvcTest(controllers = StatisticController.class)
class StatisticControllerIntegrationTests {

	private MockMvc mockMvc;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	void when_perfModel_with_validData_should_beOk() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "modality", 1,
				2L, 3L, "rate", 4);
		// Post and test results
		this.perfModelShouldBeOk(performanceModel);
	}

	@Test
	void when_perfModel_with_userIdMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("", "host", "type", "seriesUID", "modality", 1, 2L, 3L,
				"rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_hostMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "", "type", "seriesUID", "modality", 1, 2L,
				3L, "rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_typeMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "", "seriesUID", "modality", 1, 2L,
				3L, "rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_seriesUIDMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "", "modality", 1, 2L, 3L,
				"rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_modalityMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "", 1, 2L, 3L,
				"rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_nbImagesMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "modality",
				null, 2L, 3L, "rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_sizeMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "modality", 1,
				null, 3L, "rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_timeMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "modality", 1,
				2L, null, "rate", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_rateMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "modality", 1,
				2L, 3L, "", 4);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	@Test
	void when_perfModel_with_errorsMissing_should_beBadRequest() throws Exception {
		// PerformanceModel to send
		PerformanceModel performanceModel = new PerformanceModel("userId", "host", "type", "seriesUID", "modality", 1,
				2L, 3L, "", null);
		// Post and test results
		this.perfModelShouldBeBadRequest(performanceModel);
	}

	/**
	 * Depending on input should be bad request
	 * @param performanceModel PerformanceModel to send
	 * @throws Exception thrown
	 */
	private void perfModelShouldBeBadRequest(PerformanceModel performanceModel) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String perfModelString = objectMapper.writeValueAsString(performanceModel);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post().uri(uriBuilder ->
		// uriBuilder.path("/ws/launchConfig/perf").build())
		// .bodyValue(perfModelString).header(HttpHeaders.CONTENT_TYPE,
		// MediaType.APPLICATION_JSON_VALUE)
		// .exchange().expectStatus().isBadRequest();
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/statistic/perf")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(perfModelString))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Depending on input should be ok
	 * @param performanceModel PerformanceModel to send
	 * @throws Exception thrown
	 */
	private void perfModelShouldBeOk(PerformanceModel performanceModel) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String perfModelString = objectMapper.writeValueAsString(performanceModel);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post().uri(uriBuilder ->
		// uriBuilder.path("/ws/launchConfig/perf").build())
		// .bodyValue(perfModelString).header(HttpHeaders.CONTENT_TYPE,
		// MediaType.APPLICATION_JSON_VALUE)
		// .exchange().expectStatus().isOk();
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/statistic/perf")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(perfModelString))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
