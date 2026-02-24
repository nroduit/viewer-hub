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

package org.viewer.hub.front.views.viewer.selection;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.service.ViewerSelectionService;

import java.util.List;

/**
 * Logic managing viewer selection
 */
@Service
public class ViewerSelectionLogic {

	// View
	@Getter
	@Setter
	private ViewerSelectionView viewerSelectionView;

	// Service
	private final ViewerSelectionService viewerSelectionService;

	@Autowired
	public ViewerSelectionLogic(final ViewerSelectionService viewerSelectionService) {
		this.viewerSelectionService = viewerSelectionService;
	}

	/**
	 * Retrieve data from backend and transform into list
	 * @return List of entity to display
	 */
	public Page<ViewerSelectionEntity> retrieveViewerSelection(Pageable pageable) {
		return this.viewerSelectionService.retrieveViewerSelection(pageable);
	}

	/**
	 * Following a change in the grid: update the values in backend
	 * @param viewerSelectionEntity Values to update
	 * @return true if the selection has been updated
	 */
	public boolean updateViewerSelection(ViewerSelectionEntity viewerSelectionEntity) {
		return this.viewerSelectionService.update(viewerSelectionEntity);
	}


	/**
	 * Check for duplicate rule
	 * @param archive Archive name
	 * @param viewer Viewer type
	 * @param modalities List of modalities
	 * @param excludeId Id to exclude from the check (used during update)
	 * @return true if a duplicate rule exists
	 */
	public boolean checkDuplicateRule(String archive, ViewerType viewer,
									  List<ModalityType> modalities, Long excludeId) {
		return viewerSelectionService.checkDuplicate(archive, viewer, modalities, excludeId);
	}

	/**
	 * Count viewer selection
	 * @return count viewer selection
	 */
	public int countViewerSelection() {
		return this.viewerSelectionService.countViewerSelection();
	}

	/**
	 * Create a viewer selection in backend
	 * @param viewerSelectionEntity ViewerSelectionEntity to create
	 * @return true if viewer selection has been created
	 */
	public boolean addViewerSelection(@Valid ViewerSelectionEntity viewerSelectionEntity) {
		return this.viewerSelectionService.createViewerSelection(viewerSelectionEntity);
	}

	/**
	 * Delete a viewer selection in backend
	 * @param viewerSelectionEntity ViewerSelectionEntity to delete
	 */
	public void deleteViewerSelection(@Valid ViewerSelectionEntity viewerSelectionEntity) {
		this.viewerSelectionService.deleteViewerSelection(viewerSelectionEntity);
	}

	/**
	 * Update priority following a drag and drop action
	 * @param draggedItem dragged item
	 * @param value new priority value
	 */
	public void updatePriority(ViewerSelectionEntity draggedItem, int value) {
		this.viewerSelectionService.updatePriority(draggedItem, value);
	}

}
