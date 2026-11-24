
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

package org.viewer.hub.front.help;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Version;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle(HelpView.VIEW_NAME)
@Route(HelpView.ROUTE)
@Menu(order = 4, icon = LineAwesomeIconUrl.INFO_SOLID)
@Uses(Icon.class)
public class HelpView extends VerticalLayout {

	public static final String VIEW_NAME = "Help";

	public static final String ROUTE = "help";

	private static final String UNKNOWN = "Unknown";

	private final String appVersion;

	public HelpView(@Value("${spring.application.version:Development}") String appVersion) {
		this.appVersion = appVersion;
		this.setSizeFull();
		this.setPadding(true);
		this.setSpacing(true);
		this.getStyle().set("font-size", "1.1rem").set("gap", "2rem").set("overflow-y", "auto");

		// Cards stacked vertically: Documentation above About
		FlexLayout cards = new FlexLayout(createDocumentationSection(), createAboutSection());
		cards.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		cards.getStyle().set("gap", "2rem").set("align-items", "flex-start");

		this.add(new H1("Help"), cards);
	}

	private static VerticalLayout createCard(String title) {
		VerticalLayout card = new VerticalLayout();
		card.setSpacing(false);
		card.setPadding(false);
		card.setWidth("auto");
		card.getStyle()
			.set("border", "1px solid color-mix(in srgb, currentColor 20%, transparent)")
			.set("border-radius", "8px")
			.set("padding", "1.5rem 2rem")
			.set("min-width", "28rem")
			.set("gap", "0.5rem");
		H2 heading = new H2(title);
		heading.getStyle().set("margin", "0 0 1rem 0");
		card.add(heading);
		return card;
	}

	private static Component createDocumentationSection() {
		VerticalLayout card = createCard("Documentation");
		Anchor generalDoc = new Anchor("https://weasis.org/en/viewer-hub/", "General documentation");
		generalDoc.setTarget("_blank");
		card.add(generalDoc);
		return card;
	}

	private Component createAboutSection() {
		VerticalLayout aboutLayout = createCard("About");

		String springVersion = SpringVersion.getVersion();
		String springBootVersion = SpringBootVersion.getVersion();

		aboutLayout.add(row("Viewer-Hub", appVersion), row("Vaadin", Version.getFullVersion()),
				row("Spring", springVersion != null ? springVersion : UNKNOWN),
				row("Spring Boot", springBootVersion != null ? springBootVersion : UNKNOWN),
				row("Java", "%s (%s)".formatted(System.getProperty("java.version"), System.getProperty("java.vendor"))),
				row("System", "%s %s (%s)".formatted(System.getProperty("os.name"), System.getProperty("os.version"),
						System.getProperty("os.arch"))));
		return aboutLayout;
	}

	private static Component row(String label, String value) {
		Span name = new Span(label);
		name.getStyle().set("font-weight", "600").set("min-width", "9rem");
		Span val = new Span(value);
		val.getStyle().set("opacity", "0.85");
		HorizontalLayout row = new HorizontalLayout(name, val);
		row.setSpacing(true);
		row.setPadding(false);
		row.setWidthFull();
		row.getStyle()
			.set("padding", "0.6rem 0")
			.set("border-bottom", "1px solid color-mix(in srgb, currentColor 10%, transparent)");
		return row;
	}

}
