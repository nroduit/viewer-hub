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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.config.s3.S3ClientConfigurationProperties;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.WeasisIHESearchCriteria;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.service.CryptographyService;
import org.viewer.hub.back.service.DisplayService;

import java.util.LinkedHashSet;
import java.util.List;

@WithMockUser
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DisplayController.class)
class DisplayControllerIntegrationTests {

	private MockMvc mockMvc;

	@MockBean
	private DisplayService displayService;

	@MockBean
	private CryptographyService cryptographyService;

	@MockBean
	ConnectorConfigurationProperties connectorConfigurationProperties;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	void when_launchWeasisWithoutIHEParameters_with_validData_and_getRequest_should_beRedirection() throws Exception {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setUser("test");

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");

		// Get and test results
		this.launchWeasisWithoutIHEParametersGetRequestShouldBeRedirection(weasisSearchCriteria, "extCfg");
	}

	@Test
	void when_launchWeasisWithIHEParameters_with_validData_and_getRequest_should_beRedirection() throws Exception {
		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setUser("test");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);
		weasisIHESearchCriteria.setPatientID("patientId");

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");

		// Get and test results
		this.launchWeasisWithIHEParametersGetRequestShouldBeRedirection(weasisIHESearchCriteria, "extCfg");
	}

	@Test
	void when_launchWeasisWithoutIHEParameters_with_invalidData_and_getRequest_should_beBadRequest() throws Exception {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setUser("test");
		weasisSearchCriteria.setArchive(new LinkedHashSet<>(List.of("archiveNotExisting")));

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");
		Mockito.when(this.connectorConfigurationProperties.containsConnectorId(Mockito.any())).thenReturn(false);

		// Get and test results
		this.launchWeasisWithoutIHEParametersGetRequestInvalidDataShouldBeBadRequest(weasisSearchCriteria, "extCfg");
	}

	@Test
	void when_launchWeasisWithIHEParameters_with_invalidData_and_getRequest_should_beBadRequest() throws Exception {
		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setUser("test");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);
		weasisIHESearchCriteria.setPatientID(null);

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");
		Mockito.when(this.connectorConfigurationProperties.containsConnectorId(Mockito.any())).thenReturn(false);

		// Get and test results
		this.launchWeasisWithIHEParametersGetRequestInvalidDataShouldBeBadRequest(weasisIHESearchCriteria, "extCfg");
	}

	@Test
	void when_launchWeasisWithoutIHEParameters_with_validData_and_postRequest_should_beRedirection() throws Exception {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setUser("test");
		weasisSearchCriteria.setExtCfg("extCfg");

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");

		// Post and test results
		this.launchWeasisWithoutIHEParametersPostRequestShouldBeRedirection(weasisSearchCriteria);
	}

	@Test
	void when_launchWeasisWithIHEParameters_with_validData_and_postRequest_should_beRedirection() throws Exception {
		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setUser("test");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);
		weasisIHESearchCriteria.setPatientID("patientId");

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");

		// Post and test results
		this.launchWeasisWithIHEParametersPostRequestShouldBeRedirection(weasisIHESearchCriteria);
	}

	@Test
	void when_launchWeasisWithoutIHEParameters_with_invalidData_and_postRequest_should_beBadRequest() throws Exception {
		// Init data
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setArchive(new LinkedHashSet<>(List.of("archiveNotExisting")));

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");
		Mockito.when(this.connectorConfigurationProperties.containsConnectorId(Mockito.any())).thenReturn(false);

		// Post and test results
		this.launchWeasisWithoutIHEParametersPostRequestInvalidDataShouldBeBadRequest(weasisSearchCriteria);
	}

	@Test
	void when_launchWeasisWithIHEParameters_with_invalidData_and_postRequest_should_beBadRequest() throws Exception {
		// Init data
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setUser("test");
		weasisIHESearchCriteria.setRequestType(IHERequestType.PATIENT);
		weasisIHESearchCriteria.setPatientID(null);

		// Mock service
		Mockito.when(this.displayService.retrieveWeasisLaunchUrl(Mockito.any(), Mockito.any())).thenReturn("launchUrl");

		// Post and test results
		this.launchWeasisWithIHEParametersPostRequestInvalidDataShouldBeBadRequest(weasisIHESearchCriteria);
	}

	private void launchWeasisWithoutIHEParametersPostRequestInvalidDataShouldBeBadRequest(
			WeasisSearchCriteria weasisSearchCriteria) throws Exception {

		// Data Input
		String weasisSearchCriteriaString = null;
		if (weasisSearchCriteria != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			weasisSearchCriteriaString = objectMapper.writeValueAsString(weasisSearchCriteria);
		}

		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.post(EndPoint.DISPLAY_PATH + EndPoint.WEASIS_PATH)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(weasisSearchCriteriaString))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	private void launchWeasisWithIHEParametersPostRequestInvalidDataShouldBeBadRequest(
			WeasisIHESearchCriteria weasisIHESearchCriteria) throws Exception {

		// Data Input
		String weasisSearchCriteriaString = null;
		if (weasisIHESearchCriteria != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			weasisSearchCriteriaString = objectMapper.writeValueAsString(weasisIHESearchCriteria);
		}

		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.post(EndPoint.DISPLAY_PATH + EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(weasisSearchCriteriaString))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	private void launchWeasisWithoutIHEParametersGetRequestInvalidDataShouldBeBadRequest(
			WeasisSearchCriteria weasisSearchCriteria, String extCfg) throws Exception {
		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.get(EndPoint.DISPLAY_PATH + EndPoint.WEASIS_PATH)
				.param(ParamName.USER, weasisSearchCriteria.getUser())
				.param(ParamName.EXT_CFG, extCfg)
				.param(ParamName.ARCHIVE, weasisSearchCriteria.getArchive().stream().findFirst().get()))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	private void launchWeasisWithIHEParametersGetRequestInvalidDataShouldBeBadRequest(
			WeasisIHESearchCriteria weasisIHESearchCriteria, String extCfg) throws Exception {
		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.get(EndPoint.DISPLAY_PATH + EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
				.param(ParamName.USER, weasisIHESearchCriteria.getUser())
				.param(ParamName.EXT_CFG, extCfg)
				.param(ParamName.REQUEST_TYPE, weasisIHESearchCriteria.getRequestType().getCode())
				.param(ParamName.PATIENT_ID, weasisIHESearchCriteria.getPatientID()))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	private void launchWeasisWithoutIHEParametersPostRequestShouldBeRedirection(
			WeasisSearchCriteria weasisSearchCriteria) throws Exception {
		// Data Input
		ObjectMapper objectMapper = new ObjectMapper();
		String weasisSearchCriteriaString = objectMapper.writeValueAsString(weasisSearchCriteria);

		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.post(EndPoint.DISPLAY_PATH + EndPoint.WEASIS_PATH)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(weasisSearchCriteriaString))
			.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
	}

	private void launchWeasisWithIHEParametersPostRequestShouldBeRedirection(
			WeasisIHESearchCriteria weasisIHESearchCriteria) throws Exception {
		// Data Input
		ObjectMapper objectMapper = new ObjectMapper();
		String weasisSearchCriteriaString = objectMapper.writeValueAsString(weasisIHESearchCriteria);

		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.post(EndPoint.DISPLAY_PATH + EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(weasisSearchCriteriaString))
			.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
	}

	private void launchWeasisWithoutIHEParametersGetRequestShouldBeRedirection(
			WeasisSearchCriteria weasisSearchCriteria, String extCfg) throws Exception {
		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.get(EndPoint.DISPLAY_PATH + EndPoint.WEASIS_PATH)
				.param(ParamName.USER, weasisSearchCriteria.getUser())
				.param(ParamName.EXT_CFG, extCfg))
			.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
	}

	private void launchWeasisWithIHEParametersGetRequestShouldBeRedirection(
			WeasisIHESearchCriteria weasisIHESearchCriteria, String extCfg) throws Exception {
		// Call controller and check status
		this.mockMvc
			.perform(MockMvcRequestBuilders.get(EndPoint.DISPLAY_PATH + EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
				.param(ParamName.USER, weasisIHESearchCriteria.getUser())
				.param(ParamName.EXT_CFG, extCfg)
				.param(ParamName.REQUEST_TYPE, weasisIHESearchCriteria.getRequestType().getCode())
				.param(ParamName.PATIENT_ID, weasisIHESearchCriteria.getPatientID()))
			.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
	}

}
