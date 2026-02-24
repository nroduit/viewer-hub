/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
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
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

import java.util.Map;
import java.util.Set;

/**
 * Service managing Weasis manifest
 */
public interface WeasisService {

	/**
	 * Build manifest and set it in the cache
	 * @param key Cache key
	 * @param searchCriteria Criteria to build the manifest
	 * @param patientsByArchive Map of patients grouped by archive (archiveId, Set of patients found from this archive).
	 *                          Used to build manifest from already fetched patients.
	 *                          If null will fetch patients from connectors based on search criteria
	 * @param authentication Authentication
	 */
	void buildManifest(String key, SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive, Authentication authentication);

	/**
	 * Retrieve manifest from the cache
	 * @param key Cache key
	 */
	Manifest retrieveManifest(String key);

}
