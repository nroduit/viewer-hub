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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.*;
import lombok.Getter;
import org.viewer.hub.back.model.*;
import org.viewer.hub.front.views.viewerhub.association.ViewerAssociationLogic;
import org.viewer.hub.front.views.viewerhub.association.ViewerAssociationView;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.ViewerAssociationModel;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Dialog modal for user creation
 */
public class ViewerAssociationDialog extends Dialog {

	@Getter
    private Button createButton;

	// Binder
    @Getter
    Binder<ViewerAssociationModel> binder;

	private final TextField modalityField;
	private final Select<String> archiveNameSelect;

	private final ViewerAssociationLogic viewerAssociationLogic;

	/**
	 * Constructor
	 */
	public ViewerAssociationDialog(final ViewerAssociationLogic viewerAssociationLogic,
								   final ViewerAssociationView viewerAssociationView,
								   ArrayList<String> archives, ViewerAssociationModel selectedModel) {

		this.viewerAssociationLogic = viewerAssociationLogic;

		this.setWidth("50%");
		this.setHeight("30%");
		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		this.setModal(true);

		// --- Inputs ---
		this.binder = new Binder<>(ViewerAssociationModel.class);
		this.binder.setBean(new ViewerAssociationModel());

		// Modality
		this.modalityField = new TextField();
		modalityField.setLabel("Modality");
		modalityField.setPlaceholder("Modality1,Modality2,...");
		modalityField.setAllowedCharPattern("[\\w,]");
		modalityField.setPattern("^\\w+(,\\w+)*$");

		// Archive
		this.archiveNameSelect = new Select<>();
		archiveNameSelect.setLabel("Archive");
		archiveNameSelect.setPlaceholder("Select Archive");
		archiveNameSelect.setItems(archives);
		archiveNameSelect.setEmptySelectionAllowed(true);

		// Viewer
		Select<ViewerType> viewerNameSelect = new Select<>();
		viewerNameSelect.setLabel("Viewer");
		viewerNameSelect.setItemLabelGenerator(ViewerType::getCode);
		viewerNameSelect.setPlaceholder("Select Viewer");
		viewerNameSelect.setItems(ViewerType.values());
		viewerNameSelect.setRequiredIndicatorVisible(true);

		this.binder.forField(modalityField)
				.withValidator(getAtLeatOneFieldValidator())
				.bind(ViewerAssociationModel::getModality, ViewerAssociationModel::setModality);
		this.binder.forField(archiveNameSelect)
				.withValidator(getAtLeatOneFieldValidator())
				.bind(ViewerAssociationModel::getArchive, ViewerAssociationModel::setArchive);
		this.binder.forField(viewerNameSelect)
				.withValidator(Objects::nonNull, "Viewer is mandatory")
				.bind(ViewerAssociationModel::getViewer, ViewerAssociationModel::setViewer);

		if (selectedModel != null) {
			modalityField.setValue(selectedModel.getModality() == null ? "" : selectedModel.getModality());
			archiveNameSelect.setValue(selectedModel.getArchive());
			viewerNameSelect.setValue(selectedModel.getViewer());
		}

		// Layout
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.addAndExpand(modalityField, archiveNameSelect, viewerNameSelect);
		inputLayout.setWidthFull();
		inputLayout.setSpacing(true);
		inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		// --- Buttons ---
		// Create button
		if (selectedModel == null) {
			this.createButton = new Button("Create");
		}
		else {
			this.createButton = new Button("Edit");
		}

		// Listener on create button
		this.createButton.addClickListener(buttonClickEvent -> {
			// Validate inputs
			BinderValidationStatus<ViewerAssociationModel> validate = this.binder.validate();

			if (validate.isOk()) {
				// Retrieve target to create
				ViewerAssociationModel targetToCreate = this.binder.getBean();

				// Increment priority
				if (targetToCreate.getPriority() == null) {
					targetToCreate.setPriority(this.viewerAssociationLogic.countAssociationModels());
				}

				// Trim modality values
				if (targetToCreate.getModality() != null) {
					targetToCreate.setModality(targetToCreate.getModality().trim());
				}
				// Set modality null if empty
				if (targetToCreate.getModality() != null && targetToCreate.getModality().isEmpty()) {
					targetToCreate.setModality(null);
				}

				boolean saved;
				if (selectedModel == null) {
					// Create target
					saved = this.viewerAssociationLogic.addViewerAssociationModel(targetToCreate);
				}
				else {
					// Update target
					selectedModel.setModality(targetToCreate.getModality());
					selectedModel.setArchive(targetToCreate.getArchive());
					selectedModel.setViewer(targetToCreate.getViewer());
					saved = this.viewerAssociationLogic.updateViewerAssociationModel(selectedModel);
				}

				if (!saved) {
					viewerAssociationView.displayMessage(
							new Message(MessageLevel.WARN, MessageFormat.TEXT, "This rule already exists !"),
							MessageType.NOTIFICATION_MESSAGE);
					return;
				}

				// Archive association has been created
				viewerAssociationView.displayMessage(
						new Message(MessageLevel.INFO, MessageFormat.TEXT, "Viewer association rule has been associated"),
						MessageType.NOTIFICATION_MESSAGE);
				viewerAssociationView.getViewerAssociationGrid().getOriginalDataProvider().refreshAll();
				this.close();
			}
		});

		// Cancel button
		Button cancelButton = new Button("Cancel", event -> this.close());

		// Cancel action on ESC press
		Shortcuts.addShortcutListener(this, event -> this.close(), Key.ESCAPE);

		// Layout Button
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.addAndExpand(this.createButton, cancelButton);
		buttonLayout.setWidthFull();
		buttonLayout.setSpacing(true);
		buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		// -- Add components ---
		this.add(inputLayout, buttonLayout);
	}

	private Validator<? super Object> getAtLeatOneFieldValidator() {
		return (value, valueContext) -> {
			if (modalityField.getValue().isEmpty() && archiveNameSelect.getValue() == null) {
				return ValidationResult.error("You must specify one of these fields");
			}
			return ValidationResult.ok();
		};
	}

}
