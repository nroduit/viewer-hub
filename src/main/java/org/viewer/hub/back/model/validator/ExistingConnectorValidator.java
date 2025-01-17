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

package org.viewer.hub.back.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.model.SearchCriteria;

/**
 * ExistingConnectorValidator: used to check that archives correspond to existing
 * connector ids
 */
@Component
public class ExistingConnectorValidator implements ConstraintValidator<ExistingConnector, SearchCriteria> {

	private final ConnectorConfigurationProperties connectorConfigurationProperties;

	@Autowired
	public ExistingConnectorValidator(ConnectorConfigurationProperties connectorConfigurationProperties) {
		this.connectorConfigurationProperties = connectorConfigurationProperties;
	}

	@Override
	public boolean isValid(SearchCriteria searchCriteria, ConstraintValidatorContext constraintValidatorContext) {
		return searchCriteria.getArchive() == null || searchCriteria.getArchive().isEmpty()
				|| searchCriteria.getArchive()
					.stream()
					.allMatch(this.connectorConfigurationProperties::containsConnectorId);
	}

}
