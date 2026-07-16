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
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

public interface DisplayService {

	/**
	 * Determine which viewer to display based on the provided search criteria and build
	 * the launch URL for the selected viewer.
	 * @param searchCriteria the criteria used determine the viewer to display
	 * @param authentication the authentication information
	 * @return the viewer launch URL
	 */
	String viewerLaunchUrl(SearchCriteria searchCriteria, Authentication authentication);

}
