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

package org.viewer.hub.back.config.properties;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.model.property.ConnectorProperty;

import java.util.LinkedHashMap;

/**
 * Used to retrieve the different connector configuration properties from config server
 * (DB, DICOM, DICOM_WEB)
 */
@ConfigurationProperties(prefix = "connector")
@Validated
public class ConnectorConfigurationProperties {

	@Valid
	private final LinkedHashMap<String, ConnectorProperty> config;

	/**
	 * Constructor
	 * @param config corresponds to the mapping of properties with the config server
	 */
	public ConnectorConfigurationProperties(LinkedHashMap<String, ConnectorProperty> config) {
		// Populate id for Connector property
		config.forEach((key, connectorProperty) -> connectorProperty.setId(key));
		this.config = config;
	}

	/**
	 * Retrieve connectors
	 * @return mapping between connector id and connector properties
	 */
	public LinkedHashMap<String, ConnectorProperty> getConnectors() {
		return this.config;
	}

	/**
	 * Check if the config in the config server contains the id in parameter
	 * @param connectorId Connector id to check
	 * @return true if the connector id exists
	 */
	public boolean containsConnectorId(String connectorId) {
		return this.config.containsKey(connectorId);
	}

}
