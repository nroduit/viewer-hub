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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.ViewerAssociationModel;

import java.util.List;
import java.util.Set;

public interface ViewerAssociationService {

	/**
	 * Retrieve the association models depending on the filters
	 * @return association models found
	 */
	Page<ViewerAssociationModel> retrieveViewerAssociationModels(Pageable pageable);

	List<ViewerAssociationModel> retrieveViewerAssociationModels();

	ViewerAssociationModel getViewerAssociation(String archive, Set<String> accessionNumber, Set<String> studyUID, Set<String> seriesUID, Authentication authentication);
	/**
	 * Count the number of association models corresponding to the filters
	 * @return count number
	 */
	int countViewerAssociationModels();

	/**
	 * Update values in backend for BelongToMemberOf column
	 * @param viewerAssociationModel viewer association to save
	 * @return true if it has been updated
	 * of backend
	 */
	boolean update(ViewerAssociationModel viewerAssociationModel);

	/**
	 * Create a target entity if name not already existing
	 * @param viewerAssociationModel ViewerAssociationModel to create
	 * @return true if it has been created
	 */
	boolean createViewerAssociationModel(ViewerAssociationModel viewerAssociationModel);

	/**
	 * Delete a target entity
	 * @param viewerAssociationModel ViewerAssociationModel to delete
	 */
	void deleteViewerAssociationModel(ViewerAssociationModel viewerAssociationModel);


	void updatePriority(ViewerAssociationModel draggedItem, int value);
}
