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

package org.viewer.hub.back.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.constant.SlicerCommandName;
import org.viewer.hub.back.model.searchcriteria.*;
import org.viewer.hub.back.service.*;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SlicerDisplayServiceImpl implements SlicerDisplayService {

	// Services
	private final ConnectorService connectorService;

	@Autowired
	public SlicerDisplayServiceImpl(final ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public String retrieveSlicerQidoLaunchUrl(@Valid SearchCriteria searchCriteria, String archive) {
		String dicomQuery = this.retrieveDicomQidoQuery(searchCriteria, archive);

		String launchUrl = buildSlicerProtocolCommand(dicomQuery);

		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
	}

	private String retrieveDicomQidoQuery(SearchCriteria searchCriteria, String archive) {
		List<String> query = searchCriteria instanceof IHESearchCriteria
				? getQuery((IHESearchCriteria) searchCriteria)
				: getQuery((ArchiveSearchCriteria) searchCriteria);

		// Dicomweb url
		String dicomRsUrl = connectorService.getDicomRsUrlFromId(archive);
		if (dicomRsUrl != null) {
			query.add("dicomweb_endpoint=" + dicomRsUrl);
		}

		return "?%s".formatted(String.join("&", query));
	}

	private List<String> getQuery(IHESearchCriteria iheSearchCriteria) {
		List<String> query = new ArrayList<>();
		// Accession Number
		if (!iheSearchCriteria.getAccessionNumber().isEmpty()) {
			query.add("accessionNumber=" + String.join(",", iheSearchCriteria.getAccessionNumber()));
		}
		// Study Uid
		if (!iheSearchCriteria.getStudyUID().isEmpty()) {
			query.add("studyUID=" + String.join(",", iheSearchCriteria.getStudyUID()));
		}
		// Patient Id
		if (!iheSearchCriteria.getPatientID().isEmpty()) {
			query.add("patientID=" + String.join(",", iheSearchCriteria.getPatientID()));
		}
		return query;
	}

	private List<String> getQuery(ArchiveSearchCriteria searchCriteria) {
		List<String> query = new ArrayList<>();
		if (!searchCriteria.getObjectUID().isEmpty()) {
			query.add("objectUID=" + String.join(",", searchCriteria.getObjectUID()));
		}
		// Series Instance Uid
		if (!searchCriteria.getSeriesUID().isEmpty()) {
			query.add("seriesUID=" + String.join(",", searchCriteria.getSeriesUID()));
		}
		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			query.add("accessionNumber=" + String.join(",", searchCriteria.getAccessionNumber()));
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			query.add("studyUID=" + String.join(",", searchCriteria.getStudyUID()));
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			query.add("patientID=" + String.join(",", searchCriteria.getPatientID()));
		}
		return query;
	}

	/**
	 * Build encoded 3d Slicer protocol url
	 * command
	 * @param dicomQuery dicom query
	 * @return weasis protocol encoded url built
	 */
	private static String buildSlicerProtocolCommand(String dicomQuery) {
		return SlicerCommandName.LAUNCH_URL_SLICER_COMMAND.formatted(dicomQuery);
	}

}
