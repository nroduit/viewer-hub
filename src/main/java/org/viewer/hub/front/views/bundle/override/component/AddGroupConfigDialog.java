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

package org.viewer.hub.front.views.bundle.override.component;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;

import java.io.Serial;
import java.util.Objects;

/**
 * Dialog modal use to create an override config for a group with existing package version
 * and launch config
 */
@Getter
@Setter
public class AddGroupConfigDialog extends Dialog {

	@Serial
	private static final long serialVersionUID = 6586496556297408055L;

	// Create button
	private Button createButton;

	// Binder
	Binder<OverrideConfigEntity> binder;

	private ComboBox<PackageVersionEntity> packageVersionComboBox;

	private ComboBox<LaunchConfigEntity> launchConfigComboBox;

	private GroupComboBox groupComboBox;

	/**
	 * Constructor
	 */
	public AddGroupConfigDialog() {

		this.setWidth("30%");
		this.setHeight("15%");
		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		this.setModal(true);

		// --- Inputs ---
		this.binder = new Binder<>(OverrideConfigEntity.class);
		this.binder.setBean(new OverrideConfigEntity());

		// Package version
		this.packageVersionComboBox = new ComboBox<>();
		this.packageVersionComboBox.setLabel("Package version");
		this.packageVersionComboBox
			.setItemLabelGenerator((packageVersionEntity) -> (packageVersionEntity.getQualifier() == null)
					? packageVersionEntity.getVersionNumber()
					: packageVersionEntity.getVersionNumber() + packageVersionEntity.getQualifier());
		this.packageVersionComboBox.setPlaceholder("Select package version");
		this.binder.forField(this.packageVersionComboBox)
			.withValidator(Objects::nonNull, "Package version is mandatory")
			.bind(OverrideConfigEntity::getPackageVersion, OverrideConfigEntity::setPackageVersion);

		// Launch config
		this.launchConfigComboBox = new ComboBox<>();
		this.launchConfigComboBox.setLabel("Launch config");
		this.launchConfigComboBox.setItemLabelGenerator(LaunchConfigEntity::getName);
		this.launchConfigComboBox.setPlaceholder("Select launch config");
		this.binder.forField(this.launchConfigComboBox)
			.withValidator(Objects::nonNull, "Launch config is mandatory")
			.bind(OverrideConfigEntity::getLaunchConfig, OverrideConfigEntity::setLaunchConfig);

		// Group
		this.groupComboBox = new GroupComboBox();
		this.groupComboBox.setLabel("Group");
		this.groupComboBox.setPlaceHolder("Select group");
		this.binder.forField(this.groupComboBox.getComboBox())
			.withValidator(Objects::nonNull, "Group is mandatory")
			.bind(OverrideConfigEntity::getTarget, OverrideConfigEntity::setTarget);

		// Layout
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.addAndExpand(this.packageVersionComboBox, this.launchConfigComboBox, this.groupComboBox);
		inputLayout.setWidthFull();
		inputLayout.setSpacing(true);
		inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		// --- Buttons ---
		// Create button
		this.createButton = new Button("Create");

		// Cancel button
		Button cancelButton = new Button("Cancel", (event) -> this.close());

		// Cancel action on ESC press
		Shortcuts.addShortcutListener(this, (event) -> this.close(), Key.ESCAPE);

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
