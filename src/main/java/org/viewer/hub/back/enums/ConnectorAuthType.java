package org.viewer.hub.back.enums;

import lombok.Getter;

/**
 * Define the type of authentication for the connector: basic, oauth2 (for
 * client_credential and authorization_code)
 */
@Getter
public enum ConnectorAuthType {

	// Basic authentication
	BASIC,
	// Oauth2 authentication
	OAUTH2;

}