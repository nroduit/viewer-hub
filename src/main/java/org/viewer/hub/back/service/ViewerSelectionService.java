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
import org.springframework.data.domain.Sort;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

import java.util.List;
import java.util.Map;
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
	 * Check for duplicate viewer selection based on provided parameters
	 * @param archive   the archive identifier
	 * @param viewer    the viewer type
	 * @param modalities list of modality types
	 * @param excludeId id to exclude from the check (useful during updates)
	 * @return true if a duplicate exists, false otherwise
	 */
	boolean checkDuplicate(String archive, ViewerType viewer, List<ModalityType> modalities, Long excludeId);

	/** Retrieve all viewer selection entities sorted by priority
	 * @param prioritySortDirection direction to sort by priority (ASC or DESC)
	 * @return list of viewer selection entities sorted by priority
	 */
	List<ViewerSelectionEntity> retrieveViewerSelection(Sort.Direction prioritySortDirection);

	/**
	 * Retrieve a viewer selection rule based on provided parameters
	 * @param searchCriteria Search criteria of the request
	 * @param patientsByArchive       Set of patients retrieved gather by archive
	 * @return the matching ViewerSelectionEntity
	 */
	ViewerSelectionEntity retrieveViewerSelectionRule(SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive);

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
