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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import org.weasis.manager.back.controller.exception.NoContentException;
import org.weasis.manager.back.controller.exception.ParameterException;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.LaunchPreferredEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.ErrorMessage;
import org.weasis.manager.back.model.Launches;
import org.weasis.manager.back.service.LaunchPreferenceService;
import org.weasis.manager.back.service.TargetService;
import org.weasis.manager.back.util.InetUtil;
import org.weasis.manager.back.util.SpringDocUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Controller class for Weasis Preferred launch Protocol : weasis:// OR jnlp://
 */
@RestController
@RequestMapping(EndPoint.PREFERENCES_PATH)
@Tag(name = "Launch Preference", description = "API Endpoints for Launch Preference")
@Validated
@Slf4j
public class LaunchPreferenceController {

	static final String VIEWER_LAUNCH = "viewer";
	static final String JNLP_LAUNCH = "jnlp";
	static final String WEASIS_LAUNCH = "weasis";

	static final String PARAM_USER = "user";
	static final String PARAM_HOST = "host";

	private static final String PARAM_CONFIG = "config";

	private static final String PARAM_LAUNCH_CONFIG_NAME = "launchConfigName";

	private static final String PARAM_LAUNCH_PREFERED_NAME = "launchPreferedName";

	private static final String PARAM_GROUP_NAME = "groupName";

	static final String HOST_PARAM_BYPASS_VALUE = "unknown"; // when equals hostParam then
																// host
	static final String DEFAULT_CONFIG = "default";

	private static final String PARAM_PREFERED_TYPE = "preferedType";

	private final String REQUEST_ALL = "all";

	private static final UnaryOperator<String> hostWithoutPrefix = s -> s.replaceFirst("^(?i)host_", "");

	private static final Function<String, Optional<String>> optionalValidParam = str -> Optional.ofNullable(str)
		.map(String::trim)
		.filter(s -> !(s.isEmpty()))
		.map(String::toLowerCase);

	// Result
	private static final String RESULT_DELETED = "deleted";

	// Services
	private final LaunchPreferenceService launchPreferenceService;

	private final TargetService targetService;

	/**
	 * Autowired constructor
	 * @param launchPreferenceService Launch Preference Service
	 * @param targetService Target Service
	 */
	@Autowired
	public LaunchPreferenceController(LaunchPreferenceService launchPreferenceService, TargetService targetService) {
		this.launchPreferenceService = launchPreferenceService;
		this.targetService = targetService;
	}

