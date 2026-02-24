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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import lombok.Getter;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageFormat;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionLogic;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Dialog modal for viewer selection rule definition
 */
public class ViewerSelectionDialog extends Dialog {

	@Getter
    private Button createButton;
	@Getter
	private Button cancelButton;

	private MultiSelectComboBox<ModalityType> modalityField;
	private Select<String> archiveField;
	private Select<ViewerType> viewerField;

    @Getter
    Binder<ViewerSelectionEntity> binder;

	private final ViewerSelectionLogic viewerSelectionLogic;

	/**
	 * Constructor
	 */
	public ViewerSelectionDialog(final ViewerSelectionLogic viewerSelectionLogic,
								 final ViewerSelectionView viewerSelectionView,
								 final ArrayList<String> archives) {
		this.viewerSelectionLogic = viewerSelectionLogic;

		// Dialog properties
		this.setWidth("25%");
		this.setHeight("auto");
		this.setCloseOnEsc(true);
		this.setCloseOnOutsideClick(true);
		this.setModal(true);

		// Build components
		buildComponents(viewerSelectionView, archives);

		// Add components in the view
		addComponentsView();
	}

	/**
	 * Build components
	 */
	private void buildComponents(ViewerSelectionView viewerSelectionView, ArrayList<String> archives) {
		// --- Inputs ---
		this.binder = new Binder<>(ViewerSelectionEntity.class);
		this.binder.setBean(new ViewerSelectionEntity());

		// Modality
		this.modalityField = new MultiSelectComboBox<>();
		this.modalityField.setLabel("Modality");
		this.modalityField.setPlaceholder("Select Modalities");
		this.modalityField.setItems(ModalityType.values());
		this.modalityField.setItemLabelGenerator(ModalityType::name);
		this.modalityField.setRequiredIndicatorVisible(true);

		// Archive
		this.archiveField = new Select<>();
		this.archiveField.setLabel("Archive");
		this.archiveField.setPlaceholder("Select Archive");
		this.archiveField.setItems(archives);
		this.archiveField.setEmptySelectionAllowed(false);
		this.archiveField.setRequiredIndicatorVisible(true);

		// Viewer
		this.viewerField = new Select<>();
		this.viewerField.setLabel("Viewer");
		this.viewerField.setItemLabelGenerator(ViewerType::getCode);
		this.viewerField.setPlaceholder("Select Viewer");
		this.viewerField.setItems(ViewerType.values());
		this.viewerField.setRequiredIndicatorVisible(true);

		// --- Binders ---
		this.binder.forField(modalityField)
				.withValidator(modalities -> modalities != null && !modalities.isEmpty(),
						"At least one modality must be selected")
				.bind(entity -> entity.getModalities() != null ? new HashSet<>(entity.getModalities()) : Set.of(),
						(entity, value) -> entity.setModalities(new ArrayList<>(value)));
		this.binder.forField(archiveField)
				.withValidator(Objects::nonNull, "Archive is mandatory")
				.bind(ViewerSelectionEntity::getArchive, ViewerSelectionEntity::setArchive);
		this.binder.forField(viewerField)
				.withValidator(Objects::nonNull, "Viewer is mandatory")
				.bind(ViewerSelectionEntity::getViewer, ViewerSelectionEntity::setViewer);

		// --- Buttons ---
		// Create button
		this.createButton = new Button("Create");

		// Listener on create button
		this.createButton.addClickListener(buttonClickEvent -> createButtonListener(viewerSelectionView));

		// Cancel button
		this.cancelButton = new Button("Cancel", event -> this.close());
	}

	/**
	 * Add components in the view
	 */
	private void addComponentsView() {
		VerticalLayout layout = new VerticalLayout();

		// Components Layout
		VerticalLayout inputLayout = new VerticalLayout();
		inputLayout.addAndExpand(modalityField, archiveField, viewerField);
		inputLayout.setSizeFull();
		inputLayout.setSpacing(false);
		inputLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

		// Layout Button
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.addAndExpand(this.createButton, cancelButton);
		buttonLayout.setSizeFull();
		buttonLayout.setSpacing(false);
		buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		layout.add(inputLayout, buttonLayout);
		layout.setSizeFull();

		// -- Add components ---
		this.add(layout);
	}

	/**
	 * Listener on create button
	 * @param viewerSelectionView ViewerSelectionView
	 */
	private void createButtonListener(ViewerSelectionView viewerSelectionView) {
		// Validate inputs
		BinderValidationStatus<ViewerSelectionEntity> validate = this.binder.validate();

		if (validate.isOk()) {
			// Retrieve ViewerSelectionEntity to create
			ViewerSelectionEntity viewerSelection = this.binder.getBean();

			// Increment priority
			viewerSelection.setPriority(this.viewerSelectionLogic.countViewerSelection());

			// Create viewerSelection
			if (!this.viewerSelectionLogic.addViewerSelection(viewerSelection)) {
				viewerSelectionView.displayMessage(
						new Message(MessageLevel.WARN, MessageFormat.TEXT, "This rule already exists !"),
						MessageType.NOTIFICATION_MESSAGE);
				return;
			}

			// Viewer selection has been created
			viewerSelectionView.displayMessage(
					new Message(MessageLevel.INFO, MessageFormat.TEXT, "Viewer selection rule has been created"),
					MessageType.NOTIFICATION_MESSAGE);
			viewerSelectionView.getViewerSelectionGrid().getOriginalDataProvider().refreshAll();
			this.close();
		}
	}

}
