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

package org.viewer.hub.front.components;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@Uses(Icon.class)
@Uses(ToggleButton.class)
public class ToggleButtonTheme extends HorizontalLayout {

	private final ToggleButton toggleButton;

	private final String THEME_COLOR_KEY = "theme-variant";

	// Aura selects light/dark via the CSS color-scheme property (light-dark()), not the Lumo
	// "theme" attribute. These are the stored and applied color-scheme values.
	private static final String DARK = "dark";

	private static final String LIGHT = "light";

	public ToggleButtonTheme() {
		// Keep the theme switch compact so it does not add extra vertical space in the drawer footer.
		setPadding(false);
		setMargin(false);
		setSpacing(true);
		setDefaultVerticalComponentAlignment(Alignment.CENTER);

		Icon moonIcon = new Icon(VaadinIcon.MOON_O);
		Icon sunIcon = new Icon(VaadinIcon.SUN_O);
		this.toggleButton = new ToggleButton();
		this.toggleButton.addClassName("toggle");

		// read local storage theme
		UI.getCurrent()
			.getPage()
			.executeJs("return localStorage.getItem($0)", this.THEME_COLOR_KEY)
			.then(String.class, themeColor -> {
				if (DARK.equals(themeColor)) {
					this.toggleButton.setValue(true);
				}
				else if (LIGHT.equals(themeColor)) {
					this.toggleButton.setValue(false);
				}
			});

		this.toggleButton.addValueChangeListener(toggleButtonBooleanComponentValueChangeEvent -> {
			String colorScheme = Boolean.TRUE.equals(toggleButtonBooleanComponentValueChangeEvent.getValue()) ? DARK
					: LIGHT;
			UI.getCurrent().getPage().executeJs("document.documentElement.style.colorScheme = $0", colorScheme);
			UI.getCurrent().getPage().executeJs("localStorage.setItem($0, $1)", this.THEME_COLOR_KEY, colorScheme);
		});
		this.add(sunIcon, this.toggleButton, moonIcon);
	}

}
