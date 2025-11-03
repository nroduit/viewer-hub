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
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.model.ViewerAssociationModel;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Dialog modal for user creation
 */
public class ViewerAssociationAddDialog extends Dialog {

	// Create button
	@Getter
    private Button createButton;

	// Binder
    @Getter
    Binder<ViewerAssociationModel> binder;

	private final ConnectorConfigurationProperties connectorConfigurationProperties;

	/**
	 * Constructor
	 */
	public ViewerAssociationAddDialog(final ConnectorConfigurationProperties connectorConfigurationProperties,
									  ArrayList<String> archives) {

		this.connectorConfigurationProperties = connectorConfigurationProperties;

		this.setWidth("25%");
		this.setHeight("25%");
		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		this.setModal(true);

		// --- Inputs ---
		this.binder = new Binder<>(ViewerAssociationModel.class);
		this.binder.setBean(new ViewerAssociationModel());

		// Archive name
		Select<String> archiveNameSelect = new Select<>();
		archiveNameSelect.setLabel("Archive");
		archiveNameSelect.setPlaceholder("Select Archive");
		archiveNameSelect.setItems(archives);
		this.binder.forField(archiveNameSelect)
				.withValidator(Objects::nonNull, "Archive is mandatory")
			.bind(ViewerAssociationModel::getArchive, ViewerAssociationModel::setArchive);

		// Viewer
		Select<Viewer> targetTypeSelect = new Select<>();
		targetTypeSelect.setLabel("Viewer");
		targetTypeSelect.setItemLabelGenerator(Viewer::getCode);
		targetTypeSelect.setPlaceholder("Select Viewer");
		targetTypeSelect.setItems(Viewer.values());
		this.binder.forField(targetTypeSelect)
			.withValidator(Objects::nonNull, "Viewer is mandatory")
			.bind(ViewerAssociationModel::getViewer, ViewerAssociationModel::setViewer);

		// Layout
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.addAndExpand(archiveNameSelect, targetTypeSelect);
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

}
