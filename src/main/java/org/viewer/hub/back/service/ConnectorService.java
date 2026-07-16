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

package org.viewer.hub.back.service;

import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

import java.util.LinkedHashSet;

/**
 * Used to make requests to the different configured connectors
 */
public interface ConnectorService {

	/**
	 * Retrieve the connector corresponding to connector id
	 * @param connectorId Connector id to evaluate
	 * @return ConnectorProperty found
	 */
	ConnectorProperty retrieveConnectorFromId(String connectorId);

	/**
	 * Retrieve the connector properties from the list of archives id in parameter
	 * @param archives Archive to evaluate
	 * @return LinkedHashSet of connector properties
	 */
	LinkedHashSet<ConnectorProperty> retrieveConnectors(LinkedHashSet<String> archives);

	/**
	 * Retrieve first default or first specific connector depending on archive value in
	 * Search Criteria
	 * @param searchCriteria Search Criteria to evaluate
	 * @return archive found
	 */
	String retrieveFirstDefaultOrFirstSpecificConnector(SearchCriteria searchCriteria);

}
