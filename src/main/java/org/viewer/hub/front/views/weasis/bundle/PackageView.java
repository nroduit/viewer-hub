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

package org.viewer.hub.front.views.weasis.bundle;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.weasis.bundle.override.OverrideView;
import org.viewer.hub.front.views.weasis.bundle.repository.WeasisRepositoryView;

@PageTitle("Package")
@Route(AbstractView.WEASIS + PackageView.ROUTE)
@Menu(order = 2, icon = LineAwesomeIconUrl.BOX_OPEN_SOLID)
@Secured({ "ROLE_admin" })
@Component
@UIScope
public class PackageView extends Composite<VerticalLayout> implements BeforeEnterObserver {

	public static final String ROUTE = "/package";

	private static final String TAB_PARAM = "tab";

	private static final String AVAILABLE_TAB_VALUE = "available";

	private final Tabs tabs;

	private final Tab availableTab;

	private final OverrideView overrideView;

	private final WeasisRepositoryView weasisRepositoryView;

	private final Div content = new Div();

	@Autowired
	public PackageView(OverrideView overrideView, WeasisRepositoryView weasisRepositoryView) {
		this.overrideView = overrideView;
		this.weasisRepositoryView = weasisRepositoryView;

		VerticalLayout packageLayout = getContent();
		packageLayout.setSizeFull();

		Tab installedTab = new Tab(LineAwesomeIcon.SERVER_SOLID.create(), new Span("Installed"));
		this.availableTab = new Tab(LineAwesomeIcon.DOWNLOAD_SOLID.create(), new Span("Available"));
		this.tabs = new Tabs(installedTab, this.availableTab);

		tabs.getStyle().set("flex-grow", "1");
		tabs.setWidth("100%");

		// Add a listener to change the content depending on the selected tab
		tabs.addSelectedChangeListener(event -> {
			content.removeAll();
			if (event.getSelectedTab().equals(installedTab)) {
				content.add(this.overrideView);
			}
			else if (event.getSelectedTab().equals(this.availableTab)) {
				content.add(this.weasisRepositoryView);
			}
			else {
				// Unknown tab: nothing to display
			}
			// Keep the active tab in the URL so that a page refresh (F5) restores it
			String url = event.getSelectedTab().equals(this.availableTab)
					? "%s%s?%s=%s".formatted(AbstractView.WEASIS, ROUTE, TAB_PARAM, AVAILABLE_TAB_VALUE)
					: "%s%s".formatted(AbstractView.WEASIS, ROUTE);
			event.getSource().getUI().ifPresent(ui -> ui.getPage().getHistory().replaceState(null, url));
		});

		// Show the view override by default
		content.add(this.overrideView);
		content.setSizeFull();

		packageLayout.add(tabs, content);
		packageLayout.expand(content);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		boolean available = event.getLocation()
			.getQueryParameters()
			.getSingleParameter(TAB_PARAM)
			.filter(AVAILABLE_TAB_VALUE::equals)
			.isPresent();
		this.tabs.setSelectedTab(available ? this.availableTab : this.tabs.getTabAt(0));
	}

}
