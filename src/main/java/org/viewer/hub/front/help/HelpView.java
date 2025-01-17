
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
import com.vaadin.flow.server.StreamResource;
import org.viewer.hub.front.layouts.MainLayout;

import java.io.IOException;

@Route(value = HelpView.ROUTE, layout = MainLayout.class)
@PageTitle("Viewer Hub - Help")
@Tag("help-view")
@Uses(Icon.class)
public class HelpView extends VerticalLayout {

	public static final String VIEW_NAME = "Help";

	public static final String ROUTE = "help";

	public HelpView() throws IOException {
		this.setSizeFull();
		H1 heading = new H1("Help");

		// Features
		H2 featuresHead = new H2("Features");

		// Weasis viewer launch
		Anchor weasisViewerLaunchFeature = new Anchor(new StreamResource("weasis_viewer_launch.md",
				() -> getClass().getResourceAsStream("/help/weasis_viewer_launch.md")), "Weasis viewer launch");
		weasisViewerLaunchFeature.setTarget("_blank");
		weasisViewerLaunchFeature.getElement().setAttribute("router-ignore", true);

		// Association
		Anchor associationFeature = new Anchor(new StreamResource("association_user_host_to_groups.md",
				() -> getClass().getResourceAsStream("/help/association_user_host_to_groups.md")),
				"Association of user or host to groups");
		associationFeature.setTarget("_blank");
		associationFeature.getElement().setAttribute("router-ignore", true);

		// Manifest construction
		Anchor manifestFeature = new Anchor(new StreamResource("manifest_construction.md",
				() -> getClass().getResourceAsStream("/help/manifest_construction.md")),
				"Manifest construction");
		manifestFeature.setTarget("_blank");
		manifestFeature.getElement().setAttribute("router-ignore", true);

		// Manifest storage in redis cache
		Anchor manifestCacheFeature = new Anchor(new StreamResource("manifest_storage_in_redis_cache.md",
				() -> getClass().getResourceAsStream("/help/manifest_storage_in_redis_cache.md")),
				"Manifest storage in redis cache");
		manifestCacheFeature.setTarget("_blank");
		manifestCacheFeature.getElement().setAttribute("router-ignore", true);

		// Package Minio/S3
		Anchor packageS3Feature = new Anchor(new StreamResource("weasis_package_management_bundle_storage_s3.md",
				() -> getClass().getResourceAsStream("/help/weasis_package_management_bundle_storage_s3.md")),
				"Weasis package management: bundle storage on Minio/S3");
		packageS3Feature.setTarget("_blank");
		packageS3Feature.getElement().setAttribute("router-ignore", true);

		// Specific groups launch
		Anchor packageSpecificGroupLaunchFeature = new Anchor(new StreamResource("weasis_package_management_specific_group_launch.md",
				() -> getClass().getResourceAsStream("/help/weasis_package_management_specific_group_launch.md")),
				"Weasis package management: specific group launch");
		packageSpecificGroupLaunchFeature.setTarget("_blank");
		packageSpecificGroupLaunchFeature.getElement().setAttribute("router-ignore", true);

		// Live changed of Weasis properties
		Anchor packageLiveChangeWeasisPropertiesFeature = new Anchor(new StreamResource("weasis_package_management_live_changes_properties.md",
				() -> getClass().getResourceAsStream("/help/weasis_package_management_live_changes_properties.md")),
				"Weasis package management: live changes of Weasis properties");
		packageLiveChangeWeasisPropertiesFeature.setTarget("_blank");
		packageLiveChangeWeasisPropertiesFeature.getElement().setAttribute("router-ignore", true);

		// Versions management of i18n translations used by Weasis
		Anchor i18nTranslationsFeature = new Anchor(new StreamResource("versions_management_i18n_translations_used_by_weasis.md",
				() -> getClass().getResourceAsStream("/help/versions_management_i18n_translations_used_by_weasis.md")),
				" Versions management of i18n translations used by Weasis");
		i18nTranslationsFeature.setTarget("_blank");
		i18nTranslationsFeature.getElement().setAttribute("router-ignore", true);

		// Compatibility management between versions of Weasis installed on clients
		// and versions of resources uploaded in Viewer Hub (storage on Minio S3 +
		// cache)
		Anchor compatibilityFeature = new Anchor(new StreamResource("compatibility_management_weasis_versions.md",
				() -> getClass().getResourceAsStream("/help/compatibility_management_weasis_versions.md")),
				"Compatibility management of Weasis versions");
		compatibilityFeature.setTarget("_blank");
		compatibilityFeature.getElement().setAttribute("router-ignore", true);

		// Pacs connectors
		Anchor pacsConnectorFeature = new Anchor(new StreamResource("pacs_connector_management.md",
				() -> getClass().getResourceAsStream("/help/pacs_connector_management.md")),
				"Pacs connector management");
		pacsConnectorFeature.setTarget("_blank");
		pacsConnectorFeature.getElement().setAttribute("router-ignore", true);

		// Cryptography of launch urls
		Anchor launchUrlCryptographyFeature = new Anchor(new StreamResource("cryptography_launch_urls.md",
				() -> getClass().getResourceAsStream("/help/cryptography_launch_urls.md")),
				"Cryptography of launch urls");
		launchUrlCryptographyFeature.setTarget("_blank");
		launchUrlCryptographyFeature.getElement().setAttribute("router-ignore", true);

		// Add links
		VerticalLayout layout = new VerticalLayout();
		layout.add(heading, featuresHead, weasisViewerLaunchFeature, associationFeature,
				manifestFeature, manifestCacheFeature, packageS3Feature, packageSpecificGroupLaunchFeature,
				packageLiveChangeWeasisPropertiesFeature, i18nTranslationsFeature, compatibilityFeature,
				pacsConnectorFeature, launchUrlCryptographyFeature);
		this.add(layout);
	}

}
