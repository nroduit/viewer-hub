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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;

/**
 * Legacy controller for backward compatibility with the old /display/weasis and
 * /display/auth/weasis endpoints. Delegates directly to the new DisplayController
 * endpoints after injecting the viewer=WEASIS parameter.
 *
 * @deprecated This controller is scheduled for removal once the migration to the new
 * endpoints is complete.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(EndPoint.DISPLAY_PATH)
@Tag(name = "Legacy Display",
		description = "Deprecated legacy endpoints for backward compatibility - will be removed after migration")
@Slf4j
public class LegacyDisplayController {

	private static final String VIEWER_PARAM = "viewer";

	private static final String WEASIS_VIEWER = "WEASIS";

	private final DisplayController displayController;

	@Autowired
	public LegacyDisplayController(DisplayController displayController) {
		this.displayController = displayController;
	}

	/**
	 * Legacy endpoint: /display/weasis?... Delegates to /display?viewer=WEASIS&...
	 * @param request HttpServletRequest
	 * @param authentication Authentication
	 * @param params Query parameters
	 * @return RedirectView from the new endpoint
	 * @deprecated Use /display?viewer=WEASIS instead
	 */
	@Deprecated(forRemoval = true)
	@Operation(summary = "Launch Weasis viewer (Not Authenticated)",
			description = "Deprecated: delegates to /display?viewer=WEASIS. Use the new endpoint directly.",
			deprecated = true)
	@GetMapping(value = EndPoint.WEASIS_PATH)
	public RedirectView legacyLaunchWeasis(HttpServletRequest request, Authentication authentication,
			@RequestParam MultiValueMap<String, String> params) {
		LOG.warn("Deprecated endpoint called: /display/weasis. Please migrate to /display?viewer=WEASIS");
		addViewerParam(params);
		return displayController.launchViewerWithoutIHEParameters(request, authentication, params);
	}

	/**
	 * Legacy endpoint: /display/auth/weasis?... Delegates to
	 * /display/auth?viewer=WEASIS&...
	 * @param request HttpServletRequest
	 * @param authentication Authentication
	 * @param params Query parameters
	 * @return RedirectView from the new endpoint
	 * @deprecated Use /display/auth?viewer=WEASIS instead
	 */
	@Deprecated(forRemoval = true)
	@Operation(summary = "Launch Weasis viewer (Authenticated)",
			description = "Deprecated: delegates to /display/auth?viewer=WEASIS. Use the new endpoint directly.",
			deprecated = true)
	@GetMapping(value = EndPoint.AUTH_WEASIS_PATH)
	public RedirectView legacyLaunchAuthWeasis(HttpServletRequest request, Authentication authentication,
			@RequestParam MultiValueMap<String, String> params) {
		LOG.warn("Deprecated endpoint called: /display/auth/weasis. Please migrate to /display/auth?viewer=WEASIS");
		addViewerParam(params);
		return displayController.launchAuthViewerWithoutIHEParameters(request, authentication, params);
	}

	/**
	 * Injects the viewer=WEASIS parameter into the query parameters if not already
	 * present
	 * @param params Query parameters to enrich
	 */
	private void addViewerParam(MultiValueMap<String, String> params) {
		if (!params.containsKey(VIEWER_PARAM)) {
			params.add(VIEWER_PARAM, WEASIS_VIEWER);
		}
	}

	/**
	 * Legacy endpoint: POST /display/weasis Delegates to POST /display with viewer=WEASIS
	 * @param request HttpServletRequest
	 * @param archiveSearchCriteria Search criteria in body
	 * @return RedirectView from the new endpoint
	 * @deprecated Use POST /display with viewer=WEASIS instead
	 */
	@Deprecated(forRemoval = true)
	@Operation(summary = "[DEPRECATED] Launch Weasis viewer (Post)(Not Authenticated)",
			description = "Deprecated: delegates to POST /display. Use the new endpoint directly.", deprecated = true)
	@PostMapping(value = EndPoint.WEASIS_PATH)
	public RedirectView legacyPostLaunchWeasis(HttpServletRequest request,
			@RequestBody @Valid ArchiveSearchCriteria archiveSearchCriteria) {
		LOG.warn("Deprecated endpoint called: POST /display/weasis. Please migrate to POST /display");
		archiveSearchCriteria.setViewer(ViewerType.WEASIS);
		return displayController.launchViewerWithoutIHEParameters(request, archiveSearchCriteria);
	}

}
