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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.enums.QueryLevelType;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.DbConnectorQueryService;
import org.viewer.hub.back.service.DicomConnectorQueryService;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConnectorQueryServiceImpl implements ConnectorQueryService {

	@Value("${connector.default}")
	private LinkedHashSet<String> defaultConnectors;

	private final ConnectorConfigurationProperties connectorConfigurationProperties;

	final DbConnectorQueryService dbConnectorQueryService;

	final DicomConnectorQueryService dicomConnectorQueryService;

	@Autowired
	public ConnectorQueryServiceImpl(final ConnectorConfigurationProperties connectorConfigurationProperties,
			final DbConnectorQueryService dbConnectorQueryService,
			final DicomConnectorQueryService dicomConnectorQueryService) {
		this.connectorConfigurationProperties = connectorConfigurationProperties;
		this.dbConnectorQueryService = dbConnectorQueryService;
		this.dicomConnectorQueryService = dicomConnectorQueryService;
	}

	@Override
	public void buildFromPatientIds(Manifest manifest, Set<String> patientIds, @Valid SearchCriteria searchCriteria,
			Authentication authentication) {
		// Retrieve default or specific connectors
		this.retrieveConnectors(searchCriteria.getArchive()).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.PATIENT_ID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					this.dbConnectorQueryService.buildFromPatientIdsDbConnector(manifest, patientIds, connector,
							searchCriteria);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					this.dicomConnectorQueryService.buildFromPatientIdsDicomConnector(manifest, patientIds, connector,
							searchCriteria, authentication);
				}
			}
		});
	}

	@Override
	public void buildFromStudyInstanceUids(Manifest manifest, Set<String> studyInstanceUids,
			LinkedHashSet<String> archives, Authentication authentication) {
		// Retrieve default or specific connectors
		this.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.STUDY_INSTANCE_UID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					this.dbConnectorQueryService.buildFromStudyInstanceUidsDbConnector(manifest, studyInstanceUids,
							connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					this.dicomConnectorQueryService.buildFromStudyInstanceUidsDicomConnector(manifest,
							studyInstanceUids, connector, authentication);
				}
			}
		});
	}

	@Override
	public void buildFromStudyAccessionNumbers(Manifest manifest, Set<String> studyAccessionNumbers,
			LinkedHashSet<String> archives, Authentication authentication) {
		// Retrieve default or specific connectors
		this.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.STUDY_ACCESSION_NUMBER)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					this.dbConnectorQueryService.buildFromStudyAccessionNumbersDbConnector(manifest,
							studyAccessionNumbers, connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					this.dicomConnectorQueryService.buildFromStudyAccessionNumbersDicomConnector(manifest,
							studyAccessionNumbers, connector, authentication);
				}
			}
		});
	}

	@Override
	public void buildFromSeriesInstanceUids(Manifest manifest, Set<String> seriesInstanceUids,
			LinkedHashSet<String> archives, Authentication authentication) {
		// Retrieve default or specific connectors
		this.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.SERIE_INSTANCE_UID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					this.dbConnectorQueryService.buildFromSeriesInstanceUidsDbConnector(manifest, seriesInstanceUids,
							connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					this.dicomConnectorQueryService.buildFromSeriesInstanceUidsDicomConnector(manifest,
							seriesInstanceUids, connector, authentication);
				}
			}
		});
	}

	@Override
	public void buildFromSopInstanceUids(Manifest manifest, Set<String> sopInstanceUids, LinkedHashSet<String> archives,
			Authentication authentication) {
		// Retrieve default or specific connectors
		this.retrieveConnectors(archives).forEach(connector -> {
			if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.SOP_INSTANCE_UID)) {
				if (Objects.equals(ConnectorType.DB, connector.getType())) {
					this.dbConnectorQueryService.buildFromSopInstanceUidsDbConnector(manifest, sopInstanceUids,
							connector);
				}
				else if (Objects.equals(ConnectorType.DICOM, connector.getType())
						|| Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
					this.dicomConnectorQueryService.buildFromSopInstanceUidsDicomConnector(manifest, sopInstanceUids,
							connector, authentication);
				}
			}
		});
	}

	/**
	 * Retrieve the connector from the connector id in parameter
	 * @param connectorId Connector id
	 * @return Connectors found
	 */
	@Override
	public ConnectorProperty retrieveConnectorFromId(String connectorId) {
		return this.connectorConfigurationProperties.getConnectors()
			.values()
			.stream()
			.filter(c -> Objects.equals(c.getId(), connectorId))
			.findFirst()
			.orElseThrow(() -> new TechnicalException("Connector id not existing:" + connectorId));
	}

	/**
	 * Retrieve the connector properties from the list of archives id in parameter
	 * @param archives Archive to evaluate
	 * @return List of connector properties
	 */
	LinkedHashSet<ConnectorProperty> retrieveConnectors(LinkedHashSet<String> archives) {
		// If archive list empty:
		// - if no default (or invalid default connector defined) parse defined default
		// ordered connectors config
		// otherwise use default connectors defined
		// - otherwise parse requested archives
		return archives.isEmpty()
				? this.areDefaultConnectorsValid() ? this.retrieveConnectorsFromIds(this.defaultConnectors)
						: new LinkedHashSet<>(this.connectorConfigurationProperties.getConnectors().values())
				: this.retrieveConnectorsFromIds(archives);
	}

	/**
	 * Check if the default connector property is filled and valid
	 * @return true if there are default connectors and they correspond to the connector
	 * configured
	 */
	private boolean areDefaultConnectorsValid() {
		return this.defaultConnectors != null && !this.defaultConnectors.isEmpty()
				&& this.defaultConnectors.stream()
					.allMatch(dc -> this.connectorConfigurationProperties.getConnectors()
						.values()
						.stream()
						.anyMatch(c -> Objects.equals(c.getId(), dc)));
	}

	/**
	 * Retrieve the connectors from the connector ids in parameter
	 * @param connectors Connectors
	 * @return Connectors found
	 */
	private LinkedHashSet<ConnectorProperty> retrieveConnectorsFromIds(LinkedHashSet<String> connectors) {
		return connectors.stream()
			.map(connector -> this.connectorConfigurationProperties.getConnectors()
				.values()
				.stream()
				.filter(c -> Objects.equals(c.getId(), connector))
				.findFirst()
				.orElseThrow(() -> new TechnicalException("Connector id not existing:" + connector)))
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

}
