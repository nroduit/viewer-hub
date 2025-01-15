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

import org.springframework.security.core.Authentication;
import org.weasis.manager.back.model.SearchCriteria;

/**
 * Service used to launch the application Weasis
 */
public interface DisplayService {

	/**
	 * Retrieve url which will launch Weasis with the key of the manifest in the cache
	 * @param searchCriteria search criteria
	 * @param authentication Authentication
	 * @return url which will launch Weasis
	 */
	String retrieveWeasisLaunchUrl(SearchCriteria searchCriteria, Authentication authentication);

}
