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
 * Service used to launch the application OHIF
 */
public interface OhifDisplayService {

	/**
	 * Retrieve url which will launch OHIF
	 * @param searchCriteria Search criteria
	 * @param patientsByArchive Map of patients grouped by archive (archiveId, Set of patients found from this archive)
	 * @param authentication Authentication of the user
	 * @return URL which will launch OHIF
	 */
	String retrieveOhifLaunchUrl(SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive, Authentication authentication);

}
