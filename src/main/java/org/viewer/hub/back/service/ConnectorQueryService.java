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

package org.viewer.hub.back.service;

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.ConnectorProperty;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Used to make requests to the different configured connectors
 */
public interface ConnectorQueryService {

	/**
	 * Fill manifest from patients ids requests
	 * @param manifest Manifest to fill
	 * @param patientIds Patient ids to look for
	 * @param searchCriteria Search criteria
	 * @param authentication Authentication
	 */
	void buildFromPatientIds(Manifest manifest, Set<String> patientIds, SearchCriteria searchCriteria,
			Authentication authentication);

	/**
	 * Fill manifest from study instance uids requests
	 * @param manifest Manifest to fill
	 * @param studyInstanceUids Study instance uids to look for
	 * @param archives Archives
	 * @param authentication Authentication
	 */
	void buildFromStudyInstanceUids(Manifest manifest, Set<String> studyInstanceUids, LinkedHashSet<String> archives,
			Authentication authentication);

	/**
	 * Fill manifest from study accession numbers requests
	 * @param manifest Manifest to fill
	 * @param studyAccessionNumbers Study accession numbers to look for
	 * @param archives Archives
	 * @param authentication Authentication
	 */
	void buildFromStudyAccessionNumbers(Manifest manifest, Set<String> studyAccessionNumbers,
			LinkedHashSet<String> archives, Authentication authentication);

	/**
	 * Fill manifest from serie instance uids requests
	 * @param manifest Manifest to fill
	 * @param seriesInstanceUids Serie instance uids numbers to look for
	 * @param archives Archives
	 * @param authentication Authentication
	 */
	void buildFromSeriesInstanceUids(Manifest manifest, Set<String> seriesInstanceUids, LinkedHashSet<String> archives,
			Authentication authentication);

	/**
	 * Fill manifest from sop instance uids requests
	 * @param manifest Manifest to fill
	 * @param sopInstanceUids Sop instance uids numbers to look for
	 * @param archives Archives
	 * @param authentication Authentication
	 */
	void buildFromSopInstanceUids(Manifest manifest, Set<String> sopInstanceUids, LinkedHashSet<String> archives,
			Authentication authentication);

	/**
	 * Retrieve the connector corresponding to connector id
	 * @param connectorId Connector id to evaluate
	 * @return ConnectorProperty found
	 */
	ConnectorProperty retrieveConnectorFromId(String connectorId);

}
