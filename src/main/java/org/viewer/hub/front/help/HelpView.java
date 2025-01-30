
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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.viewer.hub.front.layouts.MainLayout;

@Route(value = HelpView.ROUTE, layout = MainLayout.class)
@PageTitle("Viewer Hub - Help")
@Tag("help-view")
@Uses(Icon.class)
public class HelpView extends VerticalLayout {

	public static final String VIEW_NAME = "Help";

	public static final String ROUTE = "help";

	public HelpView() {
		this.setSizeFull();
		H1 heading = new H1("Help");

		H2 genDocHead = new H2("General Documentation");

		// General documentation
		Anchor generalDoc = new Anchor("https://weasis.org/en/viewer-hub/", "General documentation");
		generalDoc.setTarget("_blank");

		// Features
		H2 featuresHead = new H2("Features");

		// Weasis viewer launch
		Anchor weasisViewerLaunchFeature = new Anchor("https://weasis.org/en/viewer-hub/api/index.html",
				"Weasis viewer launch");
		weasisViewerLaunchFeature.setTarget("_blank");

		// Association
		Anchor associationFeature = new Anchor("https://weasis.org/en/viewer-hub/user-guide/groups/index.html",
				"Association of user or host to groups");
		associationFeature.setTarget("_blank");

		// Manifest construction and storage
		Anchor manifestFeature = new Anchor("https://weasis.org/en/viewer-hub/manifest/index.html",
				"Manifest construction and storage");
		manifestFeature.setTarget("_blank");

		// Package version
		Anchor packageVersioningFeature = new Anchor(
				"https://weasis.org/en/viewer-hub/user-guide/versioning/index.html",
				"Weasis package management: versioning");
		packageVersioningFeature.setTarget("_blank");

		// Specific groups launch
		Anchor packageQualifierLaunchFeature = new Anchor(
				"https://weasis.org/en/viewer-hub/user-guide/qualifier/index.html",
				"Weasis package management: qualifier");
		packageQualifierLaunchFeature.setTarget("_blank");
		// Versions management of i18n translations used by Weasis
		Anchor i18nTranslationsFeature = new Anchor(
				"https://weasis.org/en/viewer-hub/user-guide/translation/index.html",
				" Versions management of i18n translations used by Weasis");
		i18nTranslationsFeature.setTarget("_blank");

		// Compatibility management between versions of Weasis installed on clients
		// and versions of resources uploaded in viewer-hub (storage on Minio S3 +
		// cache)
		Anchor compatibilityFeature = new Anchor("https://weasis.org/en/viewer-hub/user-guide/package/index.html",
				"Compatibility management of Weasis versions");
		compatibilityFeature.setTarget("_blank");

		// Pacs connectors
		Anchor pacsConnectorFeature = new Anchor("https://weasis.org/en/viewer-hub/connectors/index.html",
				"Pacs connector management");
		pacsConnectorFeature.setTarget("_blank");

		// Cryptography of launch urls
		Anchor launchUrlCryptographyFeature = new Anchor("https://weasis.org/en/viewer-hub/-cryptography/index.html",
				"Cryptography of launch urls");
		launchUrlCryptographyFeature.setTarget("_blank");

		// Add links
		VerticalLayout layout = new VerticalLayout();
		layout.add(heading, genDocHead, generalDoc, featuresHead, weasisViewerLaunchFeature, associationFeature,
				manifestFeature, packageVersioningFeature, packageQualifierLaunchFeature, i18nTranslationsFeature,
				compatibilityFeature, pacsConnectorFeature, launchUrlCryptographyFeature);
		this.add(layout);
	}

}
