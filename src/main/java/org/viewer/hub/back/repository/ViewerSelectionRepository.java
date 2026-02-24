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

import java.util.List;
import java.util.Optional;

/**
 * Repository for the entity ViewerSelection.
 */
public interface ViewerSelectionRepository extends JpaRepository<ViewerSelectionEntity, Long> {

	/**
	 * Find all viewer selections by archive
	 * @param archive archive to look for
	 * @return list of viewer selections
	 */
	List<ViewerSelectionEntity> findByArchive(String archive);

	/**
	 * Find all viewer selections by archive and viewer
	 * @param archive archive to look for
	 * @param viewer viewer to look for
	 * @return list of viewer selections
	 */
	List<ViewerSelectionEntity> findByArchiveAndViewer(String archive, ViewerType viewer);

	/**
	 * Find the first viewer selection by viewer
	 * @param viewer viewer to look for
	 * @return optional of viewer selection
	 */
	Optional<ViewerSelectionEntity> findFirstByViewer(ViewerType viewer);

}
