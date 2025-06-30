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

import java.util.Set;

/**
 * Used to make requests to the different configured dicom/dicom-web connectors
 */
public interface DicomConnectorQueryService {

	/**
	 * Fill manifest from patients ids requests with dicom/dicom-web connector
	 * @param manifest Manifest to fill
	 * @param patientIds Patient ids to look for
	 * @param connector Connector properties
	 * @param searchCriteria Search criteria
	 * @param authentication Authentication
	 */
	void buildFromPatientIdsDicomConnector(Manifest manifest, Set<String> patientIds, ConnectorProperty connector,
			SearchCriteria searchCriteria, Authentication authentication);

	/**
	 * Fill manifest from study instance uids requests with dicom/dicom-web connector
	 * @param manifest Manifest to fill
	 * @param studyInstanceUids Study instance uids to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	void buildFromStudyInstanceUidsDicomConnector(Manifest manifest, Set<String> studyInstanceUids,
			ConnectorProperty connector, Authentication authentication);

	/**
	 * Fill manifest from study accession numbers requests with dicom/dicom-web connector
	 * @param manifest Manifest to fill
	 * @param studyAccessionNumbers Study accession numbers to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	void buildFromStudyAccessionNumbersDicomConnector(Manifest manifest, Set<String> studyAccessionNumbers,
			ConnectorProperty connector, Authentication authentication);

	/**
	 * Fill manifest from serie instance uids requests with dicom/dicom-web connector
	 * @param manifest Manifest to fill
	 * @param seriesInstanceUids Serie instance uids numbers to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	void buildFromSeriesInstanceUidsDicomConnector(Manifest manifest, Set<String> seriesInstanceUids,
			ConnectorProperty connector, Authentication authentication);

	/**
	 * Fill manifest from sop instance uids requests with dicom/dicom-web connector
	 * @param manifest Manifest to fill
	 * @param sopInstanceUids Sop instance uids numbers to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	void buildFromSopInstanceUidsDicomConnector(Manifest manifest, Set<String> sopInstanceUids,
			ConnectorProperty connector, Authentication authentication);

}
