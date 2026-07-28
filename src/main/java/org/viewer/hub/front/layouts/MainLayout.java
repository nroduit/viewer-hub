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

package org.viewer.hub.front.layouts;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.viewer.hub.back.constant.EndPoint;
import org.viewer.hub.back.util.SecurityUtil;
import org.viewer.hub.front.components.ToggleButtonTheme;
import org.viewer.hub.front.views.AbstractView;

import java.util.List;

/** The main layout. Contains the navigation menu. */
@NpmPackage(value = "@polymer/iron-icons", version = "3.0.1")
@JsModule("@polymer/iron-icons/iron-icons.js")
@CssImport(value = "./styles/shared-styles.css")
@Uses(Icon.class)
@Uses(ToggleButton.class)
@Layout
@AnonymousAllowed
@UIScope
public class MainLayout extends AppLayout implements AfterNavigationObserver {

	private H1 viewTitle;

	/**
	 * Constructor
	 */
	public MainLayout() {
		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	/**
	 * Create the header content with toggle button and view title
	 */
	private void addHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		toggle.setAriaLabel("Menu toggle");

		viewTitle = new H1();
		viewTitle.addClassNames("text-lg", "m-0");

		addToNavbar(true, toggle, viewTitle);
	}

	/**
	 * Create the drawer content with navigation and footer
	 */
	private void addDrawerContent() {
		Span appName = new Span("Viewer-Hub");
		appName.addClassNames("font-semibold", "text-lg");
		Header header = new Header(appName);

		Scroller scroller = new Scroller(createNavigation());
		// Restrict the drawer scroller to vertical scrolling only: the default (BOTH) shows an
		// unnecessary horizontal scrollbar when the navigation content slightly overflows in width.
		scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

		addToDrawer(header, scroller, createFooter());
	}

	/**
	 * Create the navigation side nav
	 */
	private SideNav createNavigation() {
		SideNav nav = new SideNav();
		List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();

		// Build the settings side nav
		buildSettingsSideNav(menuEntries, nav);

		// weasisLink.setPrefixComponent(new Image("logo/weasis.svg", "Weasis"));

		// Build the weasis side nav
		buildWeasisSideNav(menuEntries, nav);

		return nav;
	}

	/**
	 * Build the Weasis side nav
	 * @param menuEntries List of menu available
	 * @param nav SideNav to populate
	 */
	private static void buildWeasisSideNav(List<MenuEntry> menuEntries, SideNav nav) {
		SideNavItem weasisLink = new SideNavItem("Weasis");
		// Menu for Weasis: not filtered yet by application
		menuEntries.forEach(entry -> {
			if (entry.path().startsWith("/" + AbstractView.WEASIS)
					&& entry.menuClass() != null
					&& SecurityUtil.isAccessGranted(entry.menuClass())) {
				if (entry.icon() != null) {
					weasisLink.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
				}
				else {
					weasisLink.addItem(new SideNavItem(entry.title(), entry.path()));
				}
			}
		});
		nav.addItem(weasisLink);
	}

	/**
	 * Build the settings side nav
	 * @param menuEntries List of menu available
	 * @param nav SideNav to populate
	 */
	private static void buildSettingsSideNav(List<MenuEntry> menuEntries, SideNav nav) {
		// SideNav for Settings
		SideNavItem settingsLink = new SideNavItem("Settings");
		// Menu for Settings
		menuEntries.forEach(entry -> {
			if ((entry.path().startsWith("/" + AbstractView.SETTINGS) || entry.path().equals("/"))
					&& entry.menuClass() != null
					&& SecurityUtil.isAccessGranted(entry.menuClass())) {
				if (entry.icon() != null) {
					settingsLink.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
				}
				else {
					settingsLink.addItem(new SideNavItem(entry.title(), entry.path()));
				}
			}
		});
		nav.addItem(settingsLink);
	}

	/**
	 * Create the footer with theme toggle and logout button
	 */
	private Footer createFooter() {
		Footer layout = new Footer();

		// logout menu item
		Button logoutButton = new Button("Logout", LineAwesomeIcon.SIGN_OUT_ALT_SOLID.create());
		logoutButton.addClickListener(event -> SecurityUtil.signOut());
		logoutButton.setSizeFull();
		logoutButton.getElement().getThemeList().add("primary");

		VerticalLayout themeLayout = new VerticalLayout(/* createIconSwagger(), */ new ToggleButtonTheme(),
				logoutButton);
		// Compact footer: remove the default VerticalLayout padding/margin so it does not
		// consume extra height and push the navigation Scroller into showing a scrollbar.
		themeLayout.setPadding(false);
		themeLayout.setSpacing(true);
		themeLayout.setMargin(false);
		themeLayout.setWidthFull();
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
	public void afterNavigation(AfterNavigationEvent event) {
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		return MenuConfiguration.getPageHeader(getContent()).orElse("");
	}

}
