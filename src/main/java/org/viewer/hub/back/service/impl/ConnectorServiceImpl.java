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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.ConnectorService;

import java.util.LinkedHashSet;
import java.util.Objects;
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
	public LinkedHashSet<ConnectorProperty> retrieveConnectors(LinkedHashSet<String> archives) {
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

	@Override
	public String retrieveFirstDefaultOrFirstSpecificConnector(SearchCriteria searchCriteria) {
		ConnectorProperty connectorProperty = this
			.retrieveConnectors(searchCriteria != null ? searchCriteria.getArchive() : new LinkedHashSet<>())
			.stream()
			.findFirst()
			.orElse(null);
		return connectorProperty != null ? connectorProperty.getId() : null;
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
