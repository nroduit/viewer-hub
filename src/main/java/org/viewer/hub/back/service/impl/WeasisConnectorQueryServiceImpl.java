/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.enums.QueryLevelType;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.DbConnectorQueryService;
import org.viewer.hub.back.service.DicomConnectorQueryService;
import org.viewer.hub.back.service.WeasisConnectorQueryService;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class WeasisConnectorQueryServiceImpl implements WeasisConnectorQueryService {

	private final DbConnectorQueryService dbConnectorQueryService;

	private final DicomConnectorQueryService dicomConnectorQueryService;

	private final ConnectorService connectorService;

	@Autowired
	public WeasisConnectorQueryServiceImpl(final DbConnectorQueryService dbConnectorQueryService,
			final DicomConnectorQueryService dicomConnectorQueryService, final ConnectorService connectorService) {
		this.dbConnectorQueryService = dbConnectorQueryService;
		this.dicomConnectorQueryService = dicomConnectorQueryService;
		this.connectorService = connectorService;
	}

	@Override
	public void buildFromPatientIds(Manifest manifest, Set<String> patientIds, @Valid SearchCriteria searchCriteria,
			Authentication authentication) {
		// Retrieve default or specific connectors
		this.connectorService.retrieveConnectors(searchCriteria.getArchive()).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.PATIENT_ID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					manifest.update(this.dbConnectorQueryService.retrievePatientsFromPatientIdsDbConnector(patientIds,
							connector), connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
							patientIds, connector, authentication), connector);
				}
			}
		});
	}

	@Override
	public void buildFromStudyInstanceUids(Manifest manifest, Set<String> studyInstanceUids,
			LinkedHashSet<String> archives, Authentication authentication) {
		// Retrieve default or specific connectors
		this.connectorService.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.STUDY_INSTANCE_UID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dbConnectorQueryService
						.retrievePatientsFromStudyInstanceUidsDbConnector(studyInstanceUids, connector), connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dicomConnectorQueryService.retrievePatientsFromStudyInstanceUidsDicomConnector(
							studyInstanceUids, connector, authentication), connector);
				}
			}
		});
	}

	@Override
	public void buildFromStudyAccessionNumbers(Manifest manifest, Set<String> studyAccessionNumbers,
			LinkedHashSet<String> archives, Authentication authentication) {
		// Retrieve default or specific connectors
		this.connectorService.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.STUDY_ACCESSION_NUMBER)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dbConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDbConnector(
							studyAccessionNumbers, connector), connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					// Update manifest with patients found
					manifest
						.update(this.dicomConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDicomConnector(
								studyAccessionNumbers, connector, authentication), connector);
				}
			}
		});
	}

	@Override
	public void buildFromSeriesInstanceUids(Manifest manifest, Set<String> seriesInstanceUids,
			LinkedHashSet<String> archives, Authentication authentication) {
		// Retrieve default or specific connectors
		this.connectorService.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.SERIE_INSTANCE_UID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dbConnectorQueryService
						.retrievePatientsFromSeriesInstanceUidsDbConnector(seriesInstanceUids, connector), connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					// Update manifest with patients found
					manifest
						.update(this.dicomConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDicomConnector(
								seriesInstanceUids, connector, authentication), connector);
				}
			}
		});
	}

	@Override
	public void buildFromSopInstanceUids(Manifest manifest, Set<String> sopInstanceUids, LinkedHashSet<String> archives,
			Authentication authentication) {
		// Retrieve default or specific connectors
		this.connectorService.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.SOP_INSTANCE_UID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dbConnectorQueryService
						.retrievePatientsFromSopInstanceUidsDbConnector(sopInstanceUids, connector), connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					// Update manifest with patients found
					manifest.update(this.dicomConnectorQueryService.retrievePatientsFromSopInstanceUidsDicomConnector(
							sopInstanceUids, connector, authentication), connector);
				}
			}
		});
	}

}
