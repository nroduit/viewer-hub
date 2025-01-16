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
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;

/**
 * Service managing manifest
 */
public interface ManifestService {

	/**
	 * Build manifest and set it in the cache
	 * @param key Cache key
	 * @param searchCriteria Criteria to build the manifest
	 * @param authentication Authentication
	 */
	void buildManifest(String key, SearchCriteria searchCriteria, Authentication authentication);

	/**
	 * Retrieve manifest from the cache
	 * @param key Cache key
	 */
	Manifest retrieveManifest(String key);

}
