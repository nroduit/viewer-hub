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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.ValueProvider;
import lombok.Getter;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.ViewerAssociationModel;
import org.viewer.hub.front.views.viewerhub.association.ViewerAssociationDataProvider;
import org.viewer.hub.front.views.viewerhub.association.ViewerAssociationLogic;

import java.util.Objects;

/**
 * Grid for the association view
 */
public class ViewerAssociationGrid extends Grid<ViewerAssociationModel> {

	// Original data provider not filtered by the Paginated grid: currently the method
	// getDataProvider() of the PaginatedGrid returns a temporary filtered data provider:
	// https://github.com/Klaudeta/grid-pagination/issues/9
	// Used to refresh the grid
	@Getter
    private ViewerAssociationDataProvider<ViewerAssociationModel> originalDataProvider;

	private final ViewerAssociationLogic viewerAssociationLogic;

	private ViewerAssociationModel draggedItem = null;

	/**
	 * Constructor
	 */
	public ViewerAssociationGrid(ViewerAssociationDataProvider<ViewerAssociationModel> originalDataProvider,
								 ValueProvider<ViewerAssociationModel, Select<ViewerType>> viewerValueProvider,
								 ViewerAssociationLogic viewerAssociationLogic) {

		this.originalDataProvider = originalDataProvider;
		this.viewerAssociationLogic = viewerAssociationLogic;

        // Set size for the grid
		this.setWidthFull();

		addDragListener();


		// Build columns
		// Drag icon
		this.addDragIcon();
		// Archive
		this.addColumnArchive();
		// Viewer
		this.addColumnViewer(viewerValueProvider);
		// Delete button
		this.addDeleteButton();
	}

	private void addDragListener() {
		this.setRowsDraggable(true);
		this.setDropMode(GridDropMode.BETWEEN);

		this.setDragFilter(e -> !e.getArchive().equals("DEFAULT"));
		this.setDropFilter(e -> !e.getArchive().equals("DEFAULT"));

		this.addDragStartListener(e -> {
			draggedItem = e.getDraggedItems().get(0);
		});
		this.addDragEndListener(e -> {
			draggedItem = null;
		});

		this.addDropListener(e -> {
			ViewerAssociationModel target = e.getDropTargetItem().orElse(null);
			if (target == null || draggedItem.equals(target)) {
				return;
			}
			int targetPriority = target.getPriority();
			if (e.getDropLocation() == GridDropLocation.ABOVE) {
				targetPriority++;
			}
			if (targetPriority == draggedItem.getPriority()) {
				return;
			}
			viewerAssociationLogic.updatePriority(draggedItem, targetPriority);
			originalDataProvider.refreshAll();
		});
	}

	/**
	 * Add drag icon
	 * @return column drag icon
	 */
	private Column<ViewerAssociationModel> addDragIcon() {
		return this.addComponentColumn(model -> {
			if (Objects.equals(model.getArchive(), "DEFAULT")) {
				return null;
			}
			Icon dragIcon = VaadinIcon.GRID_SMALL.create();
			dragIcon.setTooltipText("Drag & drop to update rule priority");
			dragIcon.getStyle().set("padding", "0.25em");
			return dragIcon;
		});
	}

	/**
	 * Add column archive
	 * @return column archive
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
	 * @return column viewer
	 */
	private Column<ViewerAssociationModel> addColumnViewer(ValueProvider<ViewerAssociationModel, Select<ViewerType>> viewerValueProvider) {
		return this.addComponentColumn(viewerValueProvider)
			.setHeader("Viewer")
			.setWidth("60%")
			.setSortable(false); // If sortable, define a comparator
	}

	/**
	 * Add delete button
	 * @return column delete button
	 */
	private Column<ViewerAssociationModel> addDeleteButton() {
		return this.addComponentColumn(model -> {
			if (Objects.equals(model.getArchive(), "DEFAULT")) {
				return null;
			}
			Button deleteButton = new Button();
			deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY,
					ButtonVariant.LUMO_ERROR);
			deleteButton.setIcon(new Icon(VaadinIcon.TRASH));
			deleteButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
                viewerAssociationLogic.deleteViewerAssociationModel(model);
                originalDataProvider.refreshAll();
            });
			deleteButton.setTooltipText("Delete configuration");
            return deleteButton;
        });
	}

}
