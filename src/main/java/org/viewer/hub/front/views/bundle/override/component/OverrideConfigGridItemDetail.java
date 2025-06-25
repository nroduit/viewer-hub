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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.WeasisPropertyEntity;
import org.viewer.hub.back.enums.WeasisPropertyCategory;
import org.viewer.hub.front.components.UIUtil;
import org.viewer.hub.front.views.bundle.override.OverrideView;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class OverrideConfigGridItemDetail extends FormLayout {

	@Serial
	private static final long serialVersionUID = 735929040539022724L;

	// ============== Binder ==================
	private Binder<OverrideConfigEntity> binder;

	// ============== Tabs ==================
	private TabSheet tabSheet;

	// ============== Textfields ==================
	@Getter
	private Map<WeasisPropertyCategory, List<TextField>> allTextFieldsByCategory;

	// ============== View ==================
	private OverrideView overrideView;

	/**
	 * Constructor
	 */
	public OverrideConfigGridItemDetail(OverrideView overrideView) {
		this.overrideView = overrideView;
		this.allTextFieldsByCategory = new HashMap<>();
		this.tabSheet = new TabSheet();
		this.add(this.tabSheet);
		this.binder = new Binder<>(OverrideConfigEntity.class);
	}

	/**
	 * Build TextFields
	 */
	private void initTextFields() {
		this.allTextFieldsByCategory = new HashMap<>();
		this.binder.getBean().getWeasisPropertyEntities().forEach(p -> {
			TextField propertyField = new TextField(p.getCode());
			Binder<WeasisPropertyEntity> binderWeasisProperty = new Binder<>();
			binderWeasisProperty.bind(propertyField, WeasisPropertyEntity::getValue, WeasisPropertyEntity::setValue);
			binderWeasisProperty.setBean(p);

			if (p.getDescription() != null) {
				UIUtil.setTooltip(propertyField, p.getDescription());
			}

			this.allTextFieldsByCategory.computeIfAbsent(p.getCategory(), k -> new ArrayList<>());
			this.allTextFieldsByCategory.get(p.getCategory()).add(propertyField);
		});

		// Event on textfields
		this.allTextFieldsByCategory.keySet().forEach(k -> this.allTextFieldsByCategory.get(k).forEach(field -> {
			// Set read only by default
			field.setReadOnly(true);
			// Handle event double-click on textfields: set them editable
			field.getElement().addEventListener("dblclick", event -> field.setReadOnly(false));
		}));
	}

	/**
	 * Determine texFields to display
	 */
	private void determineTextFieldsDisplay() {
		// Handle tabSheet
		this.setResponsiveSteps(new ResponsiveStep("0", 1));
		this.remove(this.tabSheet);
		this.tabSheet = new TabSheet();
		this.tabSheet.setWidthFull();

		// Retrieve the different categories of the properties of the overrideConfig
		Set<WeasisPropertyCategory> weasisPropertyCategories = this.binder.getBean()
			.getWeasisPropertyEntities()
			.stream()
			.map(WeasisPropertyEntity::getCategory)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// Add layouts in tab-sheet and add tab-sheet in current formLayout
		weasisPropertyCategories.forEach(c -> this.tabSheet.add(c.getLabel(), this.buildCategoryFormLayout(c)));
		this.add(this.tabSheet);
	}

	/**
	 * Build category layout and add all texFields link to this category
	 * @param weasisPropertyCategory Category to evaluate
	 * @return Layout built
	 */
	private FormLayout buildCategoryFormLayout(WeasisPropertyCategory weasisPropertyCategory) {
		FormLayout categoryFormLayout = new FormLayout();
		this.allTextFieldsByCategory.get(weasisPropertyCategory).forEach(categoryFormLayout::add);
		categoryFormLayout.setResponsiveSteps(new ResponsiveStep("0", 4));
		categoryFormLayout.setWidthFull();
		return categoryFormLayout;
	}

	/**
	 * Determine textfields to display and set values
	 * @param overrideConfigEntity values to evaluate
	 */
	public void buildDetailsToDisplay(OverrideConfigEntity overrideConfigEntity) {
		this.binder.setBean(overrideConfigEntity);
		// Build textFields
		this.initTextFields();
		// Build layout to display them
		this.determineTextFieldsDisplay();
		// Events on textFields
		this.overrideView.textFieldsPackageOverrideGridEventsListener();
	}

}
