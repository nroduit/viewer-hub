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

package org.viewer.hub.front.layouts;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.util.SecurityUtil;
import org.viewer.hub.front.components.ToggleButtonTheme;

import java.util.List;

/** The main layout. Contains the navigation menu. */
@NpmPackage(value = "@polymer/iron-icons", version = "3.0.1")
@JsModule("@polymer/iron-icons/iron-icons.js")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@CssImport(value = "./styles/shared-styles.css")
@CssImport(value = "./styles/empty.css", include = "lumo-badge")
@Uses(Icon.class)
@Uses(ToggleButton.class)
@Layout
@AnonymousAllowed
@UIScope
public class MainLayout extends AppLayout {

	private H1 viewTitle;

	public MainLayout() {
		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	private void addHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		toggle.setAriaLabel("Menu toggle");

		viewTitle = new H1();
		viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

		addToNavbar(true, toggle, viewTitle);
	}

	private void addDrawerContent() {
		Span appName = new Span("Viewer-Hub");
		appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
		Header header = new Header(appName);

		Scroller scroller = new Scroller(createNavigation());

		addToDrawer(header, scroller, createFooter());
	}

	private SideNav createNavigation() {
		SideNav nav = new SideNav();

		// SideNav for Weasis
		SideNavItem weasisLink = new SideNavItem("Weasis");
		// weasisLink.setPrefixComponent(new Image("logo/weasis.svg", "Weasis"));

		// Menu for Weasis: not filtered yet by application
		List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
		menuEntries.forEach(entry -> {
			if (entry.menuClass() != null && SecurityUtil.isAccessGranted(entry.menuClass())) {
				if (entry.icon() != null) {
					weasisLink.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
				}
				else {
					weasisLink.addItem(new SideNavItem(entry.title(), entry.path()));
				}
			}
		});

		nav.addItem(weasisLink);

		return nav;
	}

	private Footer createFooter() {
		Footer layout = new Footer();

		// logout menu item
		Button logoutButton = new Button("Logout", LineAwesomeIcon.SIGN_OUT_ALT_SOLID.create());
		logoutButton.addClickListener(event -> SecurityUtil.signOut());
		logoutButton.setSizeFull();
		logoutButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

		VerticalLayout themeLayout = new VerticalLayout(/* createIconSwagger(), */ new ToggleButtonTheme(),
				logoutButton);
		themeLayout.getElement().getStyle().set("align-items", "center");
		layout.add(themeLayout);

		return layout;
	}

	/**
	 * Create the swagger icon and link to the correct url
	 */
	private Icon createIconSwagger() {
		Icon swaggerIcon = new Icon(VaadinIcon.COMPILE);
		swaggerIcon.getStyle().set("margin-right", "20px");

		// Redirect to spring doc/swagger
		swaggerIcon.getElement().addEventListener("click", e -> {
			ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			UI.getCurrent()
				.getPage()
				.open(String.format("%s%s", ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
						EndPoint.SPRING_DOC_PATH), "_blank");
		});

		return swaggerIcon;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		return MenuConfiguration.getPageHeader(getContent()).orElse("");
	}

}
