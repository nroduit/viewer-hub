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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.OHIFDisplayService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OHIFDisplayServiceImpl implements OHIFDisplayService {

	// Services
	private final ConnectorService connectorService;

	@Value("${ohif.server.url}")
	private String ohifServerUrl;

	private static final String VIEWER_HUB_DATASOURCE = "viewer-hub";

	@Autowired
	public OHIFDisplayServiceImpl(final ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public String retrieveDicomUrl(ArchiveSearchCriteria archiveSearchCriteria, String archive) {
		String archiveName = connectorService.getArchiveNameFromId(archive);
		String url = ohifServerUrl + "/viewer";

		if (archiveName != null && !archiveName.isEmpty()) {
			url += "/" + archiveName + "/";
		}

		List<String> args = new ArrayList<>();
		if (!archiveSearchCriteria.getStudyUID().isEmpty()) {
			args.add("StudyInstanceUIDs=" + String.join(",", archiveSearchCriteria.getStudyUID()));
		}
		if (!archiveSearchCriteria.getSeriesUID().isEmpty()) {
			args.add("SeriesInstanceUIDs=" + String.join(",", archiveSearchCriteria.getSeriesUID()));
		}
		for (int i=0; i<args.size(); i++) {
			url += i==0 ? "?" : "&";
			url += args.get(i);
		}
		return url;
	}

}
