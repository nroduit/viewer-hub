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
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.viewer.hub.back.config.ArchiveViewerMapper;
import org.viewer.hub.back.config.DicomWebRequest;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.constant.ParamName;
import org.viewer.hub.back.model.searchcriteria.*;
import org.viewer.hub.back.service.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

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
	private final DisplaySelectViewerRuleService displaySelectViewerRuleService;

	private final CryptographyService cryptographyService;

	private final ConnectorService connectorService;

	/**
	 * Autowired constructor
	 * @param displaySelectViewerRuleService display service
	 * @param cryptographyService cryptography service
	 */

	@Autowired
	public DisplayController(final DisplaySelectViewerRuleService displaySelectViewerRuleService, final CryptographyService cryptographyService, final ConnectorService connectorService) {
		this.displaySelectViewerRuleService = displaySelectViewerRuleService;
		this.cryptographyService = cryptographyService;
		this.connectorService = connectorService;
	}

	/**
	 * Launch Viewer depending on IHE search criteria: not authenticated version
	 * @param iheSearchCriteria IHE Search Criteria
	 * @param extCfg ext config
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch target viewer (IHE)(Not authenticated)",
			description = "Launch target viewer depending on IHE search criteria: not authenticated version")
	@GetMapping(EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchViewerWithIHEParameters(HttpServletRequest request,
													  Authentication authentication,
													  @Valid IHESearchCriteria iheSearchCriteria,
													  @RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		// TODO: workaround=> currently not working with different name => conflict ?

		// Resolve the host of the request in case it is not defined
		// resolveHostSearchCriteria(request, iheSearchCriteria);

		// If encoding enabled decode values
		this.cryptographyService.decode(iheSearchCriteria);

		String archive = getArchive(request, iheSearchCriteria);
		String viewer = getViewer(request, iheSearchCriteria, archive);
		if (archive == null || viewer == null) {
			return null;
		}

		String redirectUrl = displaySelectViewerRuleService.getViewerUrl(archive, viewer, iheSearchCriteria, extCfg, authentication);
		if (redirectUrl == null) {
			LOG.error("No valid redirectUrl specified");
			return null;
		}

		String archiveName = connectorService.getArchiveNameFromId(archive);
		if (ArchiveViewerMapper.shouldOpenViewerInNewTab(archiveName, viewer)) {
			manuallyOpenUrl(redirectUrl);
		}
		return new RedirectView(redirectUrl);
	}

	/**
	 * Launch target viewer depending on IHE search criteria: authenticated version
	 * @param iheSearchCriteria IHE Search Criteria
	 * @param extCfg ext config
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch target viewer (IHE)(Authenticated)",
			description = "Launch target viewer depending on IHE search criteria: authenticated version")
	@GetMapping(EndPoint.AUTH_PATH + EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchAuthViewerWithIHEParameters(HttpServletRequest request,
														  @Parameter(hidden = true, required = true) @NotNull Authentication authentication,
														  @Valid IHESearchCriteria iheSearchCriteria,
														  @RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		try {
			return this.launchViewerWithIHEParameters(request, authentication, iheSearchCriteria, extCfg);
		}
		finally {
			// Reset the authentication in order to force OAuth2 to login
			// again and get a new fresh access token when using oauth2Login in
			// SecurityConfiguration
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	/**
	 * Launch target viewer depending on search criteria: unsecured version
	 * @param archiveSearchCriteria Archive search criteria
	 * @param extCfg ext config
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch target viewer (Regular)(Not Authenticated)",
			description = "Launch target viewer depending on search criteria: not authenticated version")
	@GetMapping
	public RedirectView launchViewerWithArchiveParameters(HttpServletRequest request, Authentication authentication,
														  @Valid ArchiveSearchCriteria archiveSearchCriteria,
														  @RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		// TODO: workaround=> currently not working with different name => conflict ?

		// If encoding enabled decode values
		this.cryptographyService.decode(archiveSearchCriteria);

		String archive = getArchive(request, archiveSearchCriteria);
		String viewer = getViewer(request, archiveSearchCriteria, archive);
		if (archive == null || viewer == null) {
			return null;
		}

		String redirectUrl = displaySelectViewerRuleService.getViewerUrl(archive, viewer, archiveSearchCriteria, extCfg, authentication);
		if (redirectUrl == null) {
			LOG.error("No valid redirectUrl specified");
			return null;
		}

		String archiveName = connectorService.getArchiveNameFromId(archive);
		if (ArchiveViewerMapper.shouldOpenViewerInNewTab(archiveName, viewer)) {
			manuallyOpenUrl(redirectUrl);
		}
		return new RedirectView(redirectUrl);
	}

	/**
	 * Launch target viewer depending on search criteria: secured version
	 * @param archiveSearchCriteria Archive search criteria
	 * @param extCfg ext config
	 * @return launch target viewer
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch target viewer (Regular)(Authenticated)",
			description = "Launch target viewer depending on search criteria: authenticated version")
	@GetMapping(EndPoint.AUTH_PATH)
	public RedirectView launchAuthViewerWithArchiveParameters(HttpServletRequest request,
															  @Parameter(hidden = true, required = true) @NotNull Authentication authentication,
															  @Valid ArchiveSearchCriteria archiveSearchCriteria,
															  @RequestParam(value = ParamName.EXT_CFG, required = false) String extCfg) {
		try {
			return this.launchViewerWithArchiveParameters(request, authentication, archiveSearchCriteria, extCfg);
		}
		finally {
			// Reset the authentication in order to force OAuth2 to login
			// again and get a new fresh access token when using oauth2Login in
			// SecurityConfiguration
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	/**
	 * Launch target viewer depending on search criteria: unsecured version
	 * @param dicomWebRequest dicom web request
	 * @return launch target viewer
	 * Build also the manifest corresponding to the search criteria if not present in the
	 * cache.
	 */
	@Operation(summary = "Launch target viewer (Regular)(Not Authenticated)",
			description = "Launch target viewer depending on viewer Header")
	@PostMapping(value = EndPoint.DICOMWEB_PATH + EndPoint.STUDIES_PATH)
	public ResponseEntity<String> launchViewerWithArchiveParametersStudies(
			@Context DicomWebRequest dicomWebRequest) throws JSONException, IOException {

		ArchiveSearchCriteria archiveSearchCriteria = resolveViewerDicomWebCriterias(dicomWebRequest);
		String archive = getArchive(dicomWebRequest, archiveSearchCriteria);
		String viewer = getViewer(dicomWebRequest, archiveSearchCriteria, archive);
		if (archive == null || viewer == null) {
			return null;
		}

		String redirectUrl = displaySelectViewerRuleService.getQidoViewerUrl(archive, viewer, archiveSearchCriteria);
		if (redirectUrl == null) {
			LOG.error("No valid redirectUrl specified");
			return null;
		}

		String archiveName = connectorService.getArchiveNameFromId(archive);
		if (ArchiveViewerMapper.shouldOpenViewerInNewTab(archiveName, viewer)) {
			manuallyOpenUrl(redirectUrl);
		}
		return dicomWebRequest.generateResponse(redirectUrl);
	}

	private ArchiveSearchCriteria resolveViewerDicomWebCriterias(DicomWebRequest request) throws IOException {
		Attributes attributes = new Attributes(request.getDicomMetaData().getDicomObject());
		ArchiveSearchCriteria archiveSearchCriteria = new ArchiveSearchCriteria();
		archiveSearchCriteria.setAccessionNumber(Set.of(attributes.getString(org.dcm4che3.data.Tag.AccessionNumber)));
		archiveSearchCriteria.setPatientID(Set.of(attributes.getString(org.dcm4che3.data.Tag.PatientID)));
		archiveSearchCriteria.setStudyUID(Set.of(attributes.getString(org.dcm4che3.data.Tag.StudyInstanceUID)));
		archiveSearchCriteria.setObjectUID(Set.of(attributes.getString(org.dcm4che3.data.Tag.SOPInstanceUID)));
		archiveSearchCriteria.setSeriesUID(Set.of(attributes.getString(org.dcm4che3.data.Tag.SeriesInstanceUID)));
		return archiveSearchCriteria;
	}

	private String getArchive(HttpServletRequest request, SearchCriteria searchCriteria) {
		String archive;
		if (!searchCriteria.getArchive().isEmpty()) {
			archive = searchCriteria.getArchive().getFirst();
		}
		else {
			archive = request.getHeader("archive");
		}
		if (archive == null || archive.isEmpty()) {
			LOG.error("No archive specified");
			return null;
		}
		return archive;
	}

	private String getViewer(HttpServletRequest request, SearchCriteria searchCriteria, String archive) {
		String viewer;
		if (!searchCriteria.getViewer().isEmpty()) {
			viewer = searchCriteria.getViewer().getFirst();
		}
		else {
			viewer = request.getHeader("viewer");
		}
		if (viewer == null || viewer.isEmpty()) {
			String archiveName = connectorService.getArchiveNameFromId(archive);
			viewer = ArchiveViewerMapper.getViewer(archiveName);
		}
		return viewer;
	}

	// Fixme : Temporary code. Because Orthanc does not properly manage RedirectView : probably does not read Location header
	private void manuallyOpenUrl(String redirectUrl) {
		if (Desktop.isDesktopSupported()){
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(redirectUrl));
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + redirectUrl);
			} catch (IOException e) {
				try {
					runtime.exec("rundll32 url.dll,FileProtocolHandler " + redirectUrl);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	@Operation(summary = "Launch target viewer (Post)(IHE)(Not Authenticated)",
			description = "Launch target viewer depending on IHE search criteria: not authenticated version => search criteria in body")
	@PostMapping(EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH)
	public RedirectView launchViewerWithIHEParameters(HttpServletRequest request,
													  @RequestBody @Valid IHESearchCriteria iheSearchCriteria) {
		return this.launchViewerWithIHEParameters(request, null, iheSearchCriteria, null);
	}

	@Operation(summary = "Launch target viewer (Post)(Regular)(Not Authenticated)",
			description = "Launch target viewer depending on search criteria: not authenticated version => search criteria in body")
	@PostMapping
	public RedirectView launchViewerWithArchiveParameters(HttpServletRequest request,
														  @RequestBody @Valid ArchiveSearchCriteria archiveSearchCriteria) {
		return this.launchViewerWithArchiveParameters(request, null, archiveSearchCriteria, null);
	}

}
