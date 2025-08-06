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
import org.viewer.hub.back.model.property.ConnectorProperty;

import java.util.LinkedHashSet;

/**
 * Used to make requests to the different configured connectors
 */
public interface ConnectorService {

	/**
	 * get connector
	 * @param archive Archives to evaluate
	 */
	ConnectorProperty retrieveConnectorFromId(LinkedHashSet<String> archive);

	/**
	 * Retrieve the connector corresponding to connector id
	 * @param connectorId Connector id to evaluate
	 * @return ConnectorProperty found
	 */
	ConnectorProperty retrieveConnectorFromId(String connectorId);

	/**
	 * get name of connector
	 * @param connectorId Connector id to evaluate
	 */
	String getArchiveNameFromId(@Valid String connectorId);

	/**
	 * get dicom-rs url of dicom web request
	 * @param connectorId Connector id to evaluate
	 */
	String getDicomRsUrlFromId(@Valid String connectorId);

	/**
	 * get AET of dicom web request
	 * @param connectorId Connector id to evaluate
	 */
	String getAETFromId(@Valid String connectorId);

	/**
	 * get credentials of dicom web request
	 * @param connectorId Connector id to evaluate
	 */
	String[] getCredentialsFromId(@Valid String connectorId);

}
