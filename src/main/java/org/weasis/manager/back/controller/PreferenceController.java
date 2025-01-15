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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.controller.exception.ParameterException;
import org.weasis.manager.back.enums.OperationType;
import org.weasis.manager.back.service.ApplicationPreferenceService;

import java.sql.SQLException;

/**
 * Resource class for Weasis Preferences (application and modules)
 */
@RestController
@RequestMapping(EndPoint.PREFERENCES_PATH)
@Tag(name = "Weasis Preference", description = "API Endpoints for Weasis Preference")
@Validated
@Slf4j
public class PreferenceController {

	// Services
	private final ApplicationPreferenceService applicationPreferenceService;

	// Request params
	private static final String PARAM_USER = "user";

	private static final String PARAM_PROFILE = "profile";

	private static final String PARAM_MODULE = "module";

	@Autowired
	public PreferenceController(final ApplicationPreferenceService applicationPreferenceService) {
		this.applicationPreferenceService = applicationPreferenceService;
	}

	@Operation(
			summary = "Returns the Weasis application preferences for a given user as well as for a given Weasis profile",
			description = "Returns the Weasis application preferences for a given user as well as for a given Weasis profile",
			tags = "Weasis Preference")
	/**
	 * Returns the Weasis application preferences for a given user as well as for a given
	 * Weasis profile
	 * @param user User identifier
	 * @param profile Weasis profile
	 * @return the Weasis application preferences for a given user as well as for a given
	 * Weasis profile
	 */
	@GetMapping(produces = { "text/x-java-properties" })
	public ResponseEntity<String> getWeasisPreferences(@RequestParam(value = PARAM_USER, required = false) String user,
			@RequestParam(value = PARAM_PROFILE, required = false) String profile) throws SQLException {
		LOG.debug("getWeasisPreferences");
		ResponseEntity<String> response;

		// Check the validity of the request
		if ((user == null || user.trim().isEmpty()) && (profile == null || profile.trim().isEmpty())) {
			throw new ParameterException("User and profile empty");
		}

		String preferences = this.applicationPreferenceService.readWeasisPreferences(user, profile, null, false);
		response = preferences == null || preferences.isEmpty() ? ResponseEntity.noContent().build()
				: ResponseEntity.ok(preferences);

		LOG.info("Preferences for user %s, profile %s have been retrieved:\n%s".formatted(user, profile, preferences));
		return response;
	}

	@Operation(
			summary = "Returns the Weasis module preferences for a given user as well as for a given Weasis profile and a Weasis module",
			description = "Returns the Weasis module preferences for a given user as well as for a given Weasis profile and a Weasis module",
			tags = "Weasis Preference")
	/**
	 * Returns the Weasis module preferences for a given user as well as for a given
	 * Weasis profile and a Weasis module
	 * @param user User identifier
	 * @param profile Weasis profile
	 * @param module Weasis module
	 * @return the Weasis module preferences for a given user as well as for a given
	 * Weasis profile and a Weasis module
	 */
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<String> getWeasisPreferences(@RequestParam(value = PARAM_USER, required = false) String user,
			@RequestParam(value = PARAM_PROFILE, required = false) String profile,
			@RequestParam(value = PARAM_MODULE, required = false) String module) throws SQLException {
		LOG.debug("getWeasisPreferences");

		// Check the validity of the request
		boolean userIsEmpty = user == null || user.trim().isEmpty();
		boolean profileIsEmpty = profile == null || profile.trim().isEmpty();
		boolean moduleIsEmpty = module == null || module.trim().isEmpty();

		if (userIsEmpty && profileIsEmpty && moduleIsEmpty) {
			throw new ParameterException("User, profile and module are empty");
		}

		String preferences = this.applicationPreferenceService.readWeasisPreferences(user, profile, module, true);
		ResponseEntity<String> response = preferences == null || preferences.isEmpty()
				? ResponseEntity.noContent().build()
				: ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(preferences);

		LOG.info("Preferences for user %s, profile %s, module %s have been retrieved:\n%s".formatted(user, profile,
				module, preferences));
		return response;
	}

	@Operation(
			summary = "Updates the Weasis application preferences for a given user as well as for a given Weasis profile",
			description = "Updates the Weasis application preferences for a given user as well as for a given Weasis profile",
			tags = "Weasis Preference")
	/**
	 * Updates the Weasis application preferences for a given user as well as for a given
	 * Weasis profile
	 * @param user User identifier
	 * @param profile Weasis Profile
	 * @param is Weasis application preferences to store
	 * @return ResponseEntity<String>
	 */
	@PostMapping(consumes = { "text/x-java-properties" })
	public ResponseEntity<String> updateWeasisPreferences(
			@RequestParam(value = PARAM_USER, required = false) String user,
			@RequestParam(value = PARAM_PROFILE, required = false) String profile,
			@RequestBody @NotBlank String preferences) throws SQLException {
		LOG.debug("updateWeasisPreferences");
		ResponseEntity<String> response = null;

		// Check the validity of the request
		boolean userIsEmpty = user == null || user.trim().isEmpty();
		boolean profileIsEmpty = profile == null || profile.trim().isEmpty();

		if (userIsEmpty && profileIsEmpty) {
			throw new ParameterException("User and profile empty");
		}

		OperationType operationType = this.applicationPreferenceService.updateWeasisPreferences(user, profile, null,
				preferences);
		if (operationType == OperationType.CREATION) {
			response = ResponseEntity.status(HttpStatus.CREATED).build();
			LOG.info("Preferences for user %s and profile %s have been created".formatted(user, profile));
		}
		else if (operationType == OperationType.UPDATE) {
			response = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			LOG.info("Preferences for user %s and profile %s have been updated".formatted(user, profile));
		}

		return response;
	}

	@Operation(
			summary = "Updates a Weasis module preferences for a given user as well as for a given Weasis profile and a Weasis module",
			description = "Updates a Weasis module preferences for a given user as well as for a given Weasis profile and a Weasis module",
			tags = "Weasis Preference")
	/**
	 * Updates a Weasis module preferences for a given user as well as for a given Weasis
	 * profile and a Weasis module
	 * @param user User identifier
	 * @param profile Weasis Profile
	 * @param module Weasis Module
	 * @param is Weasis module preferences to store
	 * @return ResponseEntity<String>
	 */
	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<String> updateWeasisPreferences(
			@RequestParam(value = PARAM_USER, required = false) String user,
			@RequestParam(value = PARAM_PROFILE, required = false) String profile,
			@RequestParam(value = PARAM_MODULE, required = false) String module, @RequestBody String preferences)
			throws SQLException {
		LOG.debug("updateWeasisPreferences");
		ResponseEntity<String> response = null;

		// Check the validity of the request
		boolean userIsEmpty = user == null || user.trim().isEmpty();
		boolean profileIsEmpty = profile == null || profile.trim().isEmpty();
		boolean moduleIsEmpty = module == null || module.trim().isEmpty();

		if (userIsEmpty && profileIsEmpty && moduleIsEmpty) {
			throw new ParameterException("User, profile and module are empty");
		}

		OperationType operationType = this.applicationPreferenceService.updateWeasisPreferences(user, profile, module,
				preferences);
		if (operationType == OperationType.CREATION) {
			response = ResponseEntity.status(HttpStatus.CREATED).build();
			LOG.info("Preferences for user %s,profile %s and module %s have been created".formatted(user, profile,
					module));
		}
		else if (operationType == OperationType.UPDATE) {
			response = ResponseEntity.noContent().build();
			LOG.info("Preferences for user %s,profile %s and module %s have been updated".formatted(user, profile,
					module));
		}

		return response;
	}

}
