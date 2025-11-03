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

package org.viewer.hub.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.viewer.hub.back.model.ViewerAssociationModel;

/**
 * Repository for the entity Target.
 */
public interface ViewerAssociationRepository extends JpaRepository<ViewerAssociationModel, Long> {

	/**
	 * Check if the target corresponding to the archive in parameter exists
	 * @param aet aet to look for
	 * @param archive archive to look for
	 * @return true if the target with the given target name exists
	 */
	boolean existsByAetAndArchive(String aet, String archive);

}
