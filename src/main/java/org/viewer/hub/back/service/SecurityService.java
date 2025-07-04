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

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.manifest.Manifest;

public interface SecurityService {

	/**
	 * Handle the authentication to set in the Weasis manifest
	 * @param manifest Manifest to fill
	 * @param authentication Authentication if request is authenticated
	 */
	void handleManifestAuthentication(Manifest manifest, Authentication authentication);

}
