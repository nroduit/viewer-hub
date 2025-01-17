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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.viewer.hub.back.config.s3.S3ClientConfigurationProperties;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.LaunchPreferredEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.PreferredType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.GroupRepositoryTest;
import org.viewer.hub.back.repository.LaunchRepositoryTest;
import org.viewer.hub.back.service.LaunchPreferenceService;
import org.viewer.hub.back.service.TargetService;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * Tests for LaunchPreferenceController
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = LaunchPreferenceController.class)
@WebMvcTest(controllers = LaunchPreferenceController.class)
class LaunchPreferenceControllerIntegrationTests {

	private MockMvc mockMvc;

	// @Autowired
	// private WebTestClient webTestClient;

	@MockBean
	private LaunchPreferenceService launchPreferenceService;

	@MockBean
	private TargetService targetService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Test Case no launch preference retrieved
	 * <p>
	 * Expected: - mocked launch preference is null and so no launch preference has been
	 * found for the user in parameter - status: no content found
	 * @throws Exception thrown
	 */
	@Test
	void shouldReturnNoContentFoundBecauseNoLaunchPreference() throws Exception {
		// Mock service to return launch value
		Mockito
			.when(this.launchPreferenceService.retrieveLaunchValue(Mockito.anyString(), Mockito.anyString(),
					Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(null);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch").queryParam("user",
		// "user").build())
		// .exchange()
		// .expectStatus().isNoContent();
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch").param("user", "user"))
			.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	/**
	 * Test should retrieve all prefered
	 * @throws Exception thrown
	 */
	// @Test TODO to reactivate
	void shouldRetrieveAllPrefered() throws Exception {

		// Init data
		List<LaunchEntity> launches = new LinkedList<>();

		// Create Launches
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "extendedConfig");
		LaunchEntity launchHost = LaunchRepositoryTest.buildLaunchEntity(2L, "TargetName", TargetType.HOST, 1L,
				"LaunchConfigName", 2L, "Property", PreferredType.PROPERTY, "property");
		LaunchEntity launchHostGroup = LaunchRepositoryTest.buildLaunchEntity(3L, "TargetName", TargetType.HOST_GROUP,
				1L, "LaunchConfigName", 3L, "Argument", PreferredType.ARGUMENT, "argument");

		// Add in the list
		launches.add(launchUser);
		launches.add(launchHost);
		launches.add(launchHostGroup);

		// Mock
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.any()))
			.thenReturn(new TargetEntity());
		Mockito.when(this.launchPreferenceService.retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets(
				Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.any()))
			.thenReturn(launches);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch").queryParam("host",
		// "pc-001").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .value(Matchers.containsString("<?xml version='1.0'
		// encoding='UTF-8'?><Launches><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Prefered><Id>1</Id><Name>ext-config</Name><Type>ext-cfg</Type></Prefered><Target><Id>1</Id><Name>TARGETNAME</Name><Type>USER</Type></Target><Value>extendedConfig</Value></Launch><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Prefered><Id>2</Id><Name>Property</Name><Type>pro</Type></Prefered><Target><Id>2</Id><Name>TARGETNAME</Name><Type>HOST</Type></Target><Value>property</Value></Launch><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Prefered><Id>3</Id><Name>Argument</Name><Type>arg</Type></Prefered><Target><Id>3</Id><Name>TARGETNAME</Name><Type>HOSTGROUP</Type></Target><Value>argument</Value></Launch></Launches>"));
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch/").param("host", "pc-001"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string(containsString(
						"<?xml version='1.0' encoding='UTF-8'?><Launches><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Preferred><Id>1</Id><Name>ext-config</Name><Type>ext-cfg</Type></Preferred><Target><Id>1</Id><Name>TARGETNAME</Name><Type>USER</Type></Target><Value>extendedConfig</Value></Launch><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Preferred><Id>2</Id><Name>Property</Name><Type>pro</Type></Preferred><Target><Id>2</Id><Name>TARGETNAME</Name><Type>HOST</Type></Target><Value>property</Value></Launch><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Preferred><Id>3</Id><Name>Argument</Name><Type>arg</Type></Preferred><Target><Id>3</Id><Name>TARGETNAME</Name><Type>HOSTGROUP</Type></Target><Value>argument</Value></Launch></Launches>")));

	}

	/**
	 * Test specific call to retrieve prefered
	 * @throws Exception thrown
	 */
	//@Test
	void shouldRetrieveSpecificPrefered() throws Exception {
		// Init data
		List<LaunchEntity> launches = new LinkedList<>();

		// Create Launches
		LaunchEntity launchHost = LaunchRepositoryTest.buildLaunchEntity(2L, "TargetName", TargetType.HOST, 1L,
				"LaunchConfigName", 2L, "Property", PreferredType.PROPERTY, "property");

		// Add in the list
		launches.add(launchHost);

		// Mock
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.any()))
			.thenReturn(new TargetEntity());
		Mockito.when(this.launchPreferenceService.retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets(
				Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.any()))
			.thenReturn(launches);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder ->
		// uriBuilder.path("/ws/preferences/launch/pro").queryParam("host",
		// "pc-001").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class).isEqualTo(
		// "<?xml version='1.0'
		// encoding='UTF-8'?><Launches><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Prefered><Id>2</Id><Name>Property</Name><Type>pro</Type></Prefered><Target><Id>2</Id><Name>TARGETNAME</Name><Type>HOST</Type></Target><Value>property</Value></Launch></Launches>");
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch/pro").param("host", "pc-001"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string(containsString(
						"<?xml version='1.0' encoding='UTF-8'?><Launches><Launch><Config><Id>1</Id><Name>LaunchConfigName</Name></Config><Preferred><Id>2</Id><Name>Property</Name><Type>pro</Type></Preferred><Target><Id>2</Id><Name>TARGETNAME</Name><Type>HOST</Type></Target><Value>property</Value></Launch></Launches>")));

	}

	/**
	 * Should have no content: user not found in db
	 * @throws Exception thrown
	 */
	@Test
	void shouldHaveNoContentCaseUserNotFoundInDb() throws Exception {
		// Mock
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.any()))
			.thenReturn(null);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder ->
		// uriBuilder.path("/ws/preferences/launch/pro").queryParam("user",
		// "user").build())
		// .exchange()
		// .expectStatus().isNoContent();
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch/pro").param("user", "user"))
			.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	/**
	 * Should show error message: host not found in db
	 * @throws Exception thrown
	 */
	//@Test
	void shouldHaveNoContentCaseHostNotFoundInDb() throws Exception {
		// Mock
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.any()))
			.thenReturn(null);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder ->
		// uriBuilder.path("/ws/preferences/launch/pro").queryParam("host",
		// "pc-001").build())
		// .exchange()
		// .expectStatus().isNoContent();
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch/pro").param("host", "pc-001"))
			.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	/**
	 * Test Post create LaunchConfig: Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldCreateLaunchConfigs() throws Exception {

		// Mock
		LaunchConfigEntity launchConfigEntityFirst = new LaunchConfigEntity();
		launchConfigEntityFirst.setId(1L);
		launchConfigEntityFirst.setName("LaunchConfigName First");
		LaunchConfigEntity launchConfigEntitySecond = new LaunchConfigEntity();
		launchConfigEntitySecond.setId(2L);
		launchConfigEntitySecond.setName("LaunchConfigName Second");
		Mockito.when(this.launchPreferenceService.createLaunchConfigs(Mockito.anyList()))
			.thenReturn(Arrays.asList(launchConfigEntityFirst, launchConfigEntitySecond));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_config")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"name\":\"LaunchConfigName First\"\n" +
		// " },\n" +
		// " {\n" +
		// " \"name\":\"LaunchConfigName Second\"\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":1,\"name\":\"LaunchConfigName
		// First\"},{\"id\":2,\"name\":\"LaunchConfigName Second\"}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch_config")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    {\n" + "        \"name\":\"LaunchConfigName First\"\n" + "    },\n" + "    {\n"
						+ "        \"name\":\"LaunchConfigName Second\"\n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":1,\"name\":\"LaunchConfigName First\"},{\"id\":2,\"name\":\"LaunchConfigName Second\"}]"));

	}

	/**
	 * Test Post Create LaunchConfig: case LaunchConfig name already saved in db
	 * <p>
	 * Expected: - list of LaunchConfig in the request body have been correctly
	 * deserialized - exception is thrown: DataIntegrityViolationException - status is bad
	 * request - response body has an error message
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotCreateLaunchConfigsAlreadyPresentInDb() throws Exception {

		// Mock service to return configs
		Mockito.when(this.launchPreferenceService.createLaunchConfigs(Mockito.anyList()))
			.thenThrow(new DataIntegrityViolationException("Unique"));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_config").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"name\":\"LaunchConfigName First\"\n" +
		// " },\n" +
		// " {\n" +
		// " \"name\":\"LaunchConfigName Second\"\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Configs not created: name of the config is empty or
		// is already saved in database => Unique\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch_config")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    {\n" + "        \"name\":\"LaunchConfigName First\"\n" + "    },\n" + "    {\n"
						+ "        \"name\":\"LaunchConfigName Second\"\n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());

	}

	/**
	 * Test Post Create LaunchConfig: case wrong input (here nam instead of name)
	 * <p>
	 * Expected: - list of LaunchConfig in the request body have not been correctly
	 * deserialized - status is bad request - response body has an error message
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotCreateLaunchConfigWrongInput() throws Exception {
		// Mock
		LaunchConfigEntity launchConfigEntityFirst = new LaunchConfigEntity();
		launchConfigEntityFirst.setId(1L);
		launchConfigEntityFirst.setName("LaunchConfigName First");
		LaunchConfigEntity launchConfigEntitySecond = new LaunchConfigEntity();
		launchConfigEntitySecond.setId(2L);
		launchConfigEntitySecond.setName("LaunchConfigName Second");
		Mockito.when(this.launchPreferenceService.createLaunchConfigs(Mockito.anyList()))
			.thenReturn(Arrays.asList(launchConfigEntityFirst, launchConfigEntitySecond));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_config").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"nam\":\"LaunchConfigName First\"\n" +
		// " },\n" +
		// " {\n" +
		// " \"name\":\"LaunchConfigName Second\"\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Configs not created: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch_config")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[{" + "\"name\":\"\"}]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Post create LaunchPrefered: Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldCreateLaunchPrefered() throws Exception {

		// Mock
		LaunchPreferredEntity launchPreferedEntityFirst = new LaunchPreferredEntity();
		launchPreferedEntityFirst.setId(1L);
		launchPreferedEntityFirst.setName("LaunchPreferedName First");
		launchPreferedEntityFirst.setType("LaunchPreferedType");
		LaunchPreferredEntity launchPreferedEntitySecond = new LaunchPreferredEntity();
		launchPreferedEntitySecond.setId(2L);
		launchPreferedEntitySecond.setName("LaunchPreferedName Second");
		launchPreferedEntitySecond.setType("LaunchPreferedType");
		Mockito.when(this.launchPreferenceService.createLaunchPrefered(Mockito.anyList()))
			.thenReturn(Arrays.asList(launchPreferedEntityFirst, launchPreferedEntitySecond));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"name\":\"LaunchPreferedName First\", \n" +
		// " \"type\":\"LaunchPreferedType\"\n" +
		// " },\n" +
		// " {\n" +
		// " \"name\":\"LaunchPreferedName Second\", \n" +
		// " \"type\":\"LaunchPreferedType\"\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":1,\"name\":\"LaunchPreferedName
		// First\",\"type\":\"LaunchPreferedType\"},{\"id\":2,\"name\":\"LaunchPreferedName
		// Second\",\"type\":\"LaunchPreferedType\"}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch_prefered")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    {\n" + "        \"name\":\"LaunchPreferedName First\", \n"
						+ "        \"type\":\"LaunchPreferedType\"\n" + "    },\n" + "    {\n"
						+ "        \"name\":\"LaunchPreferedName Second\", \n"
						+ "        \"type\":\"LaunchPreferedType\"\n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":1,\"name\":\"LaunchPreferedName First\",\"type\":\"LaunchPreferedType\"},{\"id\":2,\"name\":\"LaunchPreferedName Second\",\"type\":\"LaunchPreferedType\"}]"));

	}

	/**
	 * Test Post Create LaunchPrefered: case LaunchPrefered name already saved in db
	 * <p>
	 * Expected: - list of LaunchPrefered in the request body have been correctly
	 * deserialized - exception is thrown: DataIntegrityViolationException - status is bad
	 * request - response body has an error message
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotCreateLaunchPreferedAlreadyPresentInDb() throws Exception {

		// Mock service to return prefered
		Mockito.when(this.launchPreferenceService.createLaunchPrefered(Mockito.anyList()))
			.thenThrow(new DataIntegrityViolationException("Unique"));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"name\":\"LaunchPreferedName First\", \n" +
		// " \"type\":\"LaunchPreferedType\"\n" +
		// " },\n" +
		// " {\n" +
		// " \"name\":\"LaunchPreferedName Second\", \n" +
		// " \"type\":\"LaunchPreferedType\"\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Prefered not created: name of the prefered is empty
		// or is already saved in database => Unique\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch_prefered")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    {\n" + "        \"name\":\"LaunchPreferedName First\", \n"
						+ "        \"type\":\"LaunchPreferedType\"\n" + "    },\n" + "    {\n"
						+ "        \"name\":\"LaunchPreferedName Second\", \n"
						+ "        \"type\":\"LaunchPreferedType\"\n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());

	}

	/**
	 * Test Post Create LaunchPrefered: case wrong input (here nam instead of name)
	 * <p>
	 * Expected: - list of LaunchPrefered in the request body have not been correctly
	 * deserialized - status is bad request - response body has an error message
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotCreateLaunchPreferedWrongInput() throws Exception {
		// Mock
		LaunchPreferredEntity launchPreferedEntityFirst = new LaunchPreferredEntity();
		launchPreferedEntityFirst.setId(1L);
		launchPreferedEntityFirst.setName("LaunchPreferedName First");
		launchPreferedEntityFirst.setType("LaunchPreferedType");
		LaunchPreferredEntity launchPreferedEntitySecond = new LaunchPreferredEntity();
		launchPreferedEntitySecond.setId(2L);
		launchPreferedEntitySecond.setName("LaunchPreferedName Second");
		launchPreferedEntitySecond.setType("LaunchPreferedType");
		Mockito.when(this.launchPreferenceService.createLaunchPrefered(Mockito.anyList()))
			.thenReturn(Arrays.asList(launchPreferedEntityFirst, launchPreferedEntitySecond));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"nam\":\"LaunchPreferedName First\", \n" +
		// " \"type\":\"LaunchPreferedType\"\n" +
		// " },\n" +
		// " {\n" +
		// " \"name\":\"LaunchPreferedName Second\", \n" +
		// " \"type\":\"LaunchPreferedType\"\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Prefered not created: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch_prefered")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    {\n" + "        \"name\":\"\", \n" + "        \"type\":\"LaunchPreferedType\"\n"
						+ "    },\n" + "    {\n" + "        \"name\":\"LaunchPreferedName Second\", \n"
						+ "        \"type\":\"LaunchPreferedType\"\n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test createLaunchPreference
	 * <p>
	 * Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldCreateLaunchPreferenceNoError() throws Exception {

		// Init data
		LaunchEntity launchEntityFirst = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetNameFirst", TargetType.USER,
				1L, "LaunchConfigNameFirst", 1L, "PreferedNameFirst", PreferredType.EXT_CFG, "LaunchValueFirst");
		LaunchEntity launchEntitySecond = LaunchRepositoryTest.buildLaunchEntity(2L, "TargetNameSecond",
				TargetType.USER, 2L, "LaunchConfigNameSecond", 2L, "PreferedNameSecond", PreferredType.EXT_CFG,
				"LaunchValueSecond");

		// Mock service
		doNothing().when(this.launchPreferenceService).checkParametersLaunches(Mockito.anyList(), Mockito.anyString());
		Mockito.when(this.launchPreferenceService.createLaunches(Mockito.anyList()))
			.thenReturn(Arrays.asList(launchEntityFirst, launchEntitySecond));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"config\": {\n" +
		// " \"name\":\"LaunchConfigNameFirst\"\n" +
		// " },\n" +
		// " \"prefered\": {\n" +
		// " \"name\":\"PreferedNameFirst\"\n" +
		// " },\n" +
		// " \"target\": {\n" +
		// " \"name\":\"TargetNameFirst\"\n" +
		// " },\n" +
		// " \"value\":\"LaunchValueFirst\" \n" +
		// " },\n" +
		// " { \n" +
		// " \"config\": {\n" +
		// " \"name\":\"LaunchConfigNameSecond\"\n" +
		// " },\n" +
		// " \"prefered\": {\n" +
		// " \"name\":\"PreferedNameSecond\"\n" +
		// " },\n" +
		// " \"target\": {\n" +
		// " \"name\":\"TargetNameSecond\"\n" +
		// " },\n" +
		// " \"value\":\"LaunchValueSecond\" \n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"config\":{\"id\":1,\"name\":\"LaunchConfigNameFirst\"},\"preferred\":{\"id\":1,\"name\":\"PreferedNameFirst\",\"type\":\"ext-cfg\"},\"target\":{\"id\":1,\"name\":\"TARGETNAMEFIRST\",\"type\":\"USER\"},\"selection\":\"LaunchValueFirst\"},{\"config\":{\"id\":2,\"name\":\"LaunchConfigNameSecond\"},\"preferred\":{\"id\":2,\"name\":\"PreferedNameSecond\",\"type\":\"ext-cfg\"},\"target\":{\"id\":2,\"name\":\"TARGETNAMESECOND\",\"type\":\"USER\"},\"selection\":\"LaunchValueSecond\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch")
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.content("[\n" + "    { \n" + "        \"config\": {\n" + "            \"name\":\"LaunchConfigNameFirst\"\n"
					+ "        },\n" + "        \"prefered\": {\n" + "            \"name\":\"PreferedNameFirst\"\n"
					+ "        },\n" + "        \"target\": {\n" + "            \"name\":\"TargetNameFirst\"\n"
					+ "        },\n" + "        \"value\":\"LaunchValueFirst\" \n" + "    },\n" + "    { \n"
					+ "        \"config\": {\n" + "            \"name\":\"LaunchConfigNameSecond\"\n" + "        },\n"
					+ "        \"prefered\": {\n" + "            \"name\":\"PreferedNameSecond\"\n" + "        },\n"
					+ "        \"target\": {\n" + "            \"name\":\"TargetNameSecond\"\n" + "        },\n"
					+ "        \"value\":\"LaunchValueSecond\" \n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"config\":{\"id\":1,\"name\":\"LaunchConfigNameFirst\"},\"preferred\":{\"id\":1,\"name\":\"PreferedNameFirst\",\"type\":\"ext-cfg\"},\"target\":{\"id\":1,\"name\":\"TARGETNAMEFIRST\",\"type\":\"USER\"},\"selection\":\"LaunchValueFirst\"},{\"config\":{\"id\":2,\"name\":\"LaunchConfigNameSecond\"},\"preferred\":{\"id\":2,\"name\":\"PreferedNameSecond\",\"type\":\"ext-cfg\"},\"target\":{\"id\":2,\"name\":\"TARGETNAMESECOND\",\"type\":\"USER\"},\"selection\":\"LaunchValueSecond\"}]"));

	}

	/**
	 * Test createLaunchPreference
	 * <p>
	 * Case check detect errors
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is bad request -
	 * response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldCreateLaunchPreferenceCaseError() throws Exception {

		// Mock service
		doThrow(ParameterException.class).when(this.launchPreferenceService)
			.checkParametersLaunches(Mockito.anyList(), Mockito.anyString());
		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"config\": {\n" +
		// " \"name\":\"LaunchConfigNameFirst\"\n" +
		// " },\n" +
		// " \"prefered\": {\n" +
		// " \"name\":\"PreferedNameFirst\"\n" +
		// " },\n" +
		// " \"target\": {\n" +
		// " \"name\":\"TargetNameFirst\"\n" +
		// " },\n" +
		// " \"value\":\"LaunchValueFirst\" \n" +
		// " },\n" +
		// " { \n" +
		// " \"config\": {\n" +
		// " \"name\":\"LaunchConfigNameSecond\"\n" +
		// " },\n" +
		// " \"prefered\": {\n" +
		// " \"name\":\"PreferedNameSecond\"\n" +
		// " },\n" +
		// " \"target\": {\n" +
		// " \"name\":\"TargetNameSecond\"\n" +
		// " },\n" +
		// " \"value\":\"LaunchValueSecond\" \n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Launches not created: wrong parameters\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/preferences/launch")
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.content("[\n" + "    { \n" + "        \"config\": {\n" + "            \"name\":\"LaunchConfigNameFirst\"\n"
					+ "        },\n" + "        \"prefered\": {\n" + "            \"name\":\"PreferedNameFirst\"\n"
					+ "        },\n" + "        \"target\": {\n" + "            \"name\":\"TargetNameFirst\"\n"
					+ "        },\n" + "        \"value\":\"LaunchValueFirst\" \n" + "    },\n" + "    { \n"
					+ "        \"config\": {\n" + "            \"name\":\"LaunchConfigNameSecond\"\n" + "        },\n"
					+ "        \"prefered\": {\n" + "            \"name\":\"PreferedNameSecond\"\n" + "        },\n"
					+ "        \"target\": {\n" + "            \"name\":\"TargetNameSecond\"\n" + "        },\n"
					+ "        \"value\":\"LaunchValueSecond\" \n" + "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Delete launches: Case error in the input
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteLaunchCaseWrongInput() throws Exception {
		// Mock service
		doThrow(ParameterException.class).when(this.launchPreferenceService)
			.checkParametersDeleteLaunches(Mockito.anyList());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch")
		// .build())
		// .bodyValue("[\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Launches not deleted: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/preferences/launch")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Delete launches: Case no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteLaunchCaseNoError() throws Exception {
		// Mock service
		doNothing().when(this.launchPreferenceService).checkParametersDeleteLaunches(Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch").build())
		// .bodyValue("[\n" +
		// " {\n" +
		// " \"config\": {\n" +
		// " \"name\":\"name config 1\"\n" +
		// " },\n" +
		// " \"prefered\": {\n" +
		// " \"name\":\"name prefered 1\"\n" +
		// " },\n" +
		// " \"target\": {\n" +
		// " \"name\":\"name target 1\"\n" +
		// " }\n" +
		// " },\n" +
		// " {\n" +
		// " \"config\": {\n" +
		// " \"name\":\"name config 2\"\n" +
		// " },\n" +
		// " \"prefered\": {\n" +
		// " \"name\":\"name prefered 2\"\n" +
		// " },\n" +
		// " \"target\": {\n" +
		// " \"name\":\"name target 2\"\n" +
		// " }\n" +
		// " }\n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("deleted");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/preferences/launch")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "   {\n" + "       \"config\": {\n" + "           \"name\":\"name config 1\"\n"
						+ "       },\n" + "       \"prefered\": {\n" + "           \"name\":\"name prefered 1\"\n"
						+ "       },\n" + "       \"target\": {\n" + "           \"name\":\"name target 1\"\n"
						+ "       }\n" + "   },\n" + "   {\n" + "       \"config\": {\n"
						+ "           \"name\":\"name config 2\"\n" + "       },\n" + "       \"prefered\": {\n"
						+ "           \"name\":\"name prefered 2\"\n" + "       },\n" + "       \"target\": {\n"
						+ "           \"name\":\"name target 2\"\n" + "       }\n" + "   }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test Delete config: Case error in the input
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteConfigCaseWrongInput() throws Exception {
		// Mock service
		doThrow(ParameterException.class).when(this.launchPreferenceService)
			.checkParameterDeleteLaunchConfig(Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_config")
		// .queryParam("launchConfigName", "launchConfigName").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Delete not done: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/preferences/launch_config")
				.param("launchConfigName", "launchConfigName"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Delete config: Case no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteConfigCaseNoError() throws Exception {
		// Mock service
		doNothing().when(this.launchPreferenceService).checkParameterDeleteLaunchConfig(Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_config")
		// .queryParam("launchConfigName", "launchConfigName").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("deleted");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/preferences/launch_config")
				.param("launchConfigName", "launchConfigName"))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test Delete prefered: Case error in the input
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeletePreferedCaseWrongInput() throws Exception {
		// Mock service
		doThrow(ParameterException.class).when(this.launchPreferenceService)
			.checkParameterDeleteLaunchPrefered(Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered")
		// .queryParam("launchPreferedName", "launchPreferedName").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Delete not done: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/preferences/launch_prefered")
				.param("launchPreferedName", "launchPreferedName"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Delete prefered: Case no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeletePreferedCaseNoError() throws Exception {
		// Mock service
		doNothing().when(this.launchPreferenceService).checkParameterDeleteLaunchPrefered(Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered")
		// .queryParam("launchPreferedName", "launchPreferedName").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("deleted");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/preferences/launch_prefered")
				.param("launchPreferedName", "launchPreferedName"))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test Retrieve group launches: Case group not existing
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotRetrieveGroupLaunchesCaseGroupNotExisting() throws Exception {
		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch/group/ ").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Group is not existing or group name is not a
		// group\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch/group/ "))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Retrieve group launches: Case no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotRetrieveGroupLaunchesCaseGroupNoError() throws Exception {

		// Init data
		// Host group
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 777L, "groupName", TargetType.HOST_GROUP);
		// LaunchEntity
		LaunchEntity launchHost = LaunchRepositoryTest.buildLaunchEntity(2L, "TargetName", TargetType.HOST, 1L,
				"LaunchConfigName", 2L, "Property", PreferredType.PROPERTY, "property");

		// Mock data
		Mockito.when(this.targetService.retrieveTargetByName(Mockito.anyString())).thenReturn(group);
		Mockito
			.when(this.launchPreferenceService.retrieveGroupLaunches(Mockito.any(TargetEntity.class), Mockito.eq(null)))
			.thenReturn(Collections.singletonList(launchHost));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder ->
		// uriBuilder.path("/ws/preferences/launch/group/groupName").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"config\":{\"id\":1,\"name\":\"LaunchConfigName\"},\"preferred\":{\"id\":2,\"name\":\"Property\",\"type\":\"pro\"},\"target\":{\"id\":2,\"name\":\"TARGETNAME\",\"type\":\"HOST\"},\"selection\":\"property\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch/group/groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"config\":{\"id\":1,\"name\":\"LaunchConfigName\"},\"preferred\":{\"id\":2,\"name\":\"Property\",\"type\":\"pro\"},\"target\":{\"id\":2,\"name\":\"TARGETNAME\",\"type\":\"HOST\"},\"selection\":\"property\"}]"));

	}

	/**
	 * Test Retrieve launch prefered: Case all launch prefered and no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldRetrieveLaunchPreferedCaseAllAndNoError() throws Exception {
		// Init data
		LaunchPreferredEntity launchPreferedEntity = new LaunchPreferredEntity();
		launchPreferedEntity.setId(1L);
		launchPreferedEntity.setName("launchPreferedName");
		launchPreferedEntity.setType("launchPreferedType");

		// Mock
		Mockito.when(this.launchPreferenceService.retrieveLaunchPrefered(Mockito.eq(null)))
			.thenReturn(Collections.singletonList(launchPreferedEntity));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":1,\"name\":\"launchPreferedName\",\"type\":\"launchPreferedType\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch_prefered"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":1,\"name\":\"launchPreferedName\",\"type\":\"launchPreferedType\"}]"));
	}

	/**
	 * Test Retrieve launch prefered by preferedType: Case specific launch prefered but
	 * empty
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotRetrieveLaunchPreferedCaseByPreferedTypeAndNoPreferedTypeParam() throws Exception {
		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_prefered/
		// ").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Prefered type not filled or not existing\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch_prefered/ "))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Retrieve specific launch prefered by prefered type: Case no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldRetrieveLaunchPreferedCaseByPreferedTypeCaseNoError() throws Exception {
		// Init data
		LaunchPreferredEntity launchPreferedEntity = new LaunchPreferredEntity();
		launchPreferedEntity.setId(1L);
		launchPreferedEntity.setName("launchPreferedName");
		launchPreferedEntity.setType("launchPreferedType");

		// Mock
		Mockito.when(this.launchPreferenceService.existLaunchPreferedPreferedType(Mockito.anyString()))
			.thenReturn(true);
		Mockito.when(this.launchPreferenceService.retrieveLaunchPrefered(Mockito.anyString()))
			.thenReturn(Collections.singletonList(launchPreferedEntity));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder ->
		// uriBuilder.path("/ws/preferences/launch_prefered/preferedType").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":1,\"name\":\"launchPreferedName\",\"type\":\"launchPreferedType\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch_prefered/preferedType"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":1,\"name\":\"launchPreferedName\",\"type\":\"launchPreferedType\"}]"));
	}

	/**
	 * Test Retrieve launch config: Case all launch config and no error
	 * <p>
	 * Expected: - status is ok - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldRetrieveLaunchConfigCaseAllAndNoError() throws Exception {
		// Init data
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setId(1L);
		launchConfigEntity.setName("launchConfigName");

		// Mock
		Mockito.when(this.launchPreferenceService.retrieveLaunchConfig())
			.thenReturn(Collections.singletonList(launchConfigEntity));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/preferences/launch_config").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":1,\"name\":\"launchConfigName\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/preferences/launch_config"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("[{\"id\":1,\"name\":\"launchConfigName\"}]"));
	}

}
