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
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerSelectionType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageFormat;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionDataProvider;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionLogic;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionView;

import java.util.*;
import java.util.function.Consumer;

/**
 * Grid for the viewer selection view
 */
public class ViewerSelectionGrid extends Grid<ViewerSelectionEntity> {

	private final ViewerSelectionView viewerSelectionView;

	@Getter
	private final ViewerSelectionDataProvider<ViewerSelectionEntity> originalDataProvider;

	private final ViewerSelectionLogic viewerSelectionLogic;

	private ViewerSelectionEntity draggedItem;

	/**
	 * Constructor
	 */
	public ViewerSelectionGrid(final ViewerSelectionView viewerSelectionView,
			final ViewerSelectionDataProvider<ViewerSelectionEntity> originalDataProvider,
			ValueProvider<ViewerSelectionEntity, Select<ViewerType>> viewerValueProvider,
			ValueProvider<ViewerSelectionEntity, Select<String>> archiveValueProvider,
			final ViewerSelectionLogic viewerSelectionLogic) {
		this.viewerSelectionView = viewerSelectionView;
		this.originalDataProvider = originalDataProvider;
		this.viewerSelectionLogic = viewerSelectionLogic;

		// Set size for the grid
		this.setWidthFull();
		// Permit Drag & Drop
		createDragListener();
		// Build columns
		// Drag icon
		this.addDragIcon();
		// Modality column
		this.addModalityColumn();
		// Archive
		this.addArchiveColumn(archiveValueProvider);
		// Viewer
		this.addViewerColumn(viewerValueProvider);
		// Edit and Delete buttons
		this.addButtons();
	}

	/**
	 * Add drag listener
	 */
	private void createDragListener() {
		this.setRowsDraggable(true);
		this.setDropMode(GridDropMode.BETWEEN);

		this.setDragFilter(e -> !ViewerSelectionType.DEFAULT.name().equals(e.getArchive()));
		this.setDropFilter(e -> !ViewerSelectionType.DEFAULT.name().equals(e.getArchive()));

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
			if (Objects.equals(model.getArchive(), ViewerSelectionType.DEFAULT.name())) {
				return null;
			}
			Icon dragIcon = VaadinIcon.GRID_SMALL.create();
			dragIcon.setTooltipText("Drag & drop to update rule priority");
			dragIcon.getStyle().set("padding", "0.30em");
			return dragIcon;
		});
	}

	/**
	 * Add modality column
	 * @return column modality
	 */
	private Column<ViewerSelectionEntity> addModalityColumn() {
		return this.addComponentColumn(entity -> {
			if (Objects.equals(entity.getArchive(), ViewerSelectionType.DEFAULT.name())) {
				return null;
			}
			else {
				MultiSelectComboBox<ModalityType> modalityComboBox = new MultiSelectComboBox<>();
				modalityComboBox.setItems(ModalityType.values());
				modalityComboBox.setItemLabelGenerator(ModalityType::name);
				modalityComboBox
					.setValue(entity.getModalities() != null ? new HashSet<>(entity.getModalities()) : Set.of());
				modalityComboBox.setWidthFull();

				modalityComboBox.addValueChangeListener(event -> {
					Set<ModalityType> newModalities = event.getValue();
					ArrayList<ModalityType> modalities = new ArrayList<>(newModalities);
					Set<ModalityType> oldModalities = entity.getModalities() != null
							? new HashSet<>(entity.getModalities()) : Set.of();

					if (!validateAndUpdateEntity(entity, entity.getArchive(), entity.getViewer(), modalities,
							e -> e.setModalities(modalities))) {
						modalityComboBox.setValue(oldModalities);
					}
				});
				return modalityComboBox;
			}
		}).setHeader("Modality").setWidth("45%").setSortable(false).setKey("modalities");
	}

	/**
	 * Add column archive
	 * @return column archive
	 */
	private Column<ViewerSelectionEntity> addArchiveColumn(
			ValueProvider<ViewerSelectionEntity, Select<String>> archiveValueProvider) {
		return this.addComponentColumn(entity -> {
			Select<String> archiveSelect = archiveValueProvider.apply(entity);

			archiveSelect.addValueChangeListener(event -> {
				String newArchive = event.getValue();
				String oldArchive = entity.getArchive();
				List<ModalityType> modalities = entity.getModalities() != null ? entity.getModalities()
						: new ArrayList<>();
				if (!validateAndUpdateEntity(entity, newArchive, entity.getViewer(), modalities,
						e -> e.setArchive(newArchive))) {
					archiveSelect.setValue(oldArchive);
					originalDataProvider.refreshAll();
				}
			});
			return archiveSelect;
		}).setHeader("Archive").setWidth("20%").setSortable(false).setKey("archive");
	}

	/**
	 * Add column viewer
	 * @return column viewer
	 */
	private Column<ViewerSelectionEntity> addViewerColumn(
			ValueProvider<ViewerSelectionEntity, Select<ViewerType>> viewerValueProvider) {
		return this.addComponentColumn(entity -> {
			Select<ViewerType> viewerSelect = viewerValueProvider.apply(entity);

			viewerSelect.addValueChangeListener(event -> {
				ViewerType newViewer = event.getValue();
				ViewerType oldViewer = entity.getViewer();
				List<ModalityType> modalities = entity.getModalities() != null ? entity.getModalities()
						: new ArrayList<>();

				if (!validateAndUpdateEntity(entity, entity.getArchive(), newViewer, modalities,
						e -> e.setViewer(newViewer))) {
					viewerSelect.setValue(oldViewer);
					originalDataProvider.refreshAll();
				}
			});
			return viewerSelect;
		}).setHeader("Viewer").setWidth("20%").setSortable(false);
	}

	/**
	 * Add delete button
	 * @return column delete button
	 */
	private Column<ViewerSelectionEntity> addButtons() {
		return this.addComponentColumn(model -> {
			if (Objects.equals(model.getArchive(), ViewerSelectionType.DEFAULT.name())) {
				return null;
			}

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
			layoutWithoutSpacing.setPadding(true);
			layoutWithoutSpacing.add(deleteButton);
			layoutWithoutSpacing.setWidth("10%");

			return layoutWithoutSpacing;
		});
	}

	/**
	 * Validate and update entity field, checking for duplicate rules
	 * @param entity Entity to update
	 * @param archive Archive value
	 * @param viewer Viewer value
	 * @param modalities Modalities list
	 * @param updateAction Action to perform if validation passes
	 * @return true if update was successful, false otherwise
	 */
	private boolean validateAndUpdateEntity(ViewerSelectionEntity entity, String archive, ViewerType viewer,
			List<ModalityType> modalities, Consumer<ViewerSelectionEntity> updateAction) {
		boolean exists = viewerSelectionLogic.checkDuplicateRule(archive, viewer, modalities, entity.getId());

		if (exists) {
			viewerSelectionView.displayMessage(
					new Message(MessageLevel.ERROR, MessageFormat.TEXT,
							"A viewer selection rule with the same archive, viewer and modalities already exists!"),
					MessageType.NOTIFICATION_MESSAGE);
			return false;
		}
		if (modalities == null || modalities.isEmpty()) {
			viewerSelectionView.displayMessage(
					new Message(MessageLevel.ERROR, MessageFormat.TEXT, "At least one modality must be selected!"),
					MessageType.NOTIFICATION_MESSAGE);
			return false;
		}
		updateAction.accept(entity);
		viewerSelectionLogic.updateViewerSelection(entity);
		return true;
	}

}
