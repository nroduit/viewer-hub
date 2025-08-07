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
import org.viewer.hub.back.constant.MicroDicomCommandName;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.MicroDicomDisplayService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MicroDicomDisplayServiceImpl implements MicroDicomDisplayService {

	// Services
	private final ConnectorService connectorService;

	@Autowired
	public MicroDicomDisplayServiceImpl(final ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public String retrieveMicroDicomWadoLaunchUrl(@Valid SearchCriteria searchCriteria, String archive) {
		String dicomQuery = this.retrieveDicomWadoQuery(searchCriteria, archive);

		String launchUrl = buildMicroDicomProtocolCommand(dicomQuery);

		LOG.info("[LAUNCH URL]\n " + launchUrl + " \n[SEARCH CRITERIA] " + searchCriteria);
		return launchUrl;
	}

	private String retrieveDicomWadoQuery(SearchCriteria searchCriteria, String archive) {
		List<String> query = new ArrayList<>();

		// Dicom url should be the first param in query
		String fullDicomUrl = connectorService.getFullDicomFromId(archive);
		if (fullDicomUrl != null) {
			query.add("\"param=\"pacsServer\"&value=\"" + fullDicomUrl + "\"");
		}

		query.addAll(searchCriteria instanceof IHESearchCriteria
				? getQuery((IHESearchCriteria) searchCriteria)
				: getQuery((ArchiveSearchCriteria) searchCriteria));

		return "?%s".formatted(String.join("&", query));
	}

	private List<String> getQuery(IHESearchCriteria searchCriteria) {
		List<String> query = new ArrayList<>();

		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getAccessionNumber()) + "\"&value=\"AccessionNumber\"");
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getStudyUID()) + "\"&value=\"StudyUID\"");
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getPatientID()) + "\"&value=\"PatientID\"");
		}

		return query;
	}

	private List<String> getQuery(ArchiveSearchCriteria searchCriteria) {
		List<String> query = new ArrayList<>();

		// Series Instance Uid
		if (!searchCriteria.getSeriesUID().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getSeriesUID()) + "\"&value=\"SeriesUID\"");
		}
		// Accession Number
		if (!searchCriteria.getAccessionNumber().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getAccessionNumber()) + "\"&value=\"AccessionNumber\"");
		}
		// Study Uid
		if (!searchCriteria.getStudyUID().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getStudyUID()) + "\"&value=\"StudyUID\"");
		}
		// Patient Id
		if (!searchCriteria.getPatientID().isEmpty()) {
			query.add("param=pacsTagValue&value=\"" +  String.join(",", searchCriteria.getPatientID()) + "\"&value=\"PatientID\"");
		}

		return query;
	}

	/**
	 * Build encoded Micro Dicom protocol url
	 * command
	 * @param dicomQuery dicom query
	 * @return weasis protocol encoded url built
	 */
	private static String buildMicroDicomProtocolCommand(String dicomQuery) {
		return MicroDicomCommandName.LAUNCH_URL_MICRODICOM_COMMAND.formatted(dicomQuery);
	}

}
