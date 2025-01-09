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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.controller.exception.ParameterException;
import org.weasis.manager.back.model.WeasisModules;
import org.weasis.manager.back.service.ModuleService;

import java.sql.SQLException;

/**
 * Resource class for Weasis Modules
 */
@RestController
@RequestMapping(EndPoint.MODULES_PATH)
@Tag(name = "Weasis Module", description = "API Endpoints for Weasis Module")
@Validated
@Slf4j
public class ModuleController {

	// Services
	private final ModuleService moduleService;

	// Request params
	private static final String PARAM_USER = "user";

	private static final String PARAM_PROFILE = "profile";

	@Autowired
	public ModuleController(final ModuleService moduleService) {
		this.moduleService = moduleService;
	}

	@Operation(summary = "Returns the list of all Weasis Modules found for a User and a Weasis Profile",
			description = "Returns the list of all Weasis Modules found for a User and a Weasis Profile",
			tags = "Weasis Module")
	/**
	 * Returns the list of all Weasis Modules found for a User and a Weasis Profile
	 * @param user User identifier
	 * @param profile Weasis profile
	 * @return the list of all Weasis Modules found for a User and a Weasis Profile
	 */
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<WeasisModules> getWeasisModules(
			@RequestParam(value = PARAM_USER, required = false) String user,
			@RequestParam(value = PARAM_PROFILE, required = false) String profile) throws SQLException {
		LOG.debug("getWeasisModules");

		// Check the validity of the request
		boolean userIsEmpty = user == null || user.trim().isEmpty();
		boolean profileIsEmpty = profile == null || profile.trim().isEmpty();

		if (userIsEmpty && profileIsEmpty) {
			throw new ParameterException("User and profile empty");
		}

		WeasisModules weasisModules = new WeasisModules(this.moduleService.readWeasisModules(user, profile));
		ResponseEntity<WeasisModules> response = weasisModules.getWeasisModules().isEmpty()
				? ResponseEntity.noContent().build() : ResponseEntity.ok(weasisModules);

		LOG.info("Modules have been retrieved");
		return response;
	}

}
