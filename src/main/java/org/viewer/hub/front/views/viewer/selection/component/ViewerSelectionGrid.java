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

package org.viewer.hub.front.views.viewer.selection.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.ValueProvider;
import lombok.Getter;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionDataProvider;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionLogic;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Grid for the viewer selection view
 */
public class ViewerSelectionGrid extends Grid<ViewerSelectionEntity> {

	public static final String DEFAULT = "DEFAULT";

	private final ViewerSelectionView viewerSelectionView;

	@Getter
	private final ViewerSelectionDataProvider<ViewerSelectionEntity> originalDataProvider;

	private final ViewerSelectionLogic viewerSelectionLogic;

	private final ArrayList<String> archives;

	private ViewerSelectionEntity draggedItem;

	/**
	 * Constructor
	 */
	public ViewerSelectionGrid(final ViewerSelectionView viewerSelectionView,
							   final ViewerSelectionDataProvider<ViewerSelectionEntity> originalDataProvider,
							   ValueProvider<ViewerSelectionEntity, Select<ViewerType>> viewerValueProvider,
							   final ViewerSelectionLogic viewerSelectionLogic,
							   final ArrayList<String> archives) {
		this.viewerSelectionView = viewerSelectionView;
		this.originalDataProvider = originalDataProvider;
		this.viewerSelectionLogic = viewerSelectionLogic;
		this.archives = archives;

		// Set size for the grid
		this.setWidthFull();

		// Permit Drag & Drop
		createDragListener();

		// Build columns
		// Drag icon
		this.addDragIcon();
		// Modality column
		this.addColumnModality();
		// Archive
		this.addColumnArchive();
		// Viewer
		this.addColumnViewer(viewerValueProvider);

		// Edit and Delete buttons
		this.addButtons();
	}

	/**
	 * Add drag listener
	 */
	// TODO à tester + refactor ?
	private void createDragListener() {
		this.setRowsDraggable(true);
		this.setDropMode(GridDropMode.BETWEEN);

		this.setDragFilter(e -> !DEFAULT.equals(e.getArchive()));
		this.setDropFilter(e -> !DEFAULT.equals(e.getArchive()));

		this.addDragStartListener(e -> draggedItem = e.getDraggedItems().getFirst());
		this.addDragEndListener(e -> draggedItem = null);

		this.addDropListener(e -> {
			ViewerSelectionEntity viewerSelectionEntity = e.getDropTargetItem().orElse(null);
			if (viewerSelectionEntity == null || Objects.equals(draggedItem, viewerSelectionEntity)) {
				return;
			}
			int targetPriority = viewerSelectionEntity.getPriority();
			if (e.getDropLocation() == GridDropLocation.ABOVE) {
				targetPriority++;
			}
			if (targetPriority == draggedItem.getPriority()) {
				return;
			}
			viewerSelectionLogic.updatePriority(draggedItem, targetPriority);
			originalDataProvider.refreshAll();
		});
	}

	/**
	 * Add drag icon
	 * @return column drag icon
	 */
	private Column<ViewerSelectionEntity> addDragIcon() {
		return this.addComponentColumn(model -> {
			if (Objects.equals(model.getArchive(), DEFAULT)) {
				return null;
			}
			Icon dragIcon = VaadinIcon.GRID_SMALL.create();
			dragIcon.setTooltipText("Drag & drop to update rule priority");
			dragIcon.getStyle().set("padding", "0.30em");
			return dragIcon;
		});
	}

	/**
	 * Add column archive
	 * @return column archive
	 */
	private Column<ViewerSelectionEntity> addColumnModality() {
		return this.addColumn(ViewerSelectionEntity::getModality)
				.setHeader("Modality")
				.setWidth("22%")
				.setSortable(true)
				.setKey("modality");
	}

	/**
	 * Add column archive
	 * @return column archive
	 */
	private Column<ViewerSelectionEntity> addColumnArchive() {
		return this.addColumn(ViewerSelectionEntity::getArchive)
			.setHeader("Archive")
			.setWidth("22%")
			.setSortable(true)
			.setKey("archive");
	}

	/**
	 * Add column viewer
	 * @return column viewer
	 */
	private Column<ViewerSelectionEntity> addColumnViewer(ValueProvider<ViewerSelectionEntity, Select<ViewerType>> viewerValueProvider) {
		return this.addComponentColumn(viewerValueProvider)
			.setHeader("Viewer")
			.setWidth("30%")
			.setSortable(false); // If sortable, define a comparator
	}

	/**
	 * Add delete button
	 * @return column delete button
	 */
	private Column<ViewerSelectionEntity> addButtons() {
		return this.addComponentColumn(model -> {
			if (Objects.equals(model.getArchive(), DEFAULT)) {
				return null;
			}

			Button editButton = new Button();
			editButton.addThemeVariants(ButtonVariant.LUMO_ICON);
			editButton.setIcon(new Icon(VaadinIcon.EDIT));
			editButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
				// Create and open dialog
				ViewerSelectionDialog viewerSelectionDialog = new ViewerSelectionDialog(viewerSelectionLogic,
						viewerSelectionView, archives, model);
				viewerSelectionDialog.open();
			});
			editButton.setTooltipText("Edit configuration");

			Button deleteButton = new Button();
			deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY,
					ButtonVariant.LUMO_ERROR);
			deleteButton.setIcon(new Icon(VaadinIcon.TRASH));
			deleteButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
				viewerSelectionLogic.deleteViewerSelection(model);
				originalDataProvider.refreshAll();
			});
			deleteButton.setTooltipText("Delete configuration");

			HorizontalLayout layoutWithoutSpacing = new HorizontalLayout();
			layoutWithoutSpacing.add(editButton);
			layoutWithoutSpacing.setPadding(true);
			layoutWithoutSpacing.add(deleteButton);
			layoutWithoutSpacing.setWidth("10%");

			return layoutWithoutSpacing;
		});
	}

}
