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
import org.springframework.stereotype.Service;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.ConnectorService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConnectorServiceImpl implements ConnectorService {

	@Value("${connector.default}")
	private LinkedHashSet<String> defaultConnectors;

	private final ConnectorConfigurationProperties connectorConfigurationProperties;

	@Autowired
	public ConnectorServiceImpl(final ConnectorConfigurationProperties connectorConfigurationProperties) {
		this.connectorConfigurationProperties = connectorConfigurationProperties;
	}

	@Override
	public ConnectorProperty retrieveConnectorFromId(LinkedHashSet<String> archive) {
		// Retrieve default or specific connectors
		LinkedHashSet<ConnectorProperty> connectors = this.retrieveConnectors(archive);
		if (connectors.isEmpty()) {
			return null;
		}
		return connectors.getFirst();
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

	@Override
	public String getArchiveNameFromId(@Valid String connectorId) {
		// Retrieve default or specific connectors
		ConnectorProperty connector = this.retrieveConnectorFromId(connectorId);
		if (connector == null) {
			return null;
		}
		return connector.getName();
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

	@Override
	public String getDicomRsUrlFromId(@Valid String connectorId) {
		// Retrieve default or specific connectors
		ConnectorProperty connector = this.retrieveConnectorFromId(connectorId);
		if (connector.getDicomWebConnector() != null) {
			return connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getServer().getFullUrl();
		}
		return null;
	}

	@Override
	public String getAETFromId(@Valid String connectorId) {
		// Retrieve default or specific connectors
		ConnectorProperty connector = this.retrieveConnectorFromId(connectorId);
		if (connector.getDicomConnector() != null) {
			return connector.getDicomConnector().getDimse().getAet();
		}
		return null;
	}


	@Override
	public String[] getCredentialsFromId(@Valid String connectorId) {
		// Retrieve default or specific connectors
		ConnectorProperty connector = this.retrieveConnectorFromId(connectorId);
		if (connector.getDicomWebConnector() != null) {
			String[] credentials = new String[2];
			credentials[0] = connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getLogin();
			credentials[1] = connector.getDicomWebConnector().getQidoRs().getAuthentication().getBasic().getPassword();
			return credentials;
		}
		return null;
	}

}
