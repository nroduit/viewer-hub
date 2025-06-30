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