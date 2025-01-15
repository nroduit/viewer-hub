
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

package org.weasis.manager.front.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.util.SecurityUtil;

@NpmPackage(value = "@polymer/iron-icons", version = "3.0.1")
@JsModule("@polymer/iron-icons/iron-icons.js")
@SuppressWarnings("serial")
@Uses(Icon.class)
public class Menu extends FlexLayout {

	private static final String SHOW_TABS = "show-tabs";

	private final ToggleButtonTheme toggleButtonTheme;

	private Tabs tabs;

	private Icon swaggerIcon;

	public Menu() {
		this.setClassName("menu-bar");

		// Button for toggling the menu visibility on small screens
		final Button showMenu = new Button("Menu", event -> {
			if (this.tabs.getClassNames().contains(SHOW_TABS)) {
				this.tabs.removeClassName(SHOW_TABS);
			}
			else {
				this.tabs.addClassName(SHOW_TABS);
			}
		});
		showMenu.setClassName("menu-button");
		showMenu.addThemeVariants(ButtonVariant.LUMO_SMALL);
		showMenu.setIcon(new Icon(VaadinIcon.MENU));
		this.add(showMenu);

		// Swagger
		this.createIconSwagger();

		// container for the navigation buttons, which are added by addView()
		this.tabs = new Tabs();
		this.tabs.setOrientation(Tabs.Orientation.VERTICAL);
		this.setFlexGrow(1, this.tabs);
		this.add(this.tabs);

		// theme
		this.toggleButtonTheme = new ToggleButtonTheme();
		VerticalLayout themeLayout = new VerticalLayout(this.toggleButtonTheme, this.swaggerIcon);
		themeLayout.getElement().getStyle().set("align-items", "center");
		this.add(themeLayout);

		// logout menu item
		Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
		logoutButton.addClickListener(event -> SecurityUtil.signOut());

		logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		this.add(logoutButton);

		this.setFlexDirection(FlexDirection.COLUMN);
	}

	/**
	 * Add a view to the navigation menu
	 * @param viewClass that has a {@code Route} annotation
	 * @param caption view caption in the menu
	 * @param icon view icon in the menu
	 */
	public void addView(Class<? extends Component> viewClass, String caption, Icon icon) {
		Tab tab = new Tab();
		RouterLink routerLink = new RouterLink(viewClass);
		routerLink.setClassName("menu-link");
		icon.getStyle().set("box-sizing", "border-box").set("margin-inline-end", "var(--lumo-space-s)");
		routerLink.add(icon, new Span(caption));
		tab.add(routerLink);
		this.tabs.add(tab);
	}

	/**
	 * Create the swagger icon and link to the correct url
	 */
	private void createIconSwagger() {
		this.swaggerIcon = new Icon(VaadinIcon.COMPILE);
		this.swaggerIcon.getStyle().set("margin-right", "20px");

		// Redirect to spring doc/swagger
		this.swaggerIcon.getElement().addEventListener("click", e -> {
			ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			UI.getCurrent()
				.getPage()
				.open(String.format("%s%s", ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
						EndPoint.SPRING_DOC_PATH), "_blank");
		});
	}

}
