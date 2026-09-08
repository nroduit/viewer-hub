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

package org.viewer.hub.back.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.viewer.hub.back.constant.ApiVersion;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.searchcriteria.WeasisArchiveSearchCriteria;
import org.viewer.hub.back.service.WeasisService;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class evaluating {@link ManifestController} and, in particular, the fact that the
 * manifest can now be retrieved as JSON (newly added format) in addition to XML
 * (historical format) depending on the "Accept" header sent by the client.
 */
@ExtendWith(MockitoExtension.class)
class ManifestControllerTest {

	private static final String KEY = "key";

	@Mock
	private WeasisService weasisService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		ManifestController manifestController = new ManifestController(this.weasisService);
		// Reuse the same Jackson XML/JSON message converters configuration as the
		// production WebConfiguration so the test reflects the real (de)serialization
		// behaviour (in particular the newly added JSON support).
		this.mockMvc = MockMvcBuilders.standaloneSetup(manifestController)
			.setMessageConverters(new StringHttpMessageConverter(), this.jacksonXmlHttpMessageConverter(),
					this.jacksonJsonHttpMessageConverter())
			.build();
	}

	private JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
		JsonMapper mapper = JsonMapper.builder()
			.findAndAddModules()
			.disable(EnumFeature.READ_ENUMS_USING_TO_STRING, EnumFeature.WRITE_ENUMS_USING_TO_STRING)
			.build();
		return new JacksonJsonHttpMessageConverter(mapper);
	}

	private JacksonXmlHttpMessageConverter jacksonXmlHttpMessageConverter() {
		XmlMapper mapper = XmlMapper.builder()
			.configureForJackson2()
			.enable(XmlWriteFeature.WRITE_XML_DECLARATION)
			.build();
		return new JacksonXmlHttpMessageConverter(mapper);
	}

	private Manifest buildManifest() {
		WeasisArchiveSearchCriteria searchCriteria = new WeasisArchiveSearchCriteria();
		searchCriteria.setHost("pc-1234");
		searchCriteria.setUser("user");
		searchCriteria.setClient("client");
		searchCriteria.setConfig("config");

		Manifest manifest = new Manifest(searchCriteria);
		manifest.setUid("uid");
		manifest.setBuildInProgress(false);
		manifest.setStartManifestRequest(LocalDateTime.now());
		return manifest;
	}

	@Test
	void givenAcceptJson_whenRetrieveManifest_thenShouldReturnManifestAsJson() throws Exception {
		when(this.weasisService.retrieveManifest(anyString())).thenReturn(this.buildManifest());

		this.mockMvc.perform(get("/manifest").param("key", KEY).accept(ApiVersion.V1_APPLICATION_JSON_VALUE))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(ApiVersion.V1_APPLICATION_JSON_VALUE)))
			.andExpect(content().string(Matchers.containsString("\"uid\":\"uid\"")));
	}

	@Test
	void givenAcceptXml_whenRetrieveManifest_thenShouldReturnManifestAsXml() throws Exception {
		when(this.weasisService.retrieveManifest(anyString())).thenReturn(this.buildManifest());

		this.mockMvc.perform(get("/manifest").param("key", KEY).accept(ApiVersion.V1_APPLICATION_XML_VALUE))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(ApiVersion.V1_APPLICATION_XML_VALUE)))
			.andExpect(content().string(Matchers.containsString("<manifest")));
	}

	@Test
	void givenUnsupportedAcceptHeader_whenRetrieveManifest_thenShouldReturnNotAcceptable() throws Exception {
		// Content negotiation is rejected before the controller/service is invoked
		this.mockMvc.perform(get("/manifest").param("key", KEY).accept(MediaType.IMAGE_PNG))
			.andExpect(status().isNotAcceptable());
	}

}
