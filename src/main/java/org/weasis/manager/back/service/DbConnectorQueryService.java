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

package org.weasis.manager.back.service;

import org.weasis.manager.back.model.SearchCriteria;
import org.weasis.manager.back.model.manifest.Manifest;
import org.weasis.manager.back.model.property.ConnectorProperty;

import java.util.Set;

/**
 * Used to make requests to the different configured DB connectors
 */
public interface DbConnectorQueryService {

	/**
	 * Fill manifest from patients ids requests with DB connector
	 * @param manifest Manifest to fill
	 * @param patientIds Patient ids to look for
	 * @param connector Connector properties
	 * @param searchCriteria Search criteria
	 */
	void buildFromPatientIdsDbConnector(Manifest manifest, Set<String> patientIds, ConnectorProperty connector,
			SearchCriteria searchCriteria);

	/**
	 * Fill manifest from study instance uids requests with DB connector
	 * @param manifest Manifest to fill
	 * @param studyInstanceUids Study instance uids to look for
	 * @param connector Connector properties
	 */
	void buildFromStudyInstanceUidsDbConnector(Manifest manifest, Set<String> studyInstanceUids,
			ConnectorProperty connector);

	/**
	 * Fill manifest from study accession numbers requests with DB connector
	 * @param manifest Manifest to fill
	 * @param studyAccessionNumbers Study accession numbers to look for
	 * @param connector Connector properties
	 */
	void buildFromStudyAccessionNumbersDbConnector(Manifest manifest, Set<String> studyAccessionNumbers,
			ConnectorProperty connector);

	/**
	 * Fill manifest from serie instance uids requests with DB connector
	 * @param manifest Manifest to fill
	 * @param seriesInstanceUids Serie instance uids numbers to look for
	 * @param connector Connector properties
	 */
	void buildFromSeriesInstanceUidsDbConnector(Manifest manifest, Set<String> seriesInstanceUids,
			ConnectorProperty connector);

	/**
	 * Fill manifest from sop instance uids requests with DB connector
	 * @param manifest Manifest to fill
	 * @param sopInstanceUids Sop instance uids numbers to look for
	 * @param connector Connector properties
	 */
	void buildFromSopInstanceUidsDbConnector(Manifest manifest, Set<String> sopInstanceUids,
			ConnectorProperty connector);

}
