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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.model.ArchiveSearchCriteria;
import org.viewer.hub.back.model.IHESearchCriteria;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.service.CryptographyService;
import org.viewer.hub.back.service.DisplaySelectViewerRuleService;
import org.viewer.hub.back.service.DisplayService;
import org.viewer.hub.back.util.InetUtil;

/**
 * Controller managing the display of weasis
 */
@RestController
@RequestMapping(EndPoint.DISPLAY_PATH)
@Tag(name = "Display", description = "API Endpoints for managing display")
@Slf4j
@Validated
public class DisplayController {

	// Services
	private final DisplayService displayService;

	// TODO
	private final DisplaySelectViewerRuleService displaySelectViewerRuleService;

	private final CryptographyService cryptographyService;

	/**
	 * Autowired constructor
	 * @param displayService display service
	 * @param cryptographyService cryptography service
	 */
	@Autowired
	public DisplayController(final DisplayService displayService, final CryptographyService cryptographyService,
							 final DisplaySelectViewerRuleService displaySelectViewerRuleService) {
		this.displayService = displayService;
		this.cryptographyService = cryptographyService;
		this.displaySelectViewerRuleService = displaySelectViewerRuleService;
	}

	// TODO
	/**
	 * Display a viewer depending on SearchCriteria and rules to select the viewer to display
	 */
	@Operation(summary = "Display a viewer depending on search criteria and rule to select the viewer to launch",
			description = "Display a viewer depending on SearchCriteria and rules to select the viewer to launch")
	@GetMapping(EndPoint.VIEWER)
	public RedirectView launchViewerWithoutIHEParameters(HttpServletRequest request, Authentication authentication,
													  @Valid ArchiveSearchCriteria archiveSearchCriteria) {
		// TODO
		return new RedirectView(this.displaySelectViewerRuleService.displayViewer(authentication, archiveSearchCriteria));
	}


	/**
	 * Launch Weasis depending on IHE search criteria: not authenticated version
	 * @param weasisIHESearchCriteria weasis IHE Search Criteria
	 * @param extCfg ext config
	 * @return launch weasis with the weasis command thanks to the weasis launch url.
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch Weasis (IHE)(Not authenticated)",
			description = "Launch Weasis depending on IHE search criteria: not authenticated version")
	@GetMapping(EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchWeasisWithIHEParameters(HttpServletRequest request, Authentication authentication,
			@Valid IHESearchCriteria weasisIHESearchCriteria,
			@RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		// TODO: workaround=> currently not working with different name => conflict ?
		// to do JacksonConfig
		weasisIHESearchCriteria.setExtCfg(extCfg);

		// Resolve the host of the request in case it is not defined
		// resolveHostSearchCriteria(request, weasisIHESearchCriteria);

		// If encoding enabled decode values
		this.cryptographyService.decode(weasisIHESearchCriteria);
		return new RedirectView(this.displayService.retrieveWeasisLaunchUrl(weasisIHESearchCriteria, authentication));
	}

	/**
	 * Launch Weasis depending on IHE search criteria: authenticated version
	 * @param weasisIHESearchCriteria weasis IHE Search Criteria
	 * @param extCfg ext config
	 * @return launch weasis with the weasis command thanks to the weasis launch url.
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch Weasis (IHE)(Authenticated)",
			description = "Launch Weasis depending on IHE search criteria: authenticated version")
	@GetMapping(EndPoint.AUTH_IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchAuthWeasisWithIHEParameters(HttpServletRequest request,
			@Parameter(hidden = true, required = true) @NotNull Authentication authentication,
			@Valid IHESearchCriteria weasisIHESearchCriteria,
			@RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		try {
			return this.launchWeasisWithIHEParameters(request, authentication, weasisIHESearchCriteria, extCfg);
		}
		finally {
			// Reset the authentication in order to force OAuth2 to login
			// again and get a new fresh access token when using oauth2Login in
			// SecurityConfiguration
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	/**
	 * Launch Weasis depending on search criteria: unsecured version
	 * @param weasisSearchCriteria weasis Search Criteria
	 * @param extCfg ext config
	 * @return launch weasis with the weasis command thanks to the weasis launch url.
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch Weasis (Regular)(Not Authenticated)",
			description = "Launch Weasis depending on search criteria: not authenticated version")
	@GetMapping(EndPoint.WEASIS_PATH)
	public RedirectView launchWeasisWithoutIHEParameters(HttpServletRequest request, Authentication authentication,
			@Valid ArchiveSearchCriteria weasisSearchCriteria,
			@RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		// TODO: workaround=> currently not working with different name => conflict ?
		// to do JacksonConfig
		weasisSearchCriteria.setExtCfg(extCfg);

		// Resolve the host of the request in case it is not defined
		// resolveHostSearchCriteria(request, weasisSearchCriteria);

		// If encoding enabled decode values
		this.cryptographyService.decode(weasisSearchCriteria);
		return new RedirectView(this.displayService.retrieveWeasisLaunchUrl(weasisSearchCriteria, authentication));
	}

	/**
	 * Launch Weasis depending on search criteria: secured version
	 * @param weasisSearchCriteria weasis Search Criteria
	 * @param extCfg ext config
	 * @return launch weasis with the weasis command thanks to the weasis launch url.
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch Weasis (Regular)(Authenticated)",
			description = "Launch Weasis depending on search criteria: authenticated version")
	@GetMapping(EndPoint.AUTH_WEASIS_PATH)
	public RedirectView launchAuthWeasisWithoutIHEParameters(HttpServletRequest request,
			@Parameter(hidden = true, required = true) @NotNull Authentication authentication,
			@Valid ArchiveSearchCriteria weasisSearchCriteria,
			@RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		try {
			return this.launchWeasisWithoutIHEParameters(request, authentication, weasisSearchCriteria, extCfg);
		}
		finally {
			// Reset the authentication in order to force OAuth2 to login
			// again and get a new fresh access token when using oauth2Login in
			// SecurityConfiguration
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	@Operation(summary = "Launch Weasis (Post)(IHE)(Not Authenticated)",
			description = "Launch Weasis depending on IHE search criteria: not authenticated version => search criteria in body")
	@PostMapping(EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchWeasisWithIHEParameters(HttpServletRequest request,
			@RequestBody @Valid IHESearchCriteria weasisIHESearchCriteria) {
		return this.launchWeasisWithIHEParameters(request, null, weasisIHESearchCriteria,
				weasisIHESearchCriteria.getExtCfg());
	}

	@Operation(summary = "Launch Weasis (Post)(Regular)(Not Authenticated)",
			description = "Launch Weasis depending on search criteria: not authenticated version => search criteria in body")
	@PostMapping(EndPoint.WEASIS_PATH)
	public RedirectView launchWeasisWithoutIHEParameters(HttpServletRequest request,
			@RequestBody @Valid ArchiveSearchCriteria weasisSearchCriteria) {
		return this.launchWeasisWithoutIHEParameters(request, null, weasisSearchCriteria,
				weasisSearchCriteria.getExtCfg());
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

}
