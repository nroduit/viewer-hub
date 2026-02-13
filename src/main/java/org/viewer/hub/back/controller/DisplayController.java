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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.controller.exception.ConstraintException;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.CryptographyService;
import org.viewer.hub.back.service.DisplayService;
import org.viewer.hub.back.util.InetUtil;
import org.viewer.hub.back.util.MultiValueMapUtil;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller managing the display of viewers
 */
@RestController
@RequestMapping(EndPoint.DISPLAY_PATH)
@Tag(name = "Display", description = "API Endpoints for displaying viewers")
@Slf4j
@Validated
public class DisplayController {

	// Services
	private final DisplayService displayService;
	private final CryptographyService cryptographyService;
	private final Validator validator;

	/**
	 * Autowired constructor
	 * @param displayService service which will select the viewer to launch depending on rules
	 * @param cryptographyService cryptography service
	 */
	@Autowired
	public DisplayController(final DisplayService displayService,
			final CryptographyService cryptographyService,  final Validator validator) {
		this.displayService = displayService;
		this.cryptographyService = cryptographyService;
		this.validator = validator;
	}

	/**
	 * Launch a viewer depending on search criteria: unsecured version
	 * @param params Search Criteria
	 * @return RedirectView
	 */
	@Operation(summary = "Launch a viewer (Regular)(Not Authenticated)",
			description = "Launch a viewer depending on search criteria: not authenticated version")
	@GetMapping
	public RedirectView launchViewerWithoutIHEParameters(HttpServletRequest request, Authentication authentication,
														 @RequestParam MultiValueMap<String,String> params) {
		return launchViewerByEvaluatingQueryParams(request, authentication, params, ArchiveSearchCriteria.class);
	}

