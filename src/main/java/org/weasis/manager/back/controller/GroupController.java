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

package org.weasis.manager.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.controller.exception.NotFoundException;
import org.weasis.manager.back.entity.GroupEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.ErrorMessage;
import org.weasis.manager.back.service.GroupService;
import org.weasis.manager.back.service.TargetService;
import org.weasis.manager.back.util.SpringDocUtil;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * End points for groups
 */
@RestController
@RequestMapping(EndPoint.GROUP_PATH)
@Tag(name = "Group", description = "API Endpoints for Group")
@Slf4j
@Validated
public class GroupController {

	// Request params
	private static final String PARAM_GROUP_NAME = "groupName";

	// Result
	private static final String RESULT_MEMBERS_DELETED = "members deleted";

	// Services
	private final TargetService targetService;

	private final GroupService groupService;

	/**
	 * Autowired constructor
	 * @param targetService Target service
	 * @param groupService Group service
	 */
	@Autowired
	public GroupController(TargetService targetService, GroupService groupService) {
		this.targetService = targetService;
		this.groupService = groupService;
	}

	@Operation(summary = "Associate users to the group in parameter",
			description = SpringDocUtil.descriptionCheckParametersAssociation, tags = "Group")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyUsersToUserGroup)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Association has been done",
					content = @Content(
							array = @ArraySchema(schema = @Schema(type = "array", implementation = GroupEntity.class)),
							examples = @ExampleObject(value = SpringDocUtil.exObjValRespAssociateUsersToUserGroup))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Associate users to the group in parameter
	 * @param userGroupName User Group Name
	 * @param userTargets User Targets to associate to the group in parameter
	 * @return The response of the call
	 */
	@PostMapping(value = "/users", consumes = { MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<GroupEntity>> associateUsersToUserGroup(
			@RequestParam(PARAM_GROUP_NAME) @NotBlank String userGroupName,
			@RequestBody @Valid List<TargetEntity> userTargets) {
		return this.associateTargetsToTargetGroup(userGroupName, userTargets, TargetType.USER);
	}

	@Operation(summary = "Associate hosts to the group in parameter",
			description = SpringDocUtil.descriptionCheckParametersAssociation, tags = "Group")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyHostsToHostGroup)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Association has been done",
					content = @Content(
							array = @ArraySchema(schema = @Schema(type = "array", implementation = GroupEntity.class)),
							examples = @ExampleObject(value = SpringDocUtil.exObjValRespAssociateHostsToHostGroup))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Associate hosts to the group in parameter
	 * @param hostGroupName Host Group Name
	 * @param hostTargets Host Targets to associate to the group in parameter
	 * @return The response of the call
	 */
	@PostMapping(value = "/hosts", consumes = { MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<GroupEntity>> associateHostsToHostGroup(
			@RequestParam(PARAM_GROUP_NAME) @NotBlank String hostGroupName,
			@RequestBody @Valid List<TargetEntity> hostTargets) {
		return this.associateTargetsToTargetGroup(hostGroupName, hostTargets, TargetType.HOST);
	}

	/**
	 * Associate targets to a group
	 * @param groupName Group name
	 * @param targets Targets to associate
	 * @param targetType Target type
	 * @return the response of the association
	 */
	private ResponseEntity<List<GroupEntity>> associateTargetsToTargetGroup(String groupName,
			List<TargetEntity> targets, TargetType targetType) {
		LOG.debug("GroupController -> associateTargetsToTargetGroup");

		// Check if there are errors in the inputs
		this.targetService.checkParametersAssociation(groupName, targets, targetType, "created");

		// If no error in the inputs
		// retrieve the group
		TargetEntity groupEntity = this.targetService.retrieveTargetByNameAndType(groupName,
				Objects.equals(TargetType.HOST, targetType) ? TargetType.HOST_GROUP : TargetType.USER_GROUP);
		// retrieve the targets
		List<TargetEntity> targetEntities = targets.stream()
			.map(t -> this.targetService.retrieveTargetByNameAndType(t.getName(), targetType))
			.collect(Collectors.toList());
		// save in database
		List<GroupEntity> groupAssociations = this.groupService.createGroupAssociation(groupEntity, targetEntities);

		LOG.info("Associations for group %s have been created".formatted(groupName));
		return ResponseEntity.ok().body(groupAssociations);
	}

	@Operation(summary = "Delete members of a user group",
			description = SpringDocUtil.descriptionCheckParametersAssociation, tags = "Group")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyUsersToUserGroup)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete has been done",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
							examples = @ExampleObject(value = RESULT_MEMBERS_DELETED))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Delete members of a user group
	 * @param groupName Group name
	 * @param members Members to delete
	 * @return RESULT_MEMBERS_DELETED, otherwise error message
	 */
	@DeleteMapping(value = "/users", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> deleteUsersFromUserGroup(
			@RequestParam(value = PARAM_GROUP_NAME) @NotBlank String groupName,
			@RequestBody @Valid List<TargetEntity> members) {
		return this.deleteMembersFromGroup(groupName, members, TargetType.USER);
	}

	@Operation(summary = "Delete members of a host group",
			description = SpringDocUtil.descriptionCheckParametersAssociation, tags = "Group")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyHostsToHostGroup)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete has been done",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
							examples = @ExampleObject(value = RESULT_MEMBERS_DELETED))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Delete members of a host group
	 * @param groupName Group name
	 * @param members Members to delete
	 * @return RESULT_MEMBERS_DELETED, otherwise error message
	 */
	@DeleteMapping(value = "/hosts", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> deleteHostsFromHostGroup(
			@RequestParam(value = PARAM_GROUP_NAME) @NotBlank String groupName,
			@RequestBody @Valid List<TargetEntity> members) {
		return this.deleteMembersFromGroup(groupName, members, TargetType.HOST);
	}

	/**
	 * Delete members from a group
	 * @param groupName Group name
	 * @param members Members to delete
	 * @param targetType Target type
	 * @return RESULT_MEMBERS_DELETED, otherwise error message
	 */
	private ResponseEntity<String> deleteMembersFromGroup(String groupName, List<TargetEntity> members,
			TargetType targetType) {
		LOG.debug("GroupController -> deleteMembersFromGroup");

		// Check if there are errors in the inputs
		this.targetService.checkParametersAssociation(groupName, members, targetType, "deleted");

		// If no error in the inputs
		// retrieve the group
		TargetEntity groupEntity = this.targetService.retrieveTargetByNameAndType(groupName,
				Objects.equals(TargetType.HOST, targetType) ? TargetType.HOST_GROUP : TargetType.USER_GROUP);
		// retrieve the targets
		List<TargetEntity> targetEntities = members.stream()
			.map(t -> this.targetService.retrieveTargetByNameAndType(t.getName(), targetType))
			.collect(Collectors.toList());
		// delete in database
		this.groupService.deleteMembers(groupEntity, targetEntities);
		LOG.info(RESULT_MEMBERS_DELETED);
		return ResponseEntity.ok().body(RESULT_MEMBERS_DELETED);
	}

	@Operation(summary = "Retrieve user groups", description = "Retrieve targets with TargetType = 'USERGROUP'",
			tags = "Group")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User groups found",
			content = @Content(
					array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
					examples = @ExampleObject(value = SpringDocUtil.exObjValRespTargetsUserGroup))) })
	/**
	 * Retrieve all the user groups
	 * @return all the user groups
	 */
	@GetMapping(value = "/users", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<TargetEntity>> retrieveUserGroups() {
		return this.retrieveGroups(TargetType.USER_GROUP);
	}

	@Operation(summary = "Retrieve host groups", description = "Retrieve targets with TargetType = 'HOSTGROUP'",
			tags = "Group")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Host groups found",
			content = @Content(
					array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
					examples = @ExampleObject(value = SpringDocUtil.exObjValRespTargetsHostGroup))) })
	/**
	 * Retrieve all the host groups
	 * @return all the host groups
	 */
	@GetMapping(value = "/hosts", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<TargetEntity>> retrieveHostGroups() {
		return this.retrieveGroups(TargetType.HOST_GROUP);
	}

	/**
	 * Retrieve the groups by the target type in parameter
	 * @param targetType Target type
	 * @return the groups found
	 */
	private ResponseEntity<List<TargetEntity>> retrieveGroups(TargetType targetType) {
		LOG.debug("GroupController -> retrieveGroups");
		List<TargetEntity> targetEntities = this.targetService.retrieveTargetsByType(targetType);
		LOG.info("Retrieved %s targets".formatted(targetType.getDescription()));
		return ResponseEntity.ok().body(this.targetService.retrieveTargetsByType(targetType));
	}

	@Operation(summary = "Retrieve members of the user group in parameter",
			description = SpringDocUtil.descriptionRetrieveUsersFromUserGroup, tags = "Group")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200",
							description = "User members found for the user group in parameter",
							content = @Content(
									array = @ArraySchema(
											schema = @Schema(type = "array", implementation = TargetEntity.class)),
									examples = @ExampleObject(value = SpringDocUtil.exObjValRespTargetsUsers))),
					@ApiResponse(responseCode = "400", description = "Bad request",
							content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Retrieve user targets for the group in parameter
	 * @return all the user groups
	 */
	@GetMapping(value = "/users/{groupName}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<TargetEntity>> retrieveUsersFromUserGroup(
			@PathVariable(value = PARAM_GROUP_NAME) @NotBlank String groupName) {
		return this.retrieveTargetsFromGroups(groupName, TargetType.USER_GROUP);
	}

	@Operation(summary = "Retrieve members of the host group in parameter",
			description = SpringDocUtil.descriptionRetrieveHostsFromHostGroup, tags = "Group")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200",
							description = "Host members found for the host group in parameter",
							content = @Content(
									array = @ArraySchema(
											schema = @Schema(type = "array", implementation = TargetEntity.class)),
									examples = @ExampleObject(value = SpringDocUtil.exObjValRespTargetsHosts))),
					@ApiResponse(responseCode = "400", description = "Bad request",
							content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Retrieve host targets for the group in parameter
	 * @return all the host groups
	 */
	@GetMapping(value = "/hosts/{groupName}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<TargetEntity>> retrieveHostsFromHostGroup(
			@PathVariable(value = PARAM_GROUP_NAME) @NotBlank String groupName) {
		return this.retrieveTargetsFromGroups(groupName, TargetType.HOST_GROUP);
	}

	/**
	 * Retrieve targets from a group
	 * @param groupName Group Name
	 * @param targetType Target Type
	 * @return the targets found for a group
	 */
	private ResponseEntity<List<TargetEntity>> retrieveTargetsFromGroups(String groupName, TargetType targetType) {
		LOG.debug("GroupController -> retrieveTargetsFromGroups");
		ResponseEntity<List<TargetEntity>> response;

		// retrieve the group
		TargetEntity group = this.targetService.retrieveTargetByNameAndType(groupName, targetType);
		if (group == null) {
			// case group name not found
			throw new NotFoundException(
					String.format("Group name %s with type %s not found", groupName, targetType.getCode()));
		}
		else {
			// retrieve targets from the group
			List<TargetEntity> targetEntities = this.targetService
				.retrieveTargetsByIds(this.groupService.retrieveGroupsByGroup(group)
					.stream()
					.map(g -> g.getGroupEntityPK().getMemberId())
					.collect(Collectors.toList()));
			LOG.info("Retrieved targets from group %s".formatted(groupName));
			response = ResponseEntity.ok().body(targetEntities);
		}

		return response;
	}

}
