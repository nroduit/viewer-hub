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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.enums.ConnectorAuthType;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.property.ConnectorAuthenticationProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.property.DbConnectorProperty;
import org.viewer.hub.back.model.property.DicomConnectorProperty;
import org.viewer.hub.back.model.property.DicomWebConnectorProperty;

import java.util.Objects;

/**
 * ValidConnectorPropertyValidator: used to check valid connector properties configuration
 */
@Slf4j
@Component
public class ValidConnectorPropertyValidator implements ConstraintValidator<ValidConnectorProperty, ConnectorProperty> {

	private final ClientRegistrationRepository clientRegistrationRepository;

	public ValidConnectorPropertyValidator(ClientRegistrationRepository clientRegistrationRepository) {
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Override
	public boolean isValid(ConnectorProperty connector, ConstraintValidatorContext constraintValidatorContext) {
		if (connector == null || connector.getType() == null) {
			return false;
		}
		// Case database configuration
		else if (Objects.equals(connector.getType(), ConnectorType.DB)) {
			return isDbConnectorValid(connector.getDbConnector());
		}
		// Case dicom web configuration
		else if (Objects.equals(connector.getType(), ConnectorType.DICOM_WEB)) {
			return isDicomWebConnectorValid(connector.getDicomWebConnector());
		}
		// Case dicom configuration
		else if (Objects.equals(connector.getType(), ConnectorType.DICOM)) {
			return isDicomConnectorValid(connector.getDicomConnector());
		}
		else {
			return false;
		}
	}

	/**
	 * Validate the dicom connector properties
	 * @param dicomConnector Properties to evaluate
	 * @return true if valid
	 */
	private boolean isDicomConnectorValid(DicomConnectorProperty dicomConnector) {
		return dicomConnector != null && dicomConnector.getDimse() != null
				&& StringUtils.isNotBlank(dicomConnector.getDimse().getAet())
				&& StringUtils.isNotBlank(dicomConnector.getDimse().getHost())
				&& dicomConnector.getDimse().getPort() != 0
				&& StringUtils.isNotBlank(dicomConnector.getDimse().getCallingAet()) && dicomConnector.getWado() != null
				&& isAuthenticationValid(dicomConnector.getWado().getAuthentication());
	}

	/**
	 * Validate the dicom web connector properties
	 * @param dicomWebConnector Properties to evaluate
	 * @return true if valid
	 */
	private boolean isDicomWebConnectorValid(DicomWebConnectorProperty dicomWebConnector) {
		//
		return dicomWebConnector != null
				// Currently not mandatory because Wado-Rs /metadata has been replaced by
				// Qido-Rs
				/*
				 * && dicomWebConnector.getWadoRs() != null &&
				 * isAuthenticationValid(dicomWebConnector.getWadoRs().getAuthentication()
				 * )
				 */
				&& dicomWebConnector.getQidoRs() != null
				&& isAuthenticationValid(dicomWebConnector.getQidoRs().getAuthentication());
	}

	/**
	 * Validate the db connector properties
	 * @param dbConnector Properties to evaluate
	 * @return true if valid
	 */
	private boolean isDbConnectorValid(DbConnectorProperty dbConnector) {
		return dbConnector != null && StringUtils.isNotBlank(dbConnector.getUser())
				&& StringUtils.isNotBlank(dbConnector.getPassword()) && StringUtils.isNotBlank(dbConnector.getUri())
				&& StringUtils.isNotBlank(dbConnector.getDriver()) && dbConnector.getQuery() != null
				&& StringUtils.isNotBlank(dbConnector.getQuery().getSelect())
				&& StringUtils.isNotBlank(dbConnector.getQuery().getAccessionNumberColumn())
				&& StringUtils.isNotBlank(dbConnector.getQuery().getPatientIdColumn())
				&& StringUtils.isNotBlank(dbConnector.getQuery().getStudyInstanceUidColumn())
				&& StringUtils.isNotBlank(dbConnector.getQuery().getSerieInstanceUidColumn())
				&& StringUtils.isNotBlank(dbConnector.getQuery().getSopInstanceUidColumn())
				&& dbConnector.getWado() != null && isAuthenticationValid(dbConnector.getWado().getAuthentication());
	}

	/**
	 * Validate the authentication properties
	 * @param authentication Authentication
	 * @return true if valid
	 */
	private boolean isAuthenticationValid(ConnectorAuthenticationProperty authentication) {
		boolean isAuthenticationValid = false;

		if (authentication == null || authentication.getType() == null) {
			return false;
		}
		else if (Objects.equals(authentication.getType(), ConnectorAuthType.OAUTH2)) {
			if (authentication.getOauth2() == null || StringUtils.isBlank(authentication.getOauth2().getOidcId())
					|| clientRegistrationRepository
						.findByRegistrationId(authentication.getOauth2().getOidcId()) == null) {
				return false;
			}
			else {
				if (Objects.equals(AuthorizationGrantType.CLIENT_CREDENTIALS,
						clientRegistrationRepository.findByRegistrationId(authentication.getOauth2().getOidcId())
							.getAuthorizationGrantType())) {
					isAuthenticationValid = authentication.getOauth2().getServer() != null
							&& StringUtils.isNotBlank(authentication.getOauth2().getServer().getUrl());
				}
				// If authorization code flow, both oauth2 and basic should be defined as
				// if request not authenticated by default use basic authentication
				else if (Objects.equals(AuthorizationGrantType.AUTHORIZATION_CODE,
						clientRegistrationRepository.findByRegistrationId(authentication.getOauth2().getOidcId())
							.getAuthorizationGrantType())) {
					isAuthenticationValid = authentication.getOauth2().getServer() != null
							&& StringUtils.isNotBlank(authentication.getOauth2().getServer().getUrl())
							&& isAuthenticationBasicValid(authentication);
				}
				else {
					// No other flow currently implemented
					return false;
				}
			}
		}
		else if (Objects.equals(authentication.getType(), ConnectorAuthType.BASIC)) {
			isAuthenticationValid = isAuthenticationBasicValid(authentication);
		}

		return isAuthenticationValid;
	}

	/**
	 * Validate the basic authentication properties
	 * @param authentication Authentication
	 * @return true if valid
	 */
	private static boolean isAuthenticationBasicValid(ConnectorAuthenticationProperty authentication) {
		return authentication.getBasic() != null && StringUtils.isNotBlank(authentication.getBasic().getLogin())
				&& StringUtils.isNotBlank(authentication.getBasic().getPassword())
				&& authentication.getBasic().getServer() != null
				&& StringUtils.isNotBlank(authentication.getBasic().getServer().getUrl());
	}

}
