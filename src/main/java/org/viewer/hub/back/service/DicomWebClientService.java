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
