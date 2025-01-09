/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.front.views.association.component;

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
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;

import java.util.Objects;

/**
 * Dialog modal for user creation
 */
public class AssociationAddUserDialog extends Dialog {

	// Create button
	private Button createButton;

	// Binder
	Binder<TargetEntity> binder;

	/**
	 * Constructor
	 */
	public AssociationAddUserDialog() {

		this.setWidth("25%");
		this.setHeight("15%");
		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		this.setModal(true);

		// --- Inputs ---
		this.binder = new Binder<>(TargetEntity.class);
		this.binder.setBean(new TargetEntity());

		// Target name
		TextField targetNameField = new TextField();
		targetNameField.setLabel("Target Name");
		targetNameField.setPlaceholder("Enter Target Name");
		this.binder.forField(targetNameField)
			.withValidator(StringUtils::isNotBlank, "Target name is mandatory")
			.bind(TargetEntity::getName, TargetEntity::setName);

		// Target type
		ComboBox<TargetType> targetTypeComboBox = new ComboBox<>();
		targetTypeComboBox.setLabel("Target Type");
		targetTypeComboBox.setItemLabelGenerator(TargetType::getDescription);
		targetTypeComboBox.setPlaceholder("Select Target Type");
		targetTypeComboBox.setItems(TargetType.values());
		this.binder.forField(targetTypeComboBox)
			.withValidator(Objects::nonNull, "Target type is mandatory")
			.bind(TargetEntity::getType, TargetEntity::setType);

		// Layout
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.addAndExpand(targetNameField, targetTypeComboBox);
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

	public void setCreateButton(Button createButton) {
		this.createButton = createButton;
	}

	public Binder<TargetEntity> getBinder() {
		return this.binder;
	}

	public void setBinder(Binder<TargetEntity> binder) {
		this.binder = binder;
	}

}
