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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
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

	private Button iconButton;

	public GroupComboBox() {
		buildComponents(null);
	}

	public GroupComboBox(Set<TargetEntity> groups, TargetEntity selectedGroup) {
		this.getStyle().set("display", "inline-block");
		this.setWidthFull();

		buildComponents(selectedGroup);

		// Set the different groups
		this.comboBox.setItems(groups);

		// Set selected value
		this.comboBox.setValue(selectedGroup);
	}

	/**
	 * Build comboBox and icon button
	 * @param selectedGroup If a group is already selected
	 */
	private void buildComponents(TargetEntity selectedGroup) {
		// Build ComboBox
		this.comboBox = new ComboBox<>();
		this.comboBox.setWidthFull();
		this.comboBox.setItemLabelGenerator(TargetEntity::getName);

		// Renderer
		this.comboBox.setRenderer(new ComponentRenderer<Component, TargetEntity>(item -> {
			Div div = new Div();
			NativeLabel l = new NativeLabel(item.getName());
			div.add(retrieveGroupIcon(item), l);
			return div;
		}));

		// Build icon button
		this.iconButton = selectedGroup != null ? new Button(retrieveGroupIcon(selectedGroup))
				: new Button(new Icon(VaadinIcon.QUESTION));

		// Listener
		this.comboBox.addValueChangeListener(event -> {
			this.iconButton.setIcon(retrieveGroupIcon(event.getValue()));
			this.iconButton.setText("");
		});

		this.add(iconButton);
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
		icon.getStyle().set("padding", "var(--lumo-space-wide-xs");
		return icon;
	}

	public void setLabel(String label) {
		if (StringUtils.isNotBlank(label)) {
			comboBox.setLabel(label);
			iconButton.getStyle().set("margin-top", "12%");
			iconButton.getStyle().set("margin-right", "-3%");
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
