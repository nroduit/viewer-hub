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

package org.viewer.hub.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ViewerType;

/**
 * Repository for the entity Target.
 */
public interface ViewerSelectionRepository extends JpaRepository<ViewerSelectionEntity, Long> {

	/**
	 * Check if the target corresponding to the archive in parameter exists
	 * @param modality modality to look for
	 * @param archive archive to look for
	 * @return true if the target with the given target name exists
	 */
	boolean existsByModalityAndArchive(String modality, String archive);

	/**
	 * Check if the target corresponding to the archive in parameter exists
	 * @param modality modality to look for
	 * @param archive archive to look for
	 * @param viewer viewer to look for
	 * @return true if the target with the given target name exists
	 */
	boolean existsByModalityAndArchiveAndViewer(String modality, String archive, ViewerType viewer);

}
