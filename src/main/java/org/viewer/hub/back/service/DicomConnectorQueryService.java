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

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.property.ConnectorProperty;

import java.util.Set;

/**
 * Used to make requests to the different configured dicom/dicom-web connectors
 */
public interface DicomConnectorQueryService {

	/**
	 * Retrieve patients from patients ids requests with dicom/dicom-web connector
	 * @param patientIds Patient ids to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	Set<Patient> retrievePatientsFromPatientIdsDicomConnector(Set<String> patientIds,
			@Valid ConnectorProperty connector, Authentication authentication);

	/**
	 * Retrieve patients from study instance uids requests with dicom/dicom-web connector
	 * @param studyInstanceUids Study instance uids to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	Set<Patient> retrievePatientsFromStudyInstanceUidsDicomConnector(Set<String> studyInstanceUids,
			@Valid ConnectorProperty connector, Authentication authentication);

	/**
	 * Retrieve patients from study accession numbers requests with dicom/dicom-web
	 * connector
	 * @param studyAccessionNumbers Study accession numbers to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	Set<Patient> retrievePatientsFromStudyAccessionNumbersDicomConnector(Set<String> studyAccessionNumbers,
			@Valid ConnectorProperty connector, Authentication authentication);

	/**
	 * Retrieve patients from serie instance uids requests with dicom/dicom-web connector
	 * @param seriesInstanceUids Serie instance uids numbers to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	Set<Patient> retrievePatientsFromSeriesInstanceUidsDicomConnector(Set<String> seriesInstanceUids,
			@Valid ConnectorProperty connector, Authentication authentication);

	/**
	 * Retrieve patients from sop instance uids requests with dicom/dicom-web connector
	 * @param sopInstanceUids Sop instance uids numbers to look for
	 * @param connector Connector properties
	 * @param authentication Authentication
	 */
	Set<Patient> retrievePatientsFromSopInstanceUidsDicomConnector(Set<String> sopInstanceUids,
			@Valid ConnectorProperty connector, Authentication authentication);

}
