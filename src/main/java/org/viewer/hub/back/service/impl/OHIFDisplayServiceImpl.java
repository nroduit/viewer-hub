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
import org.viewer.hub.back.model.searchcriteria.*;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.OHIFDisplayService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OHIFDisplayServiceImpl implements OHIFDisplayService {

	@Value("${ohif.server.url}")
	private String ohifServerUrl;

	@Override
	public String retrieveDicomUrl(SearchCriteria searchCriteria, String archive) {
		String url = ohifServerUrl + "/viewer";

		if (archive != null && !archive.isEmpty()) {
			url += "/" + archive;
		}

		List<String> query = searchCriteria instanceof IHESearchCriteria
				? getQuery((IHESearchCriteria) searchCriteria)
				: getQuery((ArchiveSearchCriteria) searchCriteria);

		for (int i=0; i<query.size(); i++) {
			url += i==0 ? "?" : "&";
			url += query.get(i);
		}
		return url;
	}

	private List<String> getQuery(IHESearchCriteria iheSearchCriteria) {
		List<String> query = new ArrayList<>();
		if (!iheSearchCriteria.getStudyUID().isEmpty()) {
			query.add("StudyInstanceUIDs=" + String.join(",", iheSearchCriteria.getStudyUID()));
		}
		return query;
	}

	private List<String> getQuery(ArchiveSearchCriteria archiveSearchCriteria) {
		List<String> args = new ArrayList<>();
		if (!archiveSearchCriteria.getStudyUID().isEmpty()) {
			args.add("StudyInstanceUIDs=" + String.join(",", archiveSearchCriteria.getStudyUID()));
		}
		if (!archiveSearchCriteria.getSeriesUID().isEmpty()) {
			args.add("SeriesInstanceUIDs=" + String.join(",", archiveSearchCriteria.getSeriesUID()));
		}
		return args;
	}

}
