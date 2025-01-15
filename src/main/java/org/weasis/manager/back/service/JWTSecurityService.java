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

package org.weasis.manager.back.service;

import java.security.Principal;
import java.util.Optional;

public interface JWTSecurityService {

	/**
	 * Extracts the user's client ID (the client is the application the user is logged in
	 * with) from the object that represents the current user logged.
	 * @param principal : the current user logged
	 * @return the claim AZP from the principal
	 */
	Optional<String> getClientId(Principal principal);

	/**
	 * Extracts a user's claim from the object that represents the current user logged.
	 * @param principal : the current user logged
	 * @param claim : claim to extract
	 * @return the claim from the principal
	 */
	Optional<String> extractClaimFromPrincipal(Principal principal, String claim);

}
