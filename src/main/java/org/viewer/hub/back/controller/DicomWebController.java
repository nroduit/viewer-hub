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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.model.searchcriteria.DicomWebSearchCriteria;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.DicomWebService;
import org.viewer.hub.back.util.DicomWebUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashSet;


/**
 * Controller managing the dicom files
 */
@RestController
@RequestMapping(EndPoint.DICOMWEB_PATH)
@Tag(name = "DicomWeb", description = "API Endpoints for DicomWeb")
@Slf4j
@Validated
public class DicomWebController {

	private final DicomWebService dicomWebService;
	private final ConnectorService connectorService;

	@Value("${viewer-hub.tmp.storage}")
	private String storageDir;

	@Value("${viewer-hub.server.url}")
	private String viewerHubServerUrl;

	@Autowired
	public DicomWebController(final DicomWebService dicomWebService, final ConnectorService connectorService) {
		this.dicomWebService = dicomWebService;
		this.connectorService = connectorService;
	}

	@Operation(summary = "Retrieve dicom data in dicom json format for OHIF")
	@GetMapping(value = "/{archive}/studies",
			produces = DicomWebUtils.APPLICATION_DICOM_JSON)
	public ResponseEntity<?> retrieveOHIFDicomStudies(
		HttpServletRequest request,
        @PathVariable(value = "archive") String archive,
		@Valid DicomWebSearchCriteria dicomWebSearchCriteria
	) throws IOException, URISyntaxException, InterruptedException, JSONException {
		ConnectorProperty connector = connectorService.retrieveConnectorFromId(new LinkedHashSet<>(Collections.singletonList(archive)));
		String targetDicomUrl = connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getServer().getFullUrl()
				+ EndPoint.STUDIES_PATH;
		String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
		return dicomWebService.getResponseFrom(targetDicomUrl + queryString, DicomWebUtils.APPLICATION_DICOM_JSON);
	}

	@Operation(summary = "Retrieve dicom data in dicom json format for OHIF")
	@GetMapping(value = "/{archive}/studies/{studyUID}/series")
	public ResponseEntity<?> retrieveOHIFDicomSeries(
			HttpServletRequest request,
			@PathVariable(value = "archive") String archive,
			@PathVariable(value = "studyUID") String studyUID,
			@Valid DicomWebSearchCriteria dicomWebSearchCriteria
	) throws IOException, URISyntaxException, InterruptedException, JSONException {
		ConnectorProperty connector = connectorService.retrieveConnectorFromId(new LinkedHashSet<>(Collections.singletonList(archive)));
		String targetDicomUrl = connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getServer().getFullUrl()
				+ EndPoint.STUDIES_PATH + "/" + studyUID
				+ EndPoint.SERIES_PATH;
		String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
		return dicomWebService.getResponseFrom(targetDicomUrl + queryString, DicomWebUtils.APPLICATION_DICOM_JSON);
	}

	@Operation(summary = "Retrieve dicom data in dicom json format for OHIF")
	@GetMapping(value = "/{archive}/studies/{studyUID}/series/{seriesUID}/metadata")
	public ResponseEntity<?> retrieveOHIFDicomSeriesMetadata(
			HttpServletRequest request,
			@PathVariable(value = "archive") String archive,
			@PathVariable(value = "studyUID") String studyUID,
			@PathVariable(value = "seriesUID") String seriesUID,
			@Valid DicomWebSearchCriteria dicomWebSearchCriteria
	) throws IOException, URISyntaxException, InterruptedException, JSONException {
		ConnectorProperty connector = connectorService.retrieveConnectorFromId(new LinkedHashSet<>(Collections.singletonList(archive)));
		String targetDicomUrl = connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getServer().getFullUrl()
				+ EndPoint.STUDIES_PATH + "/" + studyUID
				+ EndPoint.SERIES_PATH + "/" + seriesUID
				+ EndPoint.METADATA_PATH;
		String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
		return dicomWebService.getResponseFrom(targetDicomUrl + queryString, DicomWebUtils.APPLICATION_DICOM_JSON);
	}

	@Operation(summary = "Retrieve dicom data in dicom json format for OHIF")
	@GetMapping(value = "/{archive}/studies/{studyUID}/series/{seriesUID}/instances/{instancesUID}/frames/{frames}")
	public ResponseEntity<?> retrieveOHIFDicomSeriesMetadata(
			HttpServletRequest request,
			@PathVariable(value = "archive") String archive,
			@PathVariable(value = "studyUID") String studyUID,
			@PathVariable(value = "seriesUID") String seriesUID,
			@PathVariable(value = "instancesUID") String instancesUID,
			@PathVariable(value = "frames") String frames,
			@Valid DicomWebSearchCriteria dicomWebSearchCriteria
	) throws IOException, URISyntaxException, InterruptedException, JSONException {
		ConnectorProperty connector = connectorService.retrieveConnectorFromId(new LinkedHashSet<>(Collections.singletonList(archive)));
		String targetDicomUrl = connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getServer().getFullUrl()
				+ EndPoint.STUDIES_PATH + "/" + studyUID
				+ EndPoint.SERIES_PATH + "/" + seriesUID
				+ EndPoint.INSTANCES_PATH + "/" + instancesUID
				+ EndPoint.FRAMES_PATH + "/" + frames;
		String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
		return dicomWebService.getResponseFrom(targetDicomUrl + queryString, DicomWebUtils.APPLICATION_DICOM_JSON);
	}

}
