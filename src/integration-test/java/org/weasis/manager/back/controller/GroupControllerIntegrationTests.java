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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.weasis.manager.back.config.s3.S3ClientConfigurationProperties;
import org.weasis.manager.back.controller.exception.ParameterException;
import org.weasis.manager.back.entity.GroupEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.GroupRepositoryTest;
import org.weasis.manager.back.service.GroupService;
import org.weasis.manager.back.service.TargetService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * Tests for WeasisGroupResource
 */

@WithMockUser
@ExtendWith(SpringExtension.class)
// @WebFluxTest(controllers = GroupController.class)
@WebMvcTest(controllers = GroupController.class)
class GroupControllerIntegrationTests {

	private MockMvc mockMvc;

	// @Autowired
	// private WebTestClient webTestClient;

	@MockBean
	private TargetService targetService;

	@MockBean
	private GroupService groupService;

	@MockBean
	private S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@BeforeEach
	public void setUp(WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Test AssociateUsersToUserGroup
	 * <p>
	 * Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldAssociateUsersToUserGroupCaseNoError() throws Exception {

		// Init data
		// Target from type Group to return
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 888L, "groupName", TargetType.USER);
		// Target to return
		TargetEntity targetA = GroupRepositoryTest.buildTarget(true, 333L, "aaaa", TargetType.USER);
		// Group to return
		GroupEntity groupEntity = GroupRepositoryTest.buildGroup(group, targetA);

		// Mock service
		doNothing().when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		Mockito
			.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(),
					Mockito.eq(TargetType.USER_GROUP)))
			.thenReturn(group);
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.eq(TargetType.USER)))
			.thenReturn(targetA);
		Mockito.when(this.groupService.createGroupAssociation(Mockito.any(), Mockito.any()))
			.thenReturn(Collections.singletonList(groupEntity));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"association\":{\"groupId\":888,\"memberId\":333}}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/group/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[{\"name\":\"aaaa\",\"type\":\"HOST\"}]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(
					MockMvcResultMatchers.content().string("[{\"association\":{\"groupId\":888,\"memberId\":333}}]"));
	}

	/**
	 * Test AssociateHostsToHostGroup
	 * <p>
	 * Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldAssociateHostsToHostGroupCaseNoError() throws Exception {

		// Init data
		// Target from type Group to return
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 888L, "groupName", TargetType.HOST);
		// Target to return
		TargetEntity targetA = GroupRepositoryTest.buildTarget(true, 333L, "aaaa", TargetType.HOST);
		// Group to return
		GroupEntity groupEntity = GroupRepositoryTest.buildGroup(group, targetA);

		// Mock service
		doNothing().when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		Mockito
			.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST_GROUP)))
			.thenReturn(group);
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.eq(TargetType.HOST)))
			.thenReturn(targetA);
		Mockito.when(this.groupService.createGroupAssociation(Mockito.any(), Mockito.any()))
			.thenReturn(Collections.singletonList(groupEntity));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"association\":{\"groupId\":888,\"memberId\":333}}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/group/hosts")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[{\"name\":\"aaaa\",\"type\":\"HOST\"}]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(
					MockMvcResultMatchers.content().string("[{\"association\":{\"groupId\":888,\"memberId\":333}}]"));
	}

	/**
	 * Test AssociateUsersToUserGroup
	 * <p>
	 * Case check detect errors
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is bad request -
	 * response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldAssociateUsersToUserGroupCaseError() throws Exception {
		// Mock service
		doThrow(ParameterException.class).when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());

		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Associations not created: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/group/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"aaaa\" \n" + "    } \n" + "]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test AssociateHostsToHostGroup
	 * <p>
	 * Case check detect errors
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is bad request -
	 * response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldAssociateHostsToHostGroupCaseError() throws Exception {

		// Mock service
		doThrow(ParameterException.class).when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		// Call service and test results
		// webTestClient.mutateWith(csrf()).post()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Associations not created: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/weasisconfig/ws/group/hosts")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"aaaa\" \n" + "    } \n" + "]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test deleteUsersFromUserGroup
	 * <p>
	 * Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteUsersFromUserGroupCaseNoError() throws Exception {
		// Init data
		// Target from type Group to return
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 888L, "groupName", TargetType.USER);
		// Target to return
		TargetEntity targetA = GroupRepositoryTest.buildTarget(true, 333L, "aaaa", TargetType.USER);

		// Mock service
		doNothing().when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		Mockito
			.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(),
					Mockito.eq(TargetType.USER_GROUP)))
			.thenReturn(group);
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.eq(TargetType.USER)))
			.thenReturn(targetA);
		Mockito.doNothing().when(this.groupService).deleteMembers(Mockito.any(), Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("members deleted");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/group/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[{\"name\":\"aaaa\",\"type\":\"HOST\"}]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test deleteHostsFromHostGroup
	 * <p>
	 * Case no error
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is OK - response body
	 * has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteHostsFromHostGroupCaseNoError() throws Exception {
		// Init data
		// Target from type Group to return
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 888L, "groupName", TargetType.HOST);
		// Target to return
		TargetEntity targetA = GroupRepositoryTest.buildTarget(true, 333L, "aaaa", TargetType.HOST);

		// Mock service
		doNothing().when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
		Mockito
			.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST_GROUP)))
			.thenReturn(group);
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.eq(TargetType.HOST)))
			.thenReturn(targetA);
		Mockito.doNothing().when(this.groupService).deleteMembers(Mockito.any(), Mockito.any());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("members deleted");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/group/hosts")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				// .content("[\n" + " { \n" + " \"name\":\"aaaa\" \n" + " } \n" +
				// "]")
				.content("[{\"name\":\"aaaa\",\"type\":\"HOST\"}]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("members deleted"));
	}

	/**
	 * Test DeleteUsersFromUserGroup
	 * <p>
	 * Case check detect errors
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is bad request -
	 * response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteUsersFromUserGroupCaseError() throws Exception {

		// Mock service
		doThrow(ParameterException.class).when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Associations not deleted: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/group/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"aaaa\" \n" + "    } \n" + "]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test DeleteHostsFromHostGroup
	 * <p>
	 * Case check detect errors
	 * <p>
	 * Expected: - mocked calls have been correctly done - status is bad request -
	 * response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldDeleteHostsFromHostGroupCaseError() throws Exception {

		// Mock service
		doThrow(ParameterException.class).when(this.targetService)
			.checkParametersAssociation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());

		// Call service and test results
		// webTestClient.mutateWith(csrf())
		// .method(HttpMethod.DELETE)
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts")
		// .queryParam("groupName", "groupName").build())
		// .bodyValue("[\n" +
		// " { \n" +
		// " \"name\":\"aaaa\" \n" +
		// " } \n" +
		// "]")
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Associations not deleted: wrong parameters\"}");
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/weasisconfig/ws/group/hosts")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("[\n" + "    { \n" + "        \"name\":\"aaaa\" \n" + "    } \n" + "]")
				.param("groupName", "groupName"))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Should retrieve the user groups
	 * <p>
	 * Expected mocked user groups have been retrieved
	 */
	@Test
	void shouldRetrieveUserGroups() throws Exception {

		// Target from type Group to return
		TargetEntity groupA = GroupRepositoryTest.buildTarget(true, 888L, "groupNameA", TargetType.USER_GROUP);
		TargetEntity groupB = GroupRepositoryTest.buildTarget(true, 999L, "groupNameB", TargetType.USER_GROUP);

		// Mock service
		Mockito.when(this.targetService.retrieveTargetsByType(Mockito.any())).thenReturn(Arrays.asList(groupA, groupB));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users").build())
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":888,\"name\":\"GROUPNAMEA\",\"type\":\"USERGROUP\"},{\"id\":999,\"name\":\"GROUPNAMEB\",\"type\":\"USERGROUP\"}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/group/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":888,\"name\":\"GROUPNAMEA\",\"type\":\"USERGROUP\"},{\"id\":999,\"name\":\"GROUPNAMEB\",\"type\":\"USERGROUP\"}]"));
	}

	/**
	 * Should retrieve the host groups
	 * <p>
	 * Expected mocked host groups have been retrieved
	 */
	@Test
	void shouldRetrieveHostGroups() throws Exception {

		// Target from type Group to return
		TargetEntity groupA = GroupRepositoryTest.buildTarget(true, 888L, "groupNameA", TargetType.HOST_GROUP);
		TargetEntity groupB = GroupRepositoryTest.buildTarget(true, 999L, "groupNameB", TargetType.HOST_GROUP);

		// Mock service
		Mockito.when(this.targetService.retrieveTargetsByType(Mockito.any())).thenReturn(Arrays.asList(groupA, groupB));

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts").build())
		// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":888,\"name\":\"GROUPNAMEA\",\"type\":\"HOSTGROUP\"},{\"id\":999,\"name\":\"GROUPNAMEB\",\"type\":\"HOSTGROUP\"}]");
		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/group/hosts")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":888,\"name\":\"GROUPNAMEA\",\"type\":\"HOSTGROUP\"},{\"id\":999,\"name\":\"GROUPNAMEB\",\"type\":\"HOSTGROUP\"}]"));

	}

	/**
	 * Test Retrieve Users From User Group: Case group name is not found
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotRetrieveUsersFromUserGroupCaseGroupNameNotFound() throws Exception {
		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users/ ").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Group name with type USERGROUP not found\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/group/users/ "))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Retrieve Hosts From Host Group: Case group name is not found
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldNotRetrieveHostsFromHostGroupCaseGroupNameNotFound() throws Exception {
		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts/ ").build())
		// .exchange()
		// .expectStatus().isBadRequest()
		// .expectBody(String.class)
		// .isEqualTo("{\"message\":\"Group name with type HOSTGROUP not found\"}");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/group/hosts/ "))
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Test Retrieve Users From User Group: Case group have been found
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldRetrieveUsersFromUserGroupCaseGroupFound() throws Exception {

		// Init data
		// User group
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 777L, "groupName", TargetType.USER_GROUP);
		// Users
		TargetEntity userA = GroupRepositoryTest.buildTarget(true, 888L, "userA", TargetType.USER);
		TargetEntity userB = GroupRepositoryTest.buildTarget(true, 999L, "userB", TargetType.USER);

		// Mock service
		Mockito.when(this.targetService.retrieveTargetsByIds(Mockito.anyList()))
			.thenReturn(Arrays.asList(userA, userB));
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.any(TargetType.class)))
			.thenReturn(group);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/users/groupName").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":888,\"name\":\"USERA\",\"type\":\"USER\"},{\"id\":999,\"name\":\"USERB\",\"type\":\"USER\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/group/users/groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":888,\"name\":\"USERA\",\"type\":\"USER\"},{\"id\":999,\"name\":\"USERB\",\"type\":\"USER\"}]"));

	}

	/**
	 * Test Retrieve Hosts From Host Group: Case group have been found
	 * <p>
	 * Expected: - status is bad request - response body has the correct values and format
	 * @throws Exception thrown
	 */
	@Test
	void shouldRetrieveHostsFromHostGroupCaseGroupFound() throws Exception {

		// Init data
		// Host group
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 777L, "groupName", TargetType.HOST_GROUP);
		// Hosts
		TargetEntity hostA = GroupRepositoryTest.buildTarget(true, 888L, "hostA", TargetType.HOST);
		TargetEntity hostB = GroupRepositoryTest.buildTarget(true, 999L, "hostB", TargetType.HOST);

		// Mock service
		Mockito.when(this.targetService.retrieveTargetsByIds(Mockito.anyList()))
			.thenReturn(Arrays.asList(hostA, hostB));
		Mockito.when(this.targetService.retrieveTargetByNameAndType(Mockito.anyString(), Mockito.any(TargetType.class)))
			.thenReturn(group);

		// Call service and test results
		// webTestClient.mutateWith(csrf()).get()
		// .uri(uriBuilder -> uriBuilder.path("/ws/group/hosts/groupName").build())
		// .exchange()
		// .expectStatus().isOk()
		// .expectBody(String.class)
		// .isEqualTo("[{\"id\":888,\"name\":\"HOSTA\",\"type\":\"HOST\"},{\"id\":999,\"name\":\"HOSTB\",\"type\":\"HOST\"}]");
		this.mockMvc.perform(MockMvcRequestBuilders.get("/weasisconfig/ws/group/hosts/groupName"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content()
				.string("[{\"id\":888,\"name\":\"HOSTA\",\"type\":\"HOST\"},{\"id\":999,\"name\":\"HOSTB\",\"type\":\"HOST\"}]"));

	}

}
