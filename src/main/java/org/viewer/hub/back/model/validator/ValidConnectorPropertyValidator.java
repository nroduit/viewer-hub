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
import org.apache.commons.lang3.StringUtils;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.property.ConnectorProperty;

import java.util.Objects;

/**
 * ValidConnectorPropertyValidator: used to check valid connector properties configuration
 */
public class ValidConnectorPropertyValidator implements ConstraintValidator<ValidConnectorProperty, ConnectorProperty> {

	@Override
	public boolean isValid(ConnectorProperty connector, ConstraintValidatorContext constraintValidatorContext) {
		// Check wado: use to retrieve the image from the viewer
		// Both basic and oauth2 authorization are currently mandatory
		if (StringUtils.isBlank(connector.getWado().getAuthentication().getOauth2().getUrl())
				|| StringUtils.isBlank(connector.getWado().getAuthentication().getBasic().getUrl())) {
			return false;
		}

		// Case database configuration
		if (Objects.equals(connector.getType(), ConnectorType.DB) && connector.getDbConnector() != null
				&& StringUtils.isNotBlank(connector.getDbConnector().getDriver())
				&& StringUtils.isNotBlank(connector.getDbConnector().getUri())
				&& StringUtils.isNotBlank(connector.getDbConnector().getUser())
				&& StringUtils.isNotBlank(connector.getDbConnector().getPassword())
				&& connector.getDbConnector().getQuery() != null
				&& StringUtils.isNotBlank(connector.getDbConnector().getQuery().getSelect())
				&& StringUtils.isNotBlank(connector.getDbConnector().getQuery().getAccessionNumberColumn())
				&& StringUtils.isNotBlank(connector.getDbConnector().getQuery().getPatientIdColumn())
				&& StringUtils.isNotBlank(connector.getDbConnector().getQuery().getStudyInstanceUidColumn())
				&& StringUtils.isNotBlank(connector.getDbConnector().getQuery().getSerieInstanceUidColumn())
				&& StringUtils.isNotBlank(connector.getDbConnector().getQuery().getSopInstanceUidColumn())) {
			return true;
		}
		else {
			// Case dicom configuration
			return Objects.equals(connector.getType(), ConnectorType.DICOM) && connector.getDicomConnector() != null
					&& StringUtils.isNotBlank(connector.getDicomConnector().getAet())
					&& StringUtils.isNotBlank(connector.getDicomConnector().getHost())
					&& connector.getDicomConnector().getPort() != 0
					&& StringUtils.isNotBlank(connector.getDicomConnector().getCallingAet());
		}
	}

}
