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

package org.viewer.hub.front.views.viewerhub.association;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.model.*;
import org.viewer.hub.back.service.ViewerAssociationService;

/**
 * Logic managing associations
 */
@Service
public class ViewerAssociationLogic {

	// View
	private ViewerAssociationView viewerAssociationView;

	// Service
	private final ViewerAssociationService viewerAssociationService;

	@Autowired
	public ViewerAssociationLogic(final ViewerAssociationService viewerAssociationService) {
		this.viewerAssociationService = viewerAssociationService;
		this.viewerAssociationView = null;
	}

	/**
	 * Retrieve data from backend and transform into list of models
	 * @return List of models to display
	 */
	public Page<ViewerAssociationModel> retrieveAssociationModels(Pageable pageable) {
		return this.viewerAssociationService.retrieveViewerAssociationModels(pageable);
	}

	/**
	 * Following a change in the grid: update the values in backend
	 * @param viewerAssociationModel Values to update
	 * @return true if target has been updated
	 */
	public boolean updateViewerAssociationModel(ViewerAssociationModel viewerAssociationModel) {
		return this.viewerAssociationService.update(viewerAssociationModel);
	}

	/**
	 * Count viewer association models
	 * @return count viewer association models
	 */
	public int countAssociationModels() {
		return this.viewerAssociationService.countViewerAssociationModels();
	}

	/**
	 * Create a target in backend
	 * @param viewerAssociationModel ViewerAssociationModel to create
	 * @return true if target has been created
	 */
	public boolean addViewerAssociationModel(@Valid ViewerAssociationModel viewerAssociationModel) {
		return this.viewerAssociationService.createViewerAssociationModel(viewerAssociationModel);
	}

	/**
	 * Delete a target in backend
	 * @param viewerAssociationModel ViewerAssociationModel to delete
	 */
	public void deleteViewerAssociationModel(@Valid ViewerAssociationModel viewerAssociationModel) {
		this.viewerAssociationService.deleteViewerAssociationModel(viewerAssociationModel);
	}

	public void updatePriority(ViewerAssociationModel draggedItem, int value) {
		this.viewerAssociationService.updatePriority(draggedItem, value);
	}

	public ViewerAssociationView getAssociationView() {
		return this.viewerAssociationView;
	}

	public void setAssociationView(ViewerAssociationView viewerAssociationView) {
		this.viewerAssociationView = viewerAssociationView;
	}
}
