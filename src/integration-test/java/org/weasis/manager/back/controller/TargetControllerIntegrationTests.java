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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.weasis.manager.back.config.s3.S3ClientConfigurationProperties;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.GroupRepositoryTest;
import org.weasis.manager.back.service.GroupService;
import org.weasis.manager.back.service.LaunchPreferenceService;
import org.weasis.manager.back.service.impl.TargetServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doNothing;

/**
 * Tests for TargetController
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = TargetController.class)
@WebMvcTest(controllers = TargetController.class)
class TargetControllerIntegrationTests {

	private MockMvc mockMvc;

	// @Autowired
	// private WebTestClient webTestClient;

	@MockBean
	private TargetServiceImpl targetService;

	@MockBean
	private LaunchPreferenceService launchPreferenceService;

	@MockBean
	private GroupService groupService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Test Post Create targets
	 * <p>
	 * Expected: - list of targets in the request body have been correctly deserialized -
	 * mocked targets have been created and returned from the service createTargets -
	 * status is OK - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldCreateTargets() throws Exception {

		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		TargetEntity targetHost = GroupRepositoryTest.buildTarget(true, 1L, "Target Host", TargetType.HOST);
		TargetEntity targetHostGroup = GroupRepositoryTest.buildTarget(true, 2L, "Target HostGroup",
				TargetType.HOST_GROUP);
		TargetEntity targetUserGroup = GroupRepositoryTest.buildTarget(true, 3L, "Target UserGroup",
				TargetType.USER_GROUP);
		TargetEntity targetUser = GroupRepositoryTest.buildTarget(true, 4L, "Target User", TargetType.USER);
		targets.add(targetHost);
		targets.add(targetHostGroup);
		targets.add(targetUserGroup);
		targets.add(targetUser);

		// Mock service to return targets
		Mockito.when(this.targetService.createTargets(Mockito.any())).thenReturn(targets);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/target")
		// .queryParam("user", "user")
		// .queryParam("profile", "default")
		// .queryParam("module", "weasis")
		// .build())
		// .contentType(MediaType.APPLICATION_JSON)
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"Target Host\", \n" +
		// " \"type\":\"HOST\" \n" +
		// " }, \n" +
		// " { \n" +
		// " \"name\":\"Target User\", \n" +
		// " \"type\":\"USER\" \n" +
		// " },\n" +
		// " { \n" +
		// " \"name\":\"Target UserGroup\", \n" +
		// " \"type\":\"USERGROUP\" \n" +
		// " },\n" +
		// " { \n" +
		// " \"name\":\"Target HostGroup\", \n" +
		// " \"type\":\"HOSTGROUP\" \n" +
		// " }\n" +
		// "]")
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":1,\"name\":\"TARGET
		// HOST\",\"type\":\"HOST\"},{\"id\":2,\"name\":\"TARGET
		// HOSTGROUP\",\"type\":\"HOSTGROUP\"},{\"id\":3,\"name\":\"TARGET
		// USERGROUP\",\"type\":\"USERGROUP\"},{\"id\":4,\"name\":\"TARGET
		// USER\",\"type\":\"USER\"}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/target")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"Target Host\", \n" + "        \"type\":\"HOST\" \n"
						+ "    }, \n" + "    { \n" + "        \"name\":\"Target User\", \n"
						+ "        \"type\":\"USER\" \n" + "    },\n" + "    { \n"
						+ "        \"name\":\"Target UserGroup\", \n" + "        \"type\":\"USERGROUP\" \n" + "    },\n"
						+ "    { \n" + "        \"name\":\"Target HostGroup\", \n" + "        \"type\":\"HOSTGROUP\" \n"
						+ "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":1,\"name\":\"TARGET HOST\",\"type\":\"HOST\"},{\"id\":2,\"name\":\"TARGET HOSTGROUP\",\"type\":\"HOSTGROUP\"},{\"id\":3,\"name\":\"TARGET USERGROUP\",\"type\":\"USERGROUP\"},{\"id\":4,\"name\":\"TARGET USER\",\"type\":\"USER\"}]"));

	}

	/**
	 * Test Post Create targets: case wrong input (here wrong code for prefered type)
	 * <p>
	 * Expected: - list of targets in the request body have not been correctly
	 * deserialized - status is bad request - response body has an error message
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotCreateTargetsWrongInput() throws Exception {

		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		TargetEntity targetHost = GroupRepositoryTest.buildTarget(true, 1L, "Target Host", TargetType.HOST);
		TargetEntity targetHostGroup = GroupRepositoryTest.buildTarget(true, 2L, "Target HostGroup",
				TargetType.HOST_GROUP);
		TargetEntity targetUserGroup = GroupRepositoryTest.buildTarget(true, 3L, "Target UserGroup",
				TargetType.USER_GROUP);
		TargetEntity targetUser = GroupRepositoryTest.buildTarget(true, 4L, "Target User", TargetType.USER);
		targets.add(targetHost);
		targets.add(targetHostGroup);
		targets.add(targetUserGroup);
		targets.add(targetUser);

		// Mock service to return targets
		Mockito.when(this.targetService.createTargets(Mockito.any())).thenReturn(targets);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/target")
		// .build())
		// .contentType(MediaType.APPLICATION_JSON)
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"Target Host\", \n" +
		// " \"type\":\"XXX\" \n" +
		// " } \n" +
		// "]")
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Targets not created: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/target")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"Target Host\", \n" + "        \"type\":\"XXX\" \n"
						+ "    } \n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Post Create targets: case target name already saved in db
	 * <p>
	 * Expected: - list of targets in the request body have been correctly deserialized -
	 * exception is thrown: DataIntegrityViolationException - status is bad request -
	 * response body has an error message
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotCreateTargetsTargetAlreadyPresentInDb() throws Exception {

		// Mock service to return targets
		Mockito.when(this.targetService.createTargets(Mockito.any()))
			.thenThrow(new DataIntegrityViolationException("Unique"));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/target")
		// .build())
		// .contentType(MediaType.APPLICATION_JSON)
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"Target Host\", \n" +
		// " \"type\":\"HOST\" \n" +
		// " }, \n" +
		// " { \n" +
		// " \"name\":\"Target User\", \n" +
		// " \"type\":\"USER\" \n" +
		// " },\n" +
		// " { \n" +
		// " \"name\":\"Target UserGroup\", \n" +
		// " \"type\":\"USERGROUP\" \n" +
		// " },\n" +
		// " { \n" +
		// " \"name\":\"Target HostGroup\", \n" +
		// " \"type\":\"HOSTGROUP\" \n" +
		// " }\n" +
		// "]")
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Targets not created: name of the target is empty or
		// is already saved in database => Unique\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/target")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"Target Host\", \n" + "        \"type\":\"HOST\" \n"
						+ "    }, \n" + "    { \n" + "        \"name\":\"Target User\", \n"
						+ "        \"type\":\"USER\" \n" + "    },\n" + "    { \n"
						+ "        \"name\":\"Target UserGroup\", \n" + "        \"type\":\"USERGROUP\" \n" + "    },\n"
						+ "    { \n" + "        \"name\":\"Target HostGroup\", \n" + "        \"type\":\"HOSTGROUP\" \n"
						+ "    }\n" + "]"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());

	}

	/**
	 * Test Delete targets: Case no error
	 * <p>
	 * Expected: - name of the target have been correctly deserialized - status is OK -
	 * response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteTargetsCaseNoError() throws Exception {
		// Init data
		TargetEntity targetHostGroup = GroupRepositoryTest.buildTarget(true, 2L, "Target HostGroup",
				TargetType.HOST_GROUP);

		// Mock service to return targets
		doNothing().when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		Mockito.when(this.launchPreferenceService.hasLaunchWithTargetName(Mockito.anyString())).thenReturn(false);
		Mockito.when(this.targetService.retrieveTargetByName(Mockito.anyString())).thenReturn(targetHostGroup);
		doNothing().when(this.groupService).deleteGroupAssociation(Mockito.any(TargetEntity.class));
		doNothing().when(this.targetService).deleteTarget(Mockito.any(TargetEntity.class));

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/target")
		// .queryParam("targetName", "nameTarget").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("Group associations and target deleted");
		this.mockMvc.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/target").param("targetName", "nameTarget"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("Group associations and target deleted"));
	}

	/**
	 * Test Delete targets: Case error in the input: empty
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteTargetsCaseEmptyName() throws Exception {
		// Mock service
		Mockito.doCallRealMethod().when(this.targetService).checkParametersDeleteTarget(Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/target")
		// .queryParam("targetName", " ").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Delete not done: wrong parameters\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/target").param("targetName", " "))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Delete targets: Case a launch is still associated to the target
	 * <p>
	 * Expected: - name of the target have been correctly deserialized - status is bad
	 * request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	public void shouldDeleteTargetsCaseLaunchStillAssociated() throws Exception {
		// Init data
		TargetEntity targetUser = GroupRepositoryTest.buildTarget(true, 2L, "Target User", TargetType.USER);

		// Mock service to return targets
		doNothing().when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		Mockito.when(this.launchPreferenceService.hasLaunchWithTargetName(Mockito.anyString())).thenReturn(true);
		Mockito.when(this.targetService.retrieveTargetByName(Mockito.anyString())).thenReturn(targetUser);
		doNothing().when(this.groupService).deleteGroupAssociation(Mockito.any(TargetEntity.class));
		doNothing().when(this.targetService).deleteTarget(Mockito.any(TargetEntity.class));

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/target")
		// .queryParam("targetName", "nameTarget").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Delete not done: a launch is associated to the
		// target. Please remove launch before deleting target.\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/target").param("targetName", "nameTarget"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());

	}

}
