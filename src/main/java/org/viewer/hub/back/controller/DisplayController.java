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
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisIHESearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.CryptographyService;
import org.viewer.hub.back.service.WeasisDisplayService;

import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.service.OHIFDisplayService;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
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
	private final WeasisDisplayService weasisDisplayService;

	private final OHIFDisplayService ohifDisplayService;

	private final CryptographyService cryptographyService;
	private final ConnectorService connectorService;

	/**
	 * Autowired constructor
	 * @param weasisDisplayService display service
	 * @param cryptographyService cryptography service
	 */

	@Autowired
	public DisplayController(final WeasisDisplayService weasisDisplayService, final OHIFDisplayService ohifDisplayService, final CryptographyService cryptographyService, final ConnectorService connectorService) {
		this.weasisDisplayService = weasisDisplayService;
		this.ohifDisplayService = ohifDisplayService;
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

		String redirectUrl = null;
		if (iheSearchCriteria.getViewer().isEmpty()) {
			LOG.error("No archive specified");
			return null;
		}
		if (Viewer.WEASIS.toString().equals(iheSearchCriteria.getViewer().getFirst())) {
			WeasisIHESearchCriteria weasisIHESearchCriteria = (WeasisIHESearchCriteria) iheSearchCriteria;
			// to do JacksonConfig
			if (extCfg != null) {
				weasisIHESearchCriteria.setExtCfg(extCfg);
			}
			redirectUrl = this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(weasisIHESearchCriteria, authentication);
		}
		else if (Viewer.OHIF.toString().equals(iheSearchCriteria.getViewer().getFirst())) {
//			redirectUrl = this.ohifDisplayService.retrieveDicomUrl(iheSearchCriteria, authentication);
		}

		if (redirectUrl == null) {
			LOG.error("No valid archive specified");
			return null;
		}

		if (!connectorService.canHandleRedirect(iheSearchCriteria.getArchive().getFirst())) {
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

		String viewer = archiveSearchCriteria.getViewer().getFirst();
		if (viewer == null || viewer.isEmpty()) {
			String archive = archiveSearchCriteria.getArchive().getFirst();
			if (archive == null || archive.isEmpty()) {
				LOG.error("No archive specified");
				return null;
			}
			viewer = ArchiveViewerMapper.getViewer(archive);
			if (viewer == null || viewer.isEmpty()) {
				LOG.error("No viewer specified");
				return null;
			}
		}

		String redirectUrl = null;
		if (Viewer.WEASIS.toString().equals(viewer)) {
			// to do JacksonConfig
			WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
			if (extCfg != null) {
				weasisArchiveSearchCriteria.setExtCfg(extCfg);
			}
			redirectUrl = this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(weasisArchiveSearchCriteria, authentication);
		}
		else if (Viewer.OHIF.toString().equals(viewer)) {
			redirectUrl = this.ohifDisplayService.retrieveDicomUrl(archiveSearchCriteria);
		}

		if (redirectUrl == null) {
			LOG.error("No valid archive specified");
			return null;
		}

		if (!connectorService.canHandleRedirect(archiveSearchCriteria.getArchive().getFirst())) {
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
		String viewer = archiveSearchCriteria.getViewer().getFirst();
		String archive = archiveSearchCriteria.getArchive().getFirst();
		if (viewer == null || viewer.isEmpty()) {
			if (archive == null || archive.isEmpty()) {
				LOG.error("No archive specified");
				return null;
			}
			viewer = ArchiveViewerMapper.getViewer(archive);
			if (viewer == null || viewer.isEmpty()) {
				LOG.error("No viewer specified");
				return null;
			}
		}

		String redirectUrl = null;
		if (Viewer.WEASIS.toString().equals(viewer)) {
			WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
			redirectUrl = weasisDisplayService.retrieveWeasisQidoLaunchUrl(weasisArchiveSearchCriteria);
		}
		else if (Viewer.OHIF.toString().equals(viewer)) {
			redirectUrl = ohifDisplayService.retrieveDicomUrl(archiveSearchCriteria);
		}

		if (redirectUrl == null) {
			LOG.error("No valid archive specified");
			return null;
		}

		if (!connectorService.canHandleRedirect(archive)) {
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
		String archive = request.getHeader("archive");
		if (archive != null) {
			archiveSearchCriteria.setArchive(new LinkedHashSet<>(java.util.List.of(archive)));
		}
		String viewer = request.getHeader("viewer");
		if (viewer != null) {
			archiveSearchCriteria.setViewer(new LinkedHashSet<>(java.util.List.of(viewer)));
		}
		return archiveSearchCriteria;
	}

//	private String getRequestURLOrigin(HttpServletRequest request) {
//		String url = request.getRequestURL().toString();
//		String queryString = request.getQueryString();   // d=789
//		if (queryString != null) {
//			url += "?"+queryString;
//		}
//		return url;
//	}

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