	@Operation(summary = "Retrieve all the Launches depending on user/host/config",
			description = SpringDocUtil.descriptionLaunchesByUserHostConfig, tags = "Launch Preference")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Retrieve the Launches by requested prefered",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = Launches.class)),
							examples = @ExampleObject(
									value = SpringDocUtil.exObjValRespGetLaunchesByRequestedPrefered))),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Retrieve the Launches depending on user/host/config or for config default if no
	 * config requested
	 * <p>
	 * - No filter on Prefered type: all by default
	 * @param userParam User
	 * @param hostParam Host
	 * @param configParam Config
	 * @param request HttpRequest
	 * @return the launches found
	 */
	@GetMapping(value = { "/launch" }, produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Launches> retrieveLaunches(
			@RequestParam(value = PARAM_USER, required = false) String userParam,
			@RequestParam(value = PARAM_HOST, required = false) String hostParam,
			@RequestParam(value = PARAM_CONFIG, required = false) String configParam, HttpServletRequest request) {
		return this.retrieveLaunchesByUserHostConfigPreferedType(this.REQUEST_ALL, userParam, hostParam, configParam,
				request);
	}

	@Operation(summary = "Retrieve the Launches depending on user/host/config/preferedType",
			description = SpringDocUtil.descriptionLaunchesByUserHostConfigPrefered, tags = "Launch Preference")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Retrieve the Launches by requested prefered",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = Launches.class)),
							examples = @ExampleObject(
									value = SpringDocUtil.exObjValRespGetLaunchesByRequestedPrefered))),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Retrieve the Launches by requested prefered: retrieve the launches for the config
	 * in parameter or for config default if no config requested
	 * <p>
	 * - Prefered type: all => retrieve all the prefered - Prefered type: specific
	 * prefered type => retrieve the specific prefered
	 * @param preferedTypeParam Prefered type
	 * @param userParam User
	 * @param hostParam Host
	 * @param configParam Config
	 * @param request HttpRequest
	 * @return the launches found
	 */
	@GetMapping(value = { "/launch/{preferedType}" },
			produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Launches> retrieveLaunchesByRequestedPrefered(
			@PathVariable(value = PARAM_PREFERED_TYPE) String preferedTypeParam,
			@RequestParam(value = PARAM_USER, required = false) String userParam,
			@RequestParam(value = PARAM_HOST, required = false) String hostParam,
			@RequestParam(value = PARAM_CONFIG, required = false) String configParam, HttpServletRequest request) {
		return this.retrieveLaunchesByUserHostConfigPreferedType(preferedTypeParam, userParam, hostParam, configParam,
				request);
	}

	/**
	 * Retrieve the Launches by requested prefered: retrieve the launches for the config
	 * in parameter or for config default if no config requested
	 * <p>
	 * - Prefered type: all => retrieve all the prefered - Prefered type: specific
	 * prefered type => retrieve the specific prefered
	 * @param preferedTypeParam Prefered type
	 * @param userParam User
	 * @param hostParam Host
	 * @param configParam Config
	 * @param request HttpRequest
	 * @return the launches found
	 */
	private ResponseEntity<Launches> retrieveLaunchesByUserHostConfigPreferedType(String preferedTypeParam,
			String userParam, String hostParam, String configParam, HttpServletRequest request) {
		LOG.debug("retrieveLaunchesByUserHostConfigPreferedType");
		ResponseEntity<Launches> responseEntity;

		// Get params user / host / config (for config if not existing: we take default
		// config)
		String user = optionalValidParam.apply(userParam).orElse(null);
		String host = InetUtil.getClientHost(request, optionalValidParam.apply(hostParam).map(hostWithoutPrefix),
				HOST_PARAM_BYPASS_VALUE);
		String launchConfig = optionalValidParam.apply(configParam).orElse(DEFAULT_CONFIG);
		// Prefered type corresponding to the specific prefered type requested or if
		// "all": all prefered types
		// corresponding to the config requested
		String preferedType = optionalValidParam.apply(preferedTypeParam).orElse(this.REQUEST_ALL);

		if (this.checkCaseUserHostNotPresentInDb(user, host)) {
			// Case user or host not existing in database
			throw new NoContentException("User/Host not existing in db");
		}
		else if (host == null && user == null) {
			// Case no user and host has not be resolved
			throw new ParameterException("User is null and host has not been resolved");
		}
		else {
			// retrieve the launches: case all => param null, param preferedType
			// otherwise
			List<LaunchEntity> launches = this.launchPreferenceService
				.retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets(host, user, launchConfig,
						Objects.equals(this.REQUEST_ALL, preferedType) ? null : preferedType);
			LOG.info("LaunchesByUserHostConfigPreferedType: launches have been retrieved");
			responseEntity = ResponseEntity.status(HttpStatus.OK).body(new Launches(launches));
		}

		return responseEntity;
	}

	@Operation(summary = "Retrieve the Launches for a group", description = SpringDocUtil.descriptionLaunchesForAGroup,
			tags = "Launch Preference")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Retrieve the Launches for a group",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = Launches.class)),
							examples = @ExampleObject(value = SpringDocUtil.exObjValRespGetGroupLaunches))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Retrieve the group launches
	 * @param groupNameParam Group name
	 * @param configParam Launch Config name
	 * @return launches found for the group
	 */
	@GetMapping(value = { "/launch/group/{groupName}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchEntity>> retrieveGroupLaunches(
			@PathVariable(value = PARAM_GROUP_NAME) @NotBlank(message = "Group name missing") String groupNameParam,
			@RequestParam(value = PARAM_CONFIG, required = false) String configParam) {
		LOG.debug("retrieveGroupLaunches");
		ResponseEntity<List<LaunchEntity>> responseEntity;

		// Get param config
		String launchConfigName = optionalValidParam.apply(configParam).orElse(null);

		// retrieve the group
		TargetEntity group = this.targetService.retrieveTargetByName(groupNameParam);
		if (group == null || (Objects.equals(group.getType(), TargetType.HOST)
				|| (Objects.equals(group.getType(), TargetType.USER)))) {
			// Group is not existing or group name is not a group
			throw new ParameterException("Group is not existing or group name is not a group");
		}
		else {
			// Retrieve the launches for the group
			LOG.info("Group launches for group %s and config %s have been retrieved".formatted(groupNameParam,
					configParam));
			responseEntity = ResponseEntity.status(HttpStatus.OK)
				.body(this.launchPreferenceService.retrieveGroupLaunches(group, launchConfigName));
		}

		return responseEntity;
	}

	@Operation(summary = "Retrieve all the launch prefered", description = "Retrieve all the launch prefered",
			tags = "Launch Preference")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retrieve all the launch prefered",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LaunchPreferredEntity.class)),
					examples = @ExampleObject(value = SpringDocUtil.exObjValRespLaunchPrefered))) })
	/**
	 * Retrieve all the launch prefered
	 * @return launch prefered found
	 */
	@GetMapping(value = { "/launch_prefered" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchPreferredEntity>> retrieveLaunchPrefered() {
		LOG.debug("retrieveLaunchPrefered");
		return ResponseEntity.ok(this.launchPreferenceService.retrieveLaunchPrefered(null));
	}

	@Operation(summary = "Retrieve launch prefered filtered by the prefered type in parameter",
			description = "Retrieve launch prefered filtered by the prefered type in parameter. Check that the prefered type has been filled and exists",
			tags = "Launch Preference")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Retrieve launch prefered filtered by the prefered type in parameter",
					content = @Content(
							array = @ArraySchema(schema = @Schema(implementation = LaunchPreferredEntity.class)),
							examples = @ExampleObject(value = SpringDocUtil.exObjValRespLaunchPreferedSpecific))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Retrieve the launch prefered depending on the prefered type in parameter
	 * @param preferedTypeParam Prefered type
	 * @return launch prefered found
	 */
	@GetMapping(value = { "/launch_prefered/{preferedType}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchPreferredEntity>> retrieveLaunchPreferedByPreferedType(
			@PathVariable(value = PARAM_PREFERED_TYPE) String preferedTypeParam) {
		LOG.debug("retrieveLaunchPreferedByPreferedType");
		ResponseEntity<List<LaunchPreferredEntity>> responseEntity;
		if (preferedTypeParam == null || Objects.equals(preferedTypeParam.trim(), "")
				|| !this.launchPreferenceService.existLaunchPreferedPreferedType(preferedTypeParam)) {
			// Case prefered type not filled or not existing
			throw new ParameterException("Prefered type not filled or not existing");
		}
		else {
			LOG.info("Retrieved launch preferred with preferred type %s".formatted(preferedTypeParam));
			responseEntity = ResponseEntity.ok(this.launchPreferenceService.retrieveLaunchPrefered(preferedTypeParam));
		}
		return responseEntity;
	}

	@Operation(summary = "Retrieve all the launch config", description = "Retrieve all the launch config",
			tags = "Launch Preference")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retrieve all the launch config",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LaunchConfigEntity.class)),
					examples = @ExampleObject(value = SpringDocUtil.exObjValRespLaunchConfig))) })
	/**
	 * Retrieve all the launch config
	 * @return launch config found
	 */
	@GetMapping(value = { "/launch_config" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchConfigEntity>> retrieveLaunchConfigs() {
		LOG.debug("retrieveLaunchConfigs");
		List<LaunchConfigEntity> launchConfigEntities = this.launchPreferenceService.retrieveLaunchConfig();
		LOG.info("Launch configs have been retrieved");
		return ResponseEntity.ok(launchConfigEntities);
	}

	/**
	 * Check case where user/host not present in database
	 * @param user User
	 * @param host Host
	 * @return result of the check
	 */
	private boolean checkCaseUserHostNotPresentInDb(String user, String host) {
		return (user != null && this.targetService.retrieveTargetByNameAndType(user, TargetType.USER) == null
				&& host == null)
				|| (host != null && this.targetService.retrieveTargetByNameAndType(host, TargetType.HOST) == null
						&& user == null)
				|| (user != null && this.targetService.retrieveTargetByNameAndType(user, TargetType.USER) == null
						&& host != null
						&& this.targetService.retrieveTargetByNameAndType(host, TargetType.HOST) == null);
	}

	@Operation(summary = "Create launches", description = SpringDocUtil.descriptionCreateLaunch,
			tags = "Launch Preference")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = LaunchEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyCreateLaunch)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Creation has been done",
					content = @Content(
							array = @ArraySchema(schema = @Schema(type = "array", implementation = LaunchEntity.class)),
							examples = @ExampleObject(value = SpringDocUtil.exObjValRespCreateLaunch))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Create launches: take the json body of the request which corresponds to the list of
	 * launch entities
	 * <p>
	 * => LaunchConfig/LaunchPrefered/Target should already exists in order to create the
	 * launch, otherwise an error message will appear to warn the user to create the
	 * corresponding entity => Value can have value " " but not "" : database constraint
	 * not null on value column
	 * <p>
	 * Example: <pre>
	 * [
	 *     {
	 *         "config": {
	 *             "name":"name config 1"
	 *         },
	 *         "prefered": {
	 *             "name":"name prefered 1"
	 *         },
	 *         "target": {
	 *             "name":"name target 1"
	 *         },
	 *         "value":"value launch 1"
	 *     },
	 *     {
	 *         "config": {
	 *             "name":"name config 2"
	 *         },
	 *         "prefered": {
	 *             "name":"name prefered 2"
	 *         },
	 *         "target": {
	 *             "name":"name target 2"
	 *         },
	 *         "value":"value launch 2"
	 *     }
	 * ]
	 * </pre>
	 * @param launches Launches to create
	 * @return Created launches with theirs created ids
	 */
	@PostMapping(value = "/launch", consumes = { MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchEntity>> createLaunchPreference(@RequestBody List<LaunchEntity> launches) {
		LOG.debug("LaunchPreferenceController -> createLaunchPreference");
		// Check if there are errors in the inputs
		this.launchPreferenceService.checkParametersLaunches(launches, "created");

		// If no error in the inputs
		List<LaunchEntity> launchEntities = this.launchPreferenceService.createLaunches(launches);
		LOG.info("Launch preference(s) have been created");
		return ResponseEntity.ok().body(launchEntities);
	}

	@Operation(summary = "Create launch configs", description = SpringDocUtil.descriptionCreateLaunchConfig,
			tags = "Launch Preference")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = LaunchConfigEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyCreateLaunchConfig)))
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Creation has been done",
							content = @Content(array = @ArraySchema(
									schema = @Schema(type = "array", implementation = LaunchConfigEntity.class)),
									examples = @ExampleObject(value = SpringDocUtil.exObjValRespLaunchConfig))),
					@ApiResponse(responseCode = "400", description = "Bad request",
							content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Create launch configs: take the json body of the request which corresponds to the
	 * list of launch config entities
	 * <p>
	 * Example: <pre>
	 * [
	 *     {
	 *         "name":"name of the config 1"
	 *     },
	 *     {
	 *         "name":"name of the config 2"
	 *     }
	 * ]
	 * </pre>
	 * @param configs Launch Configs to create with name: name of the config
	 * @return Created configs with theirs created ids
	 */
	@PostMapping(value = "/launch_config", consumes = { MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchConfigEntity>> createLaunchConfig(
			@RequestBody @NotNull @Valid List<LaunchConfigEntity> configs) {
		LOG.debug("LaunchPreferenceController -> createLaunchConfig");
		ResponseEntity<List<LaunchConfigEntity>> response;
		if (configs.isEmpty() || configs.stream().anyMatch(c -> c.getName() == null)) {
			// case there is an error in the request body or empty list
			throw new ParameterException("Configs not created: wrong parameters");
		}
		else {
			try {
				// save in database
				response = ResponseEntity.ok().body(this.launchPreferenceService.createLaunchConfigs(configs));
				LOG.info("Launch config(s) have been created");
			}
			catch (DataIntegrityViolationException e) {
				// case one of the config is already saved in database
				throw new ParameterException(
						"Configs not created: name of the config is empty or is already saved in database => %s"
							.formatted(e.getMessage()));
			}
		}
		return response;
	}

	@Operation(summary = "Create prefered", description = SpringDocUtil.descriptionCreateLaunchPrefered,
			tags = "Launch Preference")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = LaunchPreferredEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyCreateLaunchPrefered)))
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Creation has been done",
							content = @Content(array = @ArraySchema(
									schema = @Schema(type = "array", implementation = LaunchPreferredEntity.class)),
									examples = @ExampleObject(value = SpringDocUtil.exObjValRespLaunchPrefered))),
					@ApiResponse(responseCode = "400", description = "Bad request",
							content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Create prefered: take the json body of the request which corresponds to the list of
	 * prefered entities
	 * <p>
	 * Example: <pre>
	 * [
	 *     {
	 *         "name":"name of the prefered 1",
	 *         "type":"ext-cfg"
	 *     },
	 *     {
	 *         "name":"name of the prefered 2",
	 *         "type":"pro"
	 *     }
	 * ]
	 * </pre>
	 * @param prefered Prefered to create with name: name of the prefered, type: code of
	 * the prefered (pro, arg, ext-cfg, launch, ver, svr, etc..)
	 * @return Created prefered with theirs created ids
	 */
	@PostMapping(value = "/launch_prefered", consumes = { MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LaunchPreferredEntity>> createLaunchPrefered(
			@RequestBody @Valid List<LaunchPreferredEntity> prefered) {
		LOG.debug("LaunchPreferenceController -> createLaunchPrefered");
		ResponseEntity<List<LaunchPreferredEntity>> response;

		try {
			// save in database
			response = ResponseEntity.ok().body(this.launchPreferenceService.createLaunchPrefered(prefered));
			LOG.info("Launch preferred have been created");
		}
		catch (DataIntegrityViolationException e) {
			// case one of the prefered is already saved in database
			throw new ParameterException(
					"Prefered not created: name of the prefered is empty or is already saved in database => %s"
						.formatted(e.getMessage()));
		}

		return response;
	}

	@Operation(summary = "Delete launches", description = SpringDocUtil.descriptionDeleteLaunch,
			tags = "Launch Preference")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			array = @ArraySchema(schema = @Schema(type = "array", implementation = LaunchEntity.class)),
			examples = @ExampleObject(value = SpringDocUtil.exObjValReqBodyDeleteLaunch)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete has been done",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
							examples = @ExampleObject(value = RESULT_DELETED))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Delete launches: take the json body of the request which corresponds to the list of
	 * launch entities
	 * <p>
	 * [ { "config": { "name":"name config 1" }, "prefered": { "name":"name prefered 1" },
	 * "target": { "name":"name target 1" } }, { "config": { "name":"name config 2" },
	 * "prefered": { "name":"name prefered 2" }, "target": { "name":"name target 2" } } ]
	 * </pre>
	 * @param launches Launches to delete
	 * @return RESULT_DELETED if ok or error otherwise
	 */
	@DeleteMapping(value = "/launch", consumes = { MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> deleteLaunches(@RequestBody List<LaunchEntity> launches) {
		LOG.debug("LaunchPreferenceController -> deleteLaunches");

		// Check if there are errors in the inputs
		this.launchPreferenceService.checkParametersDeleteLaunches(launches);

		// If no error in the inputs: delete launches in database
		String body = this.launchPreferenceService.deleteLaunches(launches);

		LOG.info("Launches have been deleted");
		return ResponseEntity.ok().body(body);
	}

	@Operation(summary = "Delete a launch config", description = SpringDocUtil.descriptionDeleteLaunchConfig,
			tags = "Launch Preference")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete has been done",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
							examples = @ExampleObject(value = RESULT_DELETED))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Delete a launch config
	 * <p>
	 * - check name not empty - check selected launch config exist - check there is no
	 * launch associated to this launch config
	 * @param launchConfigName Launch Config name
	 * @return RESULT_DELETED if no error, error message otherwise
	 */
	@DeleteMapping(value = "/launch_config", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> deleteLaunchConfig(
			@RequestParam(value = PARAM_LAUNCH_CONFIG_NAME) String launchConfigName) {
		LOG.debug("LaunchPreferenceController -> deleteLaunchConfig");

		// Check if there are errors in the inputs
		this.launchPreferenceService.checkParameterDeleteLaunchConfig(launchConfigName);

		// If no error in the inputs
		// delete launch config in database
		this.launchPreferenceService.deleteLaunchConfig(launchConfigName);
		LOG.info(RESULT_DELETED);
		return ResponseEntity.ok().body(RESULT_DELETED);
	}

	@Operation(summary = "Delete a launch prefered", description = SpringDocUtil.descriptionDeleteLaunchPrefered,
			tags = "Launch Preference")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete has been done",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
							examples = @ExampleObject(value = RESULT_DELETED))),
			@ApiResponse(responseCode = "400", description = "Bad request",
					content = @Content(schema = @Schema(implementation = ErrorMessage.class))) })
	/**
	 * Delete a launch prefered
	 * <p>
	 * - check name not empty - check selected launch prefered exist - check there is no
	 * launch associated to this launch prefered
	 * @param launchPreferedName Launch Prefered name
	 * @return RESULT_DELETED if no error, error message otherwise
	 */
	@DeleteMapping(value = "/launch_prefered", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> deleteLaunchPrefered(
			@RequestParam(value = PARAM_LAUNCH_PREFERED_NAME) String launchPreferedName) {
		LOG.debug("LaunchPreferenceController -> deleteLaunchPrefered");

		// Check if there are errors in the inputs
		this.launchPreferenceService.checkParameterDeleteLaunchPrefered(launchPreferedName);

		// If no error in the inputs
		// delete launch prefered in database
		this.launchPreferenceService.deleteLaunchPrefered(launchPreferedName);
		LOG.info(RESULT_DELETED);
		return ResponseEntity.ok().body(RESULT_DELETED);
	}

}
