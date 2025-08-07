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

import jakarta.validation.Valid;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

/**
 * Service used to launch the application Weasis
 */
public interface SlicerDisplayService {

	/**
	 * Retrieve url which will launch 3D Slicer
	 * @param searchCriteria search criteria
	 * @param archive archive
	 * @return url which will launch 3D Slicer
	 */
	String retrieveSlicerQidoLaunchUrl(@Valid SearchCriteria searchCriteria, String archive);

}
