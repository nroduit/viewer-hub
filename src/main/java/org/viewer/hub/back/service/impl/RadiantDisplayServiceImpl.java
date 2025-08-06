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
import org.viewer.hub.back.constant.RadiantCommandName;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.RadiantDisplayService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RadiantDisplayServiceImpl implements RadiantDisplayService {

	// Services
	private final ConnectorService connectorService;

	@Autowired
	public RadiantDisplayServiceImpl(final ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public String retrieveRadiantQidoLaunchUrl(@Valid ArchiveSearchCriteria searchCriteria, String archive) {
		String dicomQuery = this.retrieveDicomQidoCommand(searchCriteria, archive);

		String launchUrl = buildRadiantProtocolCommand(dicomQuery);

		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
	}

	private String retrieveDicomQidoCommand(ArchiveSearchCriteria searchCriteria, String archive) {
		// Url to retrieve the manifest corresponding to the key
		List<String> query = new ArrayList<>();

		// Dicom aet
		String aet = connectorService.getAETFromId(archive);
		if (aet != null) {
			query.add("n=paet&v=" + aet);
		}

		query.add("n=pstv");

		// Series Instance Uid
		if (!searchCriteria.getSeriesUID().isEmpty()) {
			query.add("v=" + String.join(",", searchCriteria.getSeriesUID()));
			query.add("v=%22SERIESUID%22");
		}
		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			query.add("v=" + String.join(",", searchCriteria.getAccessionNumber()));
			query.add("v=%22ACCESSION-NUMBER%22");
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			query.add("v=" + String.join(",", searchCriteria.getStudyUID()));
			query.add("v=%22STUDYUID%22");
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			query.add("v=" + String.join(",", searchCriteria.getPatientID()));
			query.add("v=%22PATIENTID%22");
		}

		return "?%s".formatted(String.join("&", query));
	}

	/**
	 * Build encoded RadiAnt protocol url
	 * command
	 * @param dicomQuery dicom query
	 * @return weasis protocol encoded url built
	 */
	private static String buildRadiantProtocolCommand(String dicomQuery) {
		return RadiantCommandName.LAUNCH_URL_RADIANT_COMMAND.formatted(dicomQuery);
	}

}
