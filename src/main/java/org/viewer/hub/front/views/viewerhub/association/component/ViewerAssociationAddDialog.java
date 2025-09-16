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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.apache.commons.lang3.StringUtils;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.ViewerAssociationModel;

import java.util.Objects;

/**
 * Dialog modal for user creation
 */
public class ViewerAssociationAddDialog extends Dialog {

	// Create button
	private Button createButton;

	// Binder
	Binder<ViewerAssociationModel> binder;

	/**
	 * Constructor
	 */
	public ViewerAssociationAddDialog() {

		this.setWidth("25%");
		this.setHeight("25%");
		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		this.setModal(true);

		// --- Inputs ---
		this.binder = new Binder<>(ViewerAssociationModel.class);
		this.binder.setBean(new ViewerAssociationModel());

		// Archive name
		TextField archiveNameField = new TextField();
		archiveNameField.setLabel("Archive Name");
		archiveNameField.setPlaceholder("Enter Archive Name");
		this.binder.forField(archiveNameField)
			.withValidator(StringUtils::isNotBlank, "Archive name is mandatory")
			.bind(ViewerAssociationModel::getArchive, ViewerAssociationModel::setArchive);

		// Viewer
		ComboBox<ViewerType> targetTypeComboBox = new ComboBox<>();
		targetTypeComboBox.setLabel("Viewer");
		targetTypeComboBox.setItemLabelGenerator(Enum::name);
		targetTypeComboBox.setPlaceholder("Select Viewer");
		targetTypeComboBox.setItems(ViewerType.values());
		this.binder.forField(targetTypeComboBox)
			.withValidator(Objects::nonNull, "Viewer is mandatory")
			.bind(ViewerAssociationModel::getViewer, ViewerAssociationModel::setViewer);

		// Layout
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.addAndExpand(archiveNameField, targetTypeComboBox);
		inputLayout.setWidthFull();
		inputLayout.setSpacing(true);
		inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		// --- Buttons ---
		// Create button
		this.createButton = new Button("Create");

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

	public Button getCreateButton() {
		return this.createButton;
	}

	public Binder<ViewerAssociationModel> getBinder() {
		return this.binder;
	}

}
