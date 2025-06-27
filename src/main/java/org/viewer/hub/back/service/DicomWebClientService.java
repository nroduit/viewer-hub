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

import org.springframework.web.reactive.function.client.WebClient;
import org.viewer.hub.back.model.property.ConnectorProperty;

public interface DicomWebClientService {

	/**
	 * WebClient wado-rs configuration: depending on the configuration will create a
	 * webClient for the wado-rs based on the authentication method selected
	 * @param connectorProperty Property to evaluate
	 * @return WebClient wado-rs
	 */
	WebClient buildWebClientWadoRs(ConnectorProperty connectorProperty);

	/**
	 * WebClient qido-rs configuration: depending on the configuration will create a
	 * webClient for the qido-rs based on the authentication method selected
	 * @param connectorProperty Property to evaluate
	 * @return WebClient qido-rs
	 */
	WebClient buildWebClientQidoRs(ConnectorProperty connectorProperty);

}
