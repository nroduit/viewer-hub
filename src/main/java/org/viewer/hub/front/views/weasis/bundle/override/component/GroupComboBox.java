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

package org.viewer.hub.front.views.weasis.bundle.override.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;

import java.util.Objects;
import java.util.Set;

/**
 * Combobox managing groups
 */
@Getter
public class GroupComboBox extends HorizontalLayout {

	private ComboBox<TargetEntity> comboBox;

	public GroupComboBox() {
		buildComponents(null);
	}

	public GroupComboBox(Set<TargetEntity> groups, TargetEntity selectedGroup) {
		this.getStyle().set("display", "inline-flex").set("align-items", "center");
		this.setWidthFull();
		this.setSpacing(false);
		this.setPadding(false);

		buildComponents(selectedGroup);

		// Set the different groups
		this.comboBox.setItems(groups);

		// Set selected value
		this.comboBox.setValue(selectedGroup);
	}

	/**
	 * Build comboBox with the group icon displayed as a prefix inside the input field, so
	 * it stays naturally aligned with the input regardless of the presence of a label
	 * above it
	 * @param selectedGroup If a group is already selected
	 */
	private void buildComponents(TargetEntity selectedGroup) {
		// Build ComboBox
		this.comboBox = new ComboBox<>();
		this.comboBox.setWidthFull();
		this.comboBox.setItemLabelGenerator(TargetEntity::getName);

		// Renderer
		this.comboBox.setRenderer(new ComponentRenderer<>(item -> {
			Div div = new Div();
			NativeLabel l = new NativeLabel(item.getName());
			div.add(retrieveGroupIcon(item), l);
			return div;
		}));

		// Prefix icon displayed inside the ComboBox input field
		this.comboBox.setPrefixComponent(
				selectedGroup != null ? retrieveGroupIcon(selectedGroup) : new Icon(VaadinIcon.QUESTION));

		// Listener
		this.comboBox.addValueChangeListener(event -> this.comboBox.setPrefixComponent(
				event.getValue() != null ? retrieveGroupIcon(event.getValue()) : new Icon(VaadinIcon.QUESTION)));

		this.add(comboBox);
	}

	/**
	 * Set different icons depending on the type of the target
	 * @param target Target to evaluate
	 * @return Icon built
	 */
	private Icon retrieveGroupIcon(TargetEntity target) {
		Icon icon;
		if (Objects.equals(target.getType(), TargetType.HOST_GROUP)) {
			icon = new Icon(VaadinIcon.DESKTOP);
			icon.setColor("#226D68");
		}
		else if (Objects.equals(target.getType(), TargetType.USER_GROUP)) {
			icon = new Icon(VaadinIcon.GROUP);
			icon.setColor("#D7572B");
		}
		else if (Objects.equals(target.getType(), TargetType.DEFAULT)) {
			icon = new Icon(VaadinIcon.HOME);
			icon.setColor("#679436");
		}
		else {
			icon = new Icon(VaadinIcon.QUESTION);
			icon.setColor("grey");
		}
		icon.setSize("1.4em");
		return icon;
	}

	public void setLabel(String label) {
		if (StringUtils.isNotBlank(label)) {
			comboBox.setLabel(label);
		}
	}

	public void setPlaceHolder(String placeHolder) {
		if (StringUtils.isNotBlank(placeHolder)) {
			comboBox.setPlaceholder(placeHolder);
		}
	}

	public void setItems(Set<TargetEntity> groups) {
		// Set the different groups
		comboBox.setItems(groups);
	}

}