	/**
	 * Launch a viewer depending on search criteria: secured version
	 * @param params Search Criteria
	 * @return RedirectView
	 */
	@Operation(summary = "Launch a viewer (Regular)(Authenticated)",
			description = "Launch a viewer depending on search criteria: authenticated version")
	@GetMapping(EndPoint.AUTH_PATH)
	@PreAuthorize("hasRole('viewer_display')")
	public RedirectView launchAuthViewerWithoutIHEParameters(HttpServletRequest request,
															 @Parameter(hidden = true, required = true) @NotNull Authentication authentication,
															 @RequestParam MultiValueMap<String,String> params) {
		try {
			MultiValueMapUtil.cleanInputParameters(params);
			return this.launchViewerWithoutIHEParameters(request, authentication, params);
		}
		finally {
			// Reset the authentication in order to force OAuth2 to login
			// again and get a new fresh access token when using oauth2Login in
			// SecurityConfiguration
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	/**
	 * Launch a viewer depending on IHE search criteria: not authenticated version
	 * @param params Search Criteria
	 * @return RedirectView
	 */
	@Operation(summary = "Launch a viewer (IHE)(Not authenticated)",
			description = "Launch a viewer depending on IHE search criteria: not authenticated version")
	@GetMapping(EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchViewerWithIHEParameters(HttpServletRequest request, Authentication authentication,
													  @RequestParam MultiValueMap<String,String> params) {
		return launchViewerByEvaluatingQueryParams(request, authentication, params, IHESearchCriteria.class);
	}

	/**
	 * Launch a viewer depending on IHE search criteria: authenticated version
	 * @param params Search Criteria
	 * @return RedirectView
	 */
	@Operation(summary = "Launch a viewer (IHE)(Authenticated)",
			description = "Launch a viewer depending on IHE search criteria: authenticated version")
	@GetMapping(EndPoint.AUTH_IHE_INVOKE_IMAGE_DISPLAY_PATH)
	@PreAuthorize("hasRole('viewer_display')")
	public RedirectView launchAuthViewerWithIHEParameters(HttpServletRequest request,
			@Parameter(hidden = true, required = true) @NotNull Authentication authentication,
														  @RequestParam MultiValueMap<String,String> params) {
		try {
			MultiValueMapUtil.cleanInputParameters(params);
			return this.launchViewerWithIHEParameters(request, authentication, params);
		}
		finally {
			// Reset the authentication in order to force OAuth2 to login
			// again and get a new fresh access token when using oauth2Login in
			// SecurityConfiguration
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}
	@Operation(summary = "Launch a viewer (Post)(IHE)(Not Authenticated)",
			description = "Launch a viewer depending on IHE search criteria: not authenticated version => search criteria in body")
	@PostMapping(EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchViewerWithIHEParameters(HttpServletRequest request,
			@RequestBody @Valid IHESearchCriteria iheSearchCriteria) {
		return this.launchViewer(null, iheSearchCriteria);
	}

	@Operation(summary = "Launch a viewer (Post)(Regular)(Not Authenticated)",
			description = "Launch a viewer depending on search criteria: not authenticated version => search criteria in body")
	@PostMapping
	public RedirectView launchViewerWithoutIHEParameters(HttpServletRequest request,
			@RequestBody @Valid ArchiveSearchCriteria archiveSearchCriteria) {
		return this.launchViewer(null, archiveSearchCriteria);
	}

	/**
	 * Launch a viewer depending on search criteria in request params
	 * @param params Search Criteria to evaluate
	 * @param searchCriteriaClassType Class type expected
	 * @return RedirectView
	 */
	private <T extends SearchCriteria> RedirectView launchViewerByEvaluatingQueryParams(HttpServletRequest request, Authentication authentication, MultiValueMap<String, String> params, Class<T> searchCriteriaClassType) {
		// Map search criteria to corresponding object and validate inputs
		SearchCriteria searchCriteria = retrieveSearchCriteriaFromQueryParams(params, searchCriteriaClassType);

		return launchViewer(authentication, searchCriteria);
	}

	/**
	 * Launch a viewer depending on search criteria
	 * @param authentication Authentication
	 * @param searchCriteria Search Criteria to evaluate
	 * @return RedirectView
	 */
	private RedirectView launchViewer(Authentication authentication, SearchCriteria searchCriteria) {
		// If encoding enabled decode values
		this.cryptographyService.decode(searchCriteria);

		// Launch viewer
		return new RedirectView(this.displayService.viewerLaunchUrl(searchCriteria, authentication));
	}

	/**
	 * Resolve the host of the request in case it is not defined
	 * @param request Request
	 * @param searchCriteria Search Criteria
	 */
	private void resolveHostSearchCriteria(HttpServletRequest request, SearchCriteria searchCriteria) {
		if (searchCriteria != null && StringUtils.isBlank(searchCriteria.getHost())) {
			searchCriteria.setHost(InetUtil.getClientHostFromRequest(request));
		}
	}
	/**
	 * Map search criteria to corresponding object and validate inputs
	 * @param parameters Parameters to evaluate
	 * @param classToEvaluate Parent class type expected
	 * @return SearchCriteria built from parameters
	 */
	private SearchCriteria retrieveSearchCriteriaFromQueryParams(MultiValueMap<String, String> parameters, @NotNull Class<?> classToEvaluate) {
		// let Jackson do its deduction & binding
		SearchCriteria searchCriteria = SearchCriteria.jacksonDeduction(parameters);

		// Compare with expected parent class
		if(!classToEvaluate.isInstance(searchCriteria)){
			if (searchCriteria == null) {
				throw new ConstraintException("Search criteria cannot be null.");
			}
			else {
				throw new ConstraintException("Wrong type parameters, deducted class %s instead of %s".formatted(searchCriteria.getClass(), classToEvaluate.getTypeName()));
			}
		}

		// Validation of constraints
		Set<ConstraintViolation<SearchCriteria>> violations = validator.validate(searchCriteria);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException("Validation failed: %s".formatted(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","))), violations);
		}
		return searchCriteria;
	}

}
