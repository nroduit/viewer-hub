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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.entity.ViewerSelectionEntity;

import java.util.List;
import java.util.Set;

/**
 * Service to handle Viewer Selection operations
 */
public interface ViewerSelectionService {

	/**
	 * Retrieve all viewer selection entities with pagination
	 * @param pageable pagination information
	 * @return paginated list of viewer selection entities
	 */
	Page<ViewerSelectionEntity> retrieveViewerSelection(Pageable pageable);

	/**
	 * Retrieve all viewer selection entities without pagination
	 * @return list of viewer selection entities
	 */
	List<ViewerSelectionEntity> retrieveViewerSelection();

	/**
	 * Retrieve a viewer selection rule based on provided parameters
	 * @param archive          the archive identifier
	 * @param accessionNumber  set of accession numbers
	 * @param studyUID         set of study UIDs
	 * @param seriesUID        set of series UIDs
	 * @param authentication   authentication information
	 * @return the matching ViewerSelectionEntity
	 */
	ViewerSelectionEntity retrieveViewerSelectionRule(String archive, Set<String> accessionNumber, Set<String> studyUID, Set<String> seriesUID, Authentication authentication);

	/**
	 * Count total number of viewer selection entities
	 * @return total count of viewer selection entities
	 */
	int countViewerSelection();

	/**
	 * Update an existing ViewerSelectionEntity
	 * @param viewerSelectionEntity item to update
	 * @return true if updated
	 */
	boolean update(ViewerSelectionEntity viewerSelectionEntity);

	/**
	 * Create a new ViewerSelectionEntity
	 * @param viewerSelectionEntity item to create
	 * @return true if created
	 */
	boolean createViewerSelection(ViewerSelectionEntity viewerSelectionEntity);

	/**
	 * Delete a ViewerSelectionEntity
	 * @param viewerSelectionEntity item to delete
	 */
	void deleteViewerSelection(ViewerSelectionEntity viewerSelectionEntity);

	/**
	 * Update priority of ViewerSelectionEntity
	 * @param viewerSelectionEntity item to update
	 * @param value new priority value
	 */
	void updatePriority(ViewerSelectionEntity viewerSelectionEntity, int value);
}
