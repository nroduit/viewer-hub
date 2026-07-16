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
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

import java.util.Map;
import java.util.Set;

/**
 * Service used to launch the application Weasis
 */
public interface WeasisDisplayService {

	/**
	 * Retrieve url which will launch Weasis with the key of the manifest in the cache. If
	 * patientsByArchive is null this method will call the connector service to get the
	 * list of patients matching the search criteria. If patientsByArchive is not null
	 * this method will not call the connector service: the list of patients matching the
	 * search criteria in parameter will be used to fill the manifest.
	 * @param searchCriteria search criteria
	 * @param patientsByArchive map of patients by archive
	 * @param authentication Authentication
	 * @return url which will launch Weasis
	 */
	String retrieveWeasisLaunchUrl(SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive,
			Authentication authentication);

}
