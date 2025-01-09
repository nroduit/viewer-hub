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
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.weasis.manager.back.entity.PackageVersionEntity;
import org.weasis.manager.back.enums.PreferredType;
import org.weasis.manager.back.service.impl.LaunchPreferenceServiceImpl;
import org.weasis.manager.back.service.impl.PackageServiceImpl;

import static org.hamcrest.Matchers.containsString;

/**
 * Tests for LaunchConfigController
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = LaunchConfigController.class)
@WebMvcTest(controllers = LaunchConfigController.class)
class LaunchConfigControllerIntegrationTests {

	private MockMvc mockMvc;

	// @Autowired
	// private WebTestClient webTestClient;

	@MockBean
	private LaunchPreferenceServiceImpl launchPreferenceService;

	@MockBean
	private PackageServiceImpl packageService;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Test Get Launch Config
	 * <p>
	 * Expected: - mocked launch values have been found for the host in parameter - status
	 * is OK - weasisconfig generated contains property "alternatecdb"
	 * @throws Exception thrown
	 */
	// TODO: to reactivate
	// @Test
	void shouldRetrieveLaunchesByHostUserConfigOrderByTargetsAndFiltered() throws Exception {
		// Mocked data
		MultiValueMap<String, String> propertiesMap = new LinkedMultiValueMap<>();
		propertiesMap.add(PreferredType.EXT_CFG.getCode(), "alternatecdb");
		propertiesMap.add("svr", "http://example.org");
		propertiesMap.add("pro", "weasis.i18n ${svr}/weasis-i18n\n");
		propertiesMap.add("cfgExt", "${cdbExt}/conf/ext-config${extCfg!}.properties");
		// propertiesMap.add("cfg", "${cdb}/conf/config.properties");
		propertiesMap.add("configCacheKey", "1.1.1");
		// propertiesMap.add("cfg",
		// "${svr}/overrideConfig/properties/${configCacheKey!}");
		propertiesMap.add("cfg",
				"${svr}/overrideConfig/properties?packageVersionId=${packageVersionId!}&amp;launchConfigId=${launchConfigId!}&amp;groupId=${groupId!}");
		propertiesMap.add("cdb", "${svr}/weasis${ver!}");
		propertiesMap.add("cdbExt", "${svr}/weasis/package/${packageVersion!}");
		propertiesMap.add("host", "pc-001");

		PackageVersionEntity packageVersionEntityMocked = new PackageVersionEntity();
		packageVersionEntityMocked.setVersionNumber("1.0.0");
		packageVersionEntityMocked.setVersionNumber("-TEST");
		packageVersionEntityMocked.setId(1L);

		// Mock service to return launch value
		Mockito
			.when(this.launchPreferenceService.buildLaunchConfiguration(Mockito.any(), Mockito.eq(null),
					Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
			.thenReturn(propertiesMap);
		Mockito.doCallRealMethod()
			.when(this.launchPreferenceService)
			.freeMarkerModelMapping(Mockito.any(Model.class), Mockito.any(MultiValueMap.class));

		Mockito.when(this.packageService.retrieveAvailablePackageVersionToUse(Mockito.any(), Mockito.any()))
			.thenReturn(packageVersionEntityMocked);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/launchConfig").queryParam("host",
		// "pc-001").build())
		// .exchange().expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo( "<property name=\"felix.extended.config.properties\"
		// value=\"http://example.org/weasis-ext/conf/ext-config-alternatecdb.properties\"
		// />");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/launchConfig").param("host", "pc-001"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string(containsString(
						// "<property name=\"felix.config.properties\"
						// value=\"http://example.org/weasis/package/1.0.0-TEST/conf/ext-config-alternatecdb.properties\"
						// />")));
						"<property name=\"felix.config.properties\" value=\"http://example.org/overrideConfig/properties/1.1.1\" />")));
	}

}
