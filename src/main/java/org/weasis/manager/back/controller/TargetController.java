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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.controller.exception.ParameterException;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.ErrorMessage;
import org.weasis.manager.back.service.GroupService;
import org.weasis.manager.back.service.LaunchPreferenceService;
import org.weasis.manager.back.service.TargetService;
import org.weasis.manager.back.util.SpringDocUtil;

import java.util.List;
import java.util.Objects;

/**
 * End points for targets
 */
@RestController
@RequestMapping(EndPoint.TARGET_PATH)
@Tag(name = "Target", description = "API Endpoints for Target")
@Validated
@Slf4j
public class TargetController {

	// Params
	private static final String PARAM_TARGET_NAME = "targetName";

	// Result
	private static final String RESULT_GROUP_ASSOCIATION_TARGET_DELETED = "Group associations and target deleted";

	private static final String RESULT_DELETE_FAIL_LAUNCH_STILL_ASSOCIATED = "Delete not done: a launch is associated to the target. Please remove launch before deleting target.";

	// Services
	private final TargetService targetService;

	private final LaunchPreferenceService launchPreferenceService;

	private final GroupService groupService;

	/**
	 * Autowired constructor
	 * @param targetService Target service
	 * @param launchPreferenceService Launch Preference service
	 * @param groupService Group service
	 */
	@Autowired
	public TargetController(final TargetService targetService, final LaunchPreferenceService launchPreferenceService,
			final GroupService groupService) {
		this.targetService = targetService;
		this.launchPreferenceService = launchPreferenceService;
		this.groupService = groupService;
	}

	@Operation(summary = "Create targets", description = SpringDocUtil.descriptionCreateTarget, tags = "Target")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyTargets)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Creation has been done",
					content = @Content(
							array = @ArraySchema(schema = @Schema(type = "array", implementation = TargetEntity.class)),
							examples = @ExampleObject(value = SpringDocUtil.exObjValRespTargets))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Create targets: take the json body of the request which corresponds to the list of
	 * target entities
	 * <p>
	 * Example: <pre>
	 * [
	 *     {
	 *         "name":"name of the target 1",
	 *         "type":"HOST"
	 *     },
	 *     {
	 *         "name":"name of the target 2",
	 *         "type":"HOSTGROUP"
	 *     }
	 * ]
	 * </pre>
	 * @param targets Targets to create with name: name of the target, type: code of the
	 * enum TargetType (HOST, HOSTGROUP, USER, USERGROUP)
	 * @return Created targets with theirs created ids
	 */
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<TargetEntity>> createTarget(@RequestBody List<@Valid TargetEntity> targets) {
		LOG.debug("TargetController -> createTarget");

		ResponseEntity<List<TargetEntity>> response;
		if (targets.isEmpty() || targets.stream().anyMatch((t) -> t.getName() == null || t.getType() == null)) {
			// case there is an error in the request body or empty list
			throw new ParameterException("Targets not created: wrong parameters");
		}
		else {
			try {
				// save in database
				response = ResponseEntity.ok().body(this.targetService.createTargets(targets));
			}
			catch (DataIntegrityViolationException e) {
				// case one of the target is already saved in database
				throw new ParameterException(
						"Targets not created: name of the target is empty or is already saved in database => %s"
							.formatted(e.getMessage()));
			}
		}
		return response;
	}

	@Operation(summary = "Delete selected target", description = SpringDocUtil.descriptionDeleteTarget, tags = "Target")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete has been done",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
							examples = @ExampleObject(value = RESULT_GROUP_ASSOCIATION_TARGET_DELETED))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Delete selected target:
	 * <p>
	 * - check target name has been filled - check target exist - check target not
	 * associated to a launch entity
	 * <p>
	 * - delete group associations - delete target
	 * @param targetName target name to delete
	 * @return RESULT_GROUP_ASSOCIATION_TARGET_DELETED if ok or error message otherwise
	 */
	@DeleteMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> deleteTarget(@RequestParam(PARAM_TARGET_NAME) String targetName) {
		LOG.debug("TargetController -> deleteTarget");

		// Check target name: exists and not associated to a launch entity
		this.targetService.checkParametersDeleteTarget(targetName);

		// Check target not associated to a launch entity
		if (this.launchPreferenceService.hasLaunchWithTargetName(targetName)) {
			throw new ParameterException(RESULT_DELETE_FAIL_LAUNCH_STILL_ASSOCIATED);
		}

		// If no error in the input
		// Retrieve the target to delete
		TargetEntity target = this.targetService.retrieveTargetByName(targetName);
		// Delete group associations
		if (Objects.equals(TargetType.USER_GROUP, target.getType())
				|| Objects.equals(TargetType.HOST_GROUP, target.getType())) {
			this.groupService.deleteGroupAssociation(target);
		}
		else {
			this.groupService.deleteMemberAssociation(target);
		}
		// Delete target
		this.targetService.deleteTarget(target);
		// Ok response
		ResponseEntity<String> response = ResponseEntity.ok().body(RESULT_GROUP_ASSOCIATION_TARGET_DELETED);
		LOG.info(RESULT_GROUP_ASSOCIATION_TARGET_DELETED);

		return response;
	}

}
