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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import lombok.Getter;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageFormat;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionLogic;
import org.viewer.hub.front.views.viewer.selection.ViewerSelectionView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Dialog modal for viewer selection rule  definition
 */
public class ViewerSelectionDialog extends Dialog {

	@Getter
    private Button createButton;
	@Getter
	private Button cancelButton;

	private TextField modalityField;
	private Select<String> archiveNameSelect;
	private Select<ViewerType> viewerNameSelect;

    @Getter
    Binder<ViewerSelectionEntity> binder;

	private final ViewerSelectionLogic viewerSelectionLogic;

	/**
	 * Constructor
	 */
	public ViewerSelectionDialog(final ViewerSelectionLogic viewerSelectionLogic,
								 final ViewerSelectionView viewerSelectionView,
								 ArrayList<String> archives, ViewerSelectionEntity viewerSelectionEntity) {
		this.viewerSelectionLogic = viewerSelectionLogic;

		this.setWidth("50%");
		this.setHeight("30%");
		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		this.setModal(true);

		// Build components
		buildComponents(viewerSelectionView, archives, viewerSelectionEntity);

		// Add components in the view
		addComponentsView();
	}

	private void buildComponents(ViewerSelectionView viewerSelectionView, ArrayList<String> archives, ViewerSelectionEntity viewerSelectionEntity) {
		// --- Inputs ---
		this.binder = new Binder<>(ViewerSelectionEntity.class);
		this.binder.setBean(new ViewerSelectionEntity());

		// Modality
		this.modalityField = new TextField();
		this.modalityField.setLabel("Modality");
		this.modalityField.setPlaceholder("Modality1,Modality2,...");
		this.modalityField.setAllowedCharPattern("[\\w,]");
		this.modalityField.setPattern("^\\w+(,\\w+)*$");

		// Archive
		this.archiveNameSelect = new Select<>();
		this.archiveNameSelect.setLabel("Archive");
		this.archiveNameSelect.setPlaceholder("Select Archive");
		this.archiveNameSelect.setItems(archives);
		this.archiveNameSelect.setEmptySelectionAllowed(true);

		// Viewer
		this.viewerNameSelect = new Select<>();
		this.viewerNameSelect.setLabel("Viewer");
		this.viewerNameSelect.setItemLabelGenerator(ViewerType::getCode);
		this.viewerNameSelect.setPlaceholder("Select Viewer");
		this.viewerNameSelect.setItems(ViewerType.values());
		this.viewerNameSelect.setRequiredIndicatorVisible(true);

		this.binder.forField(modalityField)
				.withValidator(atLeatOneFieldValidator())
				.bind(ViewerSelectionEntity::getModality, ViewerSelectionEntity::setModality);
		this.binder.forField(archiveNameSelect)
				.withValidator(atLeatOneFieldValidator())
				.bind(ViewerSelectionEntity::getArchive, ViewerSelectionEntity::setArchive);
		this.binder.forField(viewerNameSelect)
				.withValidator(Objects::nonNull, "Viewer is mandatory")
				.bind(ViewerSelectionEntity::getViewer, ViewerSelectionEntity::setViewer);

		if (viewerSelectionEntity != null) {
			modalityField.setValue(viewerSelectionEntity.getModality() == null ? "" : viewerSelectionEntity.getModality());
			archiveNameSelect.setValue(viewerSelectionEntity.getArchive());
			viewerNameSelect.setValue(viewerSelectionEntity.getViewer());
		}

		// --- Buttons ---
		// Create button
		if (viewerSelectionEntity == null) {
			this.createButton = new Button("Create");
		}
		else {
			this.createButton = new Button("Edit");
		}

		// Listener on create button
		this.createButton.addClickListener(buttonClickEvent -> {
			createButtonListener(viewerSelectionView, viewerSelectionEntity);
		});

		// Cancel button
		this.cancelButton = new Button("Cancel", event -> this.close());

		// Cancel action on ESC press
		Shortcuts.addShortcutListener(this, event -> this.close(), Key.ESCAPE);
	}

	/**
	 * Add components in the view
	 */
	private void addComponentsView() {
		// Components Layout
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.addAndExpand(modalityField, archiveNameSelect, viewerNameSelect);
		inputLayout.setWidthFull();
		inputLayout.setSpacing(true);
		inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		// Layout Button
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.addAndExpand(this.createButton, cancelButton);
		buttonLayout.setWidthFull();
		buttonLayout.setSpacing(true);
		buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		// -- Add components ---
		this.add(inputLayout, buttonLayout);
	}

	/**
	 * Listener on create button
	 * @param viewerSelectionView ViewerSelectionView
	 * @param viewerSelectionEntity ViewerSelectionEntity
	 */
	private void createButtonListener(ViewerSelectionView viewerSelectionView, ViewerSelectionEntity viewerSelectionEntity) {
		// Validate inputs
		BinderValidationStatus<ViewerSelectionEntity> validate = this.binder.validate();

		if (validate.isOk()) {
			// Retrieve ViewerSelectionEntity to create
			ViewerSelectionEntity viewerSelection = this.binder.getBean();

			// Increment priority
			if (viewerSelection.getPriority() == null) {
				viewerSelection.setPriority(this.viewerSelectionLogic.countViewerSelection());
			}

			// Trim modality values
			if (viewerSelection.getModality() != null) {
				viewerSelection.setModality(viewerSelection.getModality().trim());
			}
			// Set modality null if empty
			if (viewerSelection.getModality() != null && viewerSelection.getModality().isEmpty()) {
				viewerSelection.setModality(null);
			}

			boolean saved;
			if (viewerSelectionEntity == null) {
				// Create viewerSelection
				saved = this.viewerSelectionLogic.addViewerSelection(viewerSelection);
			}
			else {
				// Update viewerSelectionEntity
				viewerSelectionEntity.setModality(viewerSelection.getModality());
				viewerSelectionEntity.setArchive(viewerSelection.getArchive());
				viewerSelectionEntity.setViewer(viewerSelection.getViewer());
				saved = this.viewerSelectionLogic.updateViewerSelection(viewerSelectionEntity);
			}

			if (!saved) {
				viewerSelectionView.displayMessage(
						new Message(MessageLevel.WARN, MessageFormat.TEXT, "This rule already exists !"),
						MessageType.NOTIFICATION_MESSAGE);
				return;
			}

			// Viewer selection has been created
			viewerSelectionView.displayMessage(
					new Message(MessageLevel.INFO, MessageFormat.TEXT, "Viewer selection rule has been updated"),
					MessageType.NOTIFICATION_MESSAGE);
			viewerSelectionView.getViewerSelectionGrid().getOriginalDataProvider().refreshAll();
			this.close();
		}
	}

	/**
	 * Validator to check that at least one field is filled
	 * @return Validator created
	 */
	private Validator<? super Object> atLeatOneFieldValidator() {
		return (value, valueContext) -> {
			if (modalityField.getValue().isEmpty() && archiveNameSelect.getValue() == null) {
				return ValidationResult.error("You must specify one of these fields");
			}
			return ValidationResult.ok();
		};
	}

}
