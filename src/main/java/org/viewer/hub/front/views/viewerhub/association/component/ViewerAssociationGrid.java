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

package org.viewer.hub.front.views.viewerhub.association.component;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.ValueProvider;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.ViewerAssociationModel;
import org.viewer.hub.front.views.viewerhub.association.ViewerAssociationDataProvider;

/**
 * Grid for the association view
 */
public class ViewerAssociationGrid extends Grid<ViewerAssociationModel> {

	// Original data provider not filtered by the Paginated grid: currently the method
	// getDataProvider() of the PaginatedGrid returns a temporary filtered data provider:
	// https://github.com/Klaudeta/grid-pagination/issues/9
	// Used to refresh the grid
	private ViewerAssociationDataProvider<ViewerAssociationModel> originalDataProvider;

	/**
	 * Constructor
	 * @param viewerValueProvider Value provider for column
	 * BelongToMemberOf
	 */
	public ViewerAssociationGrid(ViewerAssociationDataProvider<ViewerAssociationModel> originalDataProvider,
								 ValueProvider<ViewerAssociationModel, Select<ViewerType>> viewerValueProvider) {

		this.originalDataProvider = originalDataProvider;

        // Set size for the grid
		this.setWidthFull();

		// Build columns
		// Archive
		this.addColumnArchive();
		// Viewer
		this.addColumnViewer(viewerValueProvider);
	}

	/**
	 * Add column archive
	 * @return column built
	 */
	private Column<ViewerAssociationModel> addColumnArchive() {
		return this.addColumn(ViewerAssociationModel::getArchive)
			.setHeader("Archive")
			.setWidth("22%")
			.setSortable(true)
			.setKey("archive");
	}

	/**
	 * Add column viewer
	 * @return column built
	 */
	private Column<ViewerAssociationModel> addColumnViewer(ValueProvider<ViewerAssociationModel, Select<ViewerType>> viewerValueProvider) {
		return this.addComponentColumn(viewerValueProvider)
			.setHeader("Viewer")
			.setWidth("60%")
			.setSortable(false); // If sortable, define a comparator
	}

	public ViewerAssociationDataProvider<ViewerAssociationModel> getOriginalDataProvider() {
		return this.originalDataProvider;
	}

}
