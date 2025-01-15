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

package org.weasis.manager.front.layouts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.springframework.security.access.annotation.Secured;
import org.weasis.manager.back.util.SecurityUtil;
import org.weasis.manager.front.components.Menu;
import org.weasis.manager.front.help.HelpView;
import org.weasis.manager.front.views.association.AssociationView;
import org.weasis.manager.front.views.i18n.I18nView;
import org.weasis.manager.front.views.override.OverrideView;
import org.weasis.manager.front.views.preference.application.ApplicationPreferencesView;

/** The main layout. Contains the navigation menu. */
@NpmPackage(value = "@polymer/iron-icons", version = "3.0.1")
@JsModule("@polymer/iron-icons/iron-icons.js")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@CssImport(value = "./styles/shared-styles.css")
@CssImport(value = "./styles/empty.css", include = "lumo-badge")
@Route(value = "mainLayout")
@SuppressWarnings("serial")
@Uses(Icon.class)
@Secured({ "ROLE_admin" })
public class MainLayout extends FlexLayout implements RouterLayout {

	private final Menu menu;

	public MainLayout() {
		this.setSizeFull();
		this.setClassName("main-layout");

		this.menu = new Menu();

		// Add secured Menu
		this.addSecuredMenu(ApplicationPreferencesView.class, ApplicationPreferencesView.VIEW_NAME,
				new Icon(VaadinIcon.CHART_GRID));
		this.addSecuredMenu(AssociationView.class, AssociationView.VIEW_NAME, new Icon(VaadinIcon.LINK));
		this.addSecuredMenu(OverrideView.class, OverrideView.VIEW_NAME, new Icon(VaadinIcon.PACKAGE));
		this.addSecuredMenu(I18nView.class, I18nView.VIEW_NAME, new Icon(VaadinIcon.GLOBE));
		this.addSecuredMenu(HelpView.class, HelpView.VIEW_NAME, new Icon(VaadinIcon.INFO_CIRCLE));

		// Add menu to the layout
		this.add(this.menu);
	}

	/**
	 * Build and add secured menus
	 * @param securedClass View to secure
	 * @param viewName Name of the view
	 * @param icon Icon to apply to the menu
	 */
	private void addSecuredMenu(Class<? extends Component> securedClass, String viewName, Icon icon) {
		if (SecurityUtil.isAccessGranted(securedClass)) {
			this.menu.addView(securedClass, viewName, icon);
		}
	}

}
