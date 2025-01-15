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

package org.viewer.hub.back.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;

import static org.viewer.hub.back.enums.WeasisPropertyCategory.DICOM;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.DOC;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.FACTORY;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.FELIX;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.FELIX_CONFIG;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.FELIX_INSTALL;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.GENERAL;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.JPEG;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.LAUNCH;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.LOCK;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.LOG;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.METADATA;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.ORG;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.UI;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.VIEWER;
import static org.viewer.hub.back.enums.WeasisPropertyCategory.WEASIS;

@Getter
public enum WeasisProperties {

	// Weasis
	WEASIS_NAME("weasis.name", "Change the name of the application everywhere in UI", LAUNCH),
	WEASIS_VERSION("weasis.version", "Application version. This property is mandatory to launch Weasis", LOCK),
	WEASIS_PROFILE("weasis.profile",
			"Application profile: when no profile name is provided, the value is \"default\".\nIt allows having a custom preferences' directory on the client side (will not share preferences with other Weasis instances)",
			LAUNCH),
	WEASIS_AET("weasis.aet", "Calling AETitle for DICOM send and DICOM print", DICOM),
	WEASIS_RELEASE_NOTES("weasis.releasenotes", "Releases notes", DOC),
	WEASIS_COPYRIGHTS("weasis.copyrights", "Copyrights", DOC),
	WEASIS_UPDATE_RELEASE("weasis.update.release", "Show a message when a new release is available", LAUNCH),
	WEASIS_MAIN_UI("weasis.main.ui", "Application main user interface bundle. Mandatory with the default launcher.",
			LAUNCH),
	WEASIS_RESOURCES_URL("weasis.resources.url",
			"Application resources (logo, presets, LUTs, dicom annotations configuration...)\n\"resources.zip\" is downloaded again only when the last modified date has changed",
			LAUNCH),
	WEASIS_HELP_ONLINE("weasis.help.online", "Online tutorial", DOC),
	WEASIS_HELP_SHORTCUTS("weasis.help.shortcuts", "List of shortcuts", DOC),
	WEASIS_LOOK_MAC_OSX("weasis.look.macosx",
			"Define the Look and Feel for the first launch related to the platform (macosx,linux,windows,...)", WEASIS),
	WEASIS_LOOK("weasis.look", "Look and feel, if the Substance library is not loaded, Nimbus will be used by default.",
			WEASIS),
	WEASIS_CONTEXT_MENU_LUT_SHAPE("weasis.contextmenu.lutShape", "Show LUT Shape in the contextual menu", UI),
	WEASIS_CONTEXT_MENU_LUT("weasis.contextmenu.lut", "Show LUT in the contextual menu", UI),
	WEASIS_CONTEXT_MENU_FILTER("weasis.contextmenu.filter", "Show Filter in the contextual menu", UI),
	WEASIS_MIN_NATIVE_VERSION("weasis.min.native.version", "Minimal required version of the native installer", LOCK),
	WEASIS_CLEAN_PREVIOUS_VERSION("weasis.clean.previous.version",
			"If true, the plug-ins cache is cleared when the weasis current version is different from the previous launch",
			LAUNCH),
	WEASIS_PORTABLE_DICOM_DIRECTORY("weasis.portable.dicom.directory",
			"For loading DICOMs automatically in the portable Weasis distribution. Comma-separated directories relative to the Weasis executable file.\n"
					+ "# Ex. subdirectory : images/dicom,my images/dicom",
			LAUNCH),
	WEASIS_ALL_CINE_TOOLBAR_VISIBLE("weasis.all.cinetoolbar.visible", "Show all the cine toolbars", UI),
	WEASIS_ALL_KEY_OBJECT_TOOLBAR_VISIBLE("weasis.all.keyobjecttoolbar.visible", "Show all the key object toolbars",
			UI),

	WEASIS_DICOM_VIEWER_2D_ALL_ROTATION_TOOLBAR_VISIBLE("weasis-dicom-viewer2d.all.rotationtoolbar.visible",
			"Show the rotation toolbars in DICOM 2D viewer", UI),
	WEASIS_THEME_MACOSX("weasis.theme.macosx",
			"Define the Look and Feel for the first launch related to the platform (macosx,linux,windows,...)", WEASIS),
	WEASIS_THEME("weasis.theme",
			"FlatLaf Look and feel, see https://www.formdev.com/flatlaf/themes/ and https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-intellij-themes#themes.",
			GENERAL),
	WEASIS_SHOW_DISCLAIMER("weasis.show.disclaimer",
			"Show a disclaimer at the first launch of Weasis (requires to be accepted to start the application)",
			LAUNCH),
	WEASIS_TOOLBAR_MOUSE_LEFT("weasis.toolbar.mouse.left",
			"Left mouse button action, possible values: pan|winLevel|sequence|zoom|rotation|measure|drawings|contextMenu|crosshair|none",
			VIEWER),
	WEASIS_TOOLBAR_MOUSE_MIDDLE("weasis.toolbar.mouse.middle",
			"Middle mouse button action, possible values: pan|winLevel|sequence|zoom|rotation|measure|drawings|contextMenu|crosshair|none",
			VIEWER),
	WEASIS_TOOLBAR_MOUSE_RIGHT("weasis.toolbar.mouse.right",
			"Right mouse button action, possible values: pan|winLevel|sequence|zoom|rotation|measure|drawings|contextMenu|crosshair|none",
			VIEWER),
	WEASIS_TOOLBAR_MOUSE_WHEEL("weasis.toolbar.mouse.wheel",
			"Mouse wheel action, possible values: sequence|zoom|rotation|none", VIEWER),
	WEASIS_TOOLBAR_LAYOUT_BUTTON("weasis.toolbar.layout.button", "Show the layout toolbar", UI),
	WEASIS_TOOLBAR_SYNCH_BUTTON("weasis.toolbar.synch.button", "Show the synch toolbar", UI),
	WEASIS_ACQUIRE_META_GLOBAL_DISPLAY("weasis.acquire.meta.global.display",
			"Comma-separated list of patient and study tags which are displayed in UI. When a required tag has no value, it will be displayed.",
			METADATA),
	WEASIS_ACQUIRE_META_GLOBAL_EDIT("weasis.acquire.meta.global.edit",
			"Comma-separated list of patient and study tags which are editable in UI. When a required tag has no value, it will be editable.",
			METADATA),
	WEASIS_ACQUIRE_META_GLOBAL_REQUIRED("weasis.acquire.meta.global.required",
			"Comma-separated list of patient and study tags which are required to publish an image", METADATA),
	WEASIS_ACQUIRE_META_SERIES_DISPLAY("weasis.acquire.meta.series.display",
			"Comma-separated list of series tags which are displayed in UI. When a required tag has no value, it will be displayed.",
			METADATA),
	WEASIS_ACQUIRE_META_SERIES_EDIT("weasis.acquire.meta.series.edit",
			"Comma-separated list of series tags which are editable in UI. When a required tag has no value, it will be editable.",
			METADATA),
	WEASIS_ACQUIRE_META_SERIES_REQUIRED("weasis.acquire.meta.series.required",
			"Comma-separated list of series tags which are required to publish an image", METADATA),
	WEASIS_ACQUIRE_META_IMAGE_DISPLAY("weasis.acquire.meta.image.display",
			"Comma-separated list of image tags which are displayed in UI. When a required tag has no value, it will be displayed.",
			METADATA),
	WEASIS_ACQUIRE_META_IMAGE_EDIT("weasis.acquire.meta.image.edit",
			"Comma-separated list of image tags which are editable in UI. When a required tag has no value, it will be editable.",
			METADATA),
	WEASIS_ACQUIRE_META_IMAGE_REQUIRED("weasis.acquire.meta.image.required",
			"Comma-separated list of image tags which are required to publish an image", METADATA),
	WEASIS_BASE_VIEWER_2D_ALL_ROTATION_TOOLBAR_ENABLE("weasis-base-viewer2d.all.rotationtoolbar.enable",
			"Show the rotation toolbar with base 2D viewer", UI),
	WEASIS_BASE_VIEWER_2D_ALL_IMPORT_TOOLBAR_ENABLE("weasis-base-viewer2d.all.importtoolbar.enable",
			"enable:  create or not the component (cannot be displayed through the menu Display, if false the visible property has no effect)",
			WEASIS),
	WEASIS_BASE_VIEWER_2D_ALL_MINI_TOOL_ENABLE("weasis-base-viewer2d.all.minitool.enable",
			"Show the mini tool with base 2D viewer", UI),
	WEASIS_BASE_VIEWER_2D_ALL_IMAGE_TOOL_ENABLE("weasis-base-viewer2d.all.imagetool.enable",
			"Show the image tool with base 2D viewer", UI),
	WEASIS_BASE_VIEWER_2D_ALL_MEASURE_TOOL_ENABLE("weasis-base-viewer2d.all.measuretool.enable",
			"enable:  create or not the component (cannot be displayed through the menu Display, if false the visible property has no effect)",
			UI),
	WEASIS_TOOLBAR_SYNCHBOUTON("weasis.toolbar.synchbouton", "", WEASIS),
	WEASIS_TOOLBAR_MOUSEBOUTONS("weasis.toolbar.mouseboutons", "", WEASIS),
	WEASIS_TOOLBAR_LAYOUTBOUTON("weasis.toolbar.layoutbouton", "", WEASIS),
	WEASIS_TOOLBAR_EXPORT_CLIPBOARD_IMG_LIMIT("weasis.toolbar.export.clipboard.img.limit", "", WEASIS),
	WEASIS_SHOW_RELEASE("weasis.show.release", "Show a message when the release has changed", LAUNCH),
	WEASIS_IMPORT_IMAGES("weasis.import.images", "Show the import image toolbar and menu", UI),
	WEASIS_IMPORT_DICOM_QR("weasis.import.dicom.qr", "Show the DICOM Q/R page in the DICOM Export dialog", UI),
	WEASIS_FORCE_3D("weasis.force.3d", "Force to detect a graphic card at every launc", VIEWER),
	WEASIS_EXPORT_DICOM("weasis.export.dicom", "Show the DICOM export menu and dialog", UI),
	WEASIS_EXPLORER_MOREOPTIONS("weasis.explorer.moreoptions", "", WEASIS),
	WEASIS_DICOM_ROOT_UID("weasis.dicom.root.uid",
			"##### Set value for dicom root UID when creating DICOM objects (KO or PR)", DICOM),
	WEASIS_ALL_ZOOMTOOLBAR_ENABLE("weasis.all.zoomtoolbar.enable", "", WEASIS),
	WEASIS_ALL_VIEWERTOOLBAR_ENABLE("weasis.all.viewertoolbar.enable", "", WEASIS),
	WEASIS_ALL_MEASURETOOLBAR_ENABLE("weasis.all.measuretoolbar.enable", "", WEASIS),
	WEASIS_ALL_MEASUREBAR_ENABLE("weasis.all.measuretool.enable", "", WEASIS),
	WEASIS_ALL_IMAGETOOL_ENABLE("weasis.all.imagetool.enable", "", WEASIS),
	WEASIS_ALL_DISPLAYTOOL_ENABLE("weasis.all.displaytool.enable", "", WEASIS),
	WEASIS_ALL_CINETOOLBAR_ENABLE("weasis.all.cinetoolbar.enable", "", WEASIS),
	WEASIS_ALL_BASIC3DTOOLBAR_ENABLE("weasis.all.basic3dtoolbar.enable", "", WEASIS),
	WEASIS_ACQUIRE_META_STUDY_DESCRIPTION("weasis.acquire.meta.study.description",
			"Comma-separated list of study description elements (to obtain a selection in a combo box). Empty value will be an editable text field",
			METADATA),
	WEASIS_ACQUIRE_DEST_PORT("weasis.acquire.dest.port", "Port of DICOM send destination for Dicomizer", DICOM),
	WEASIS_ACQUIRE_DEST_HOST("weasis.acquire.dest.host",
			"Hostname of DICOM send destination for Dicomizer. If no value, the list of DICOM nodes for storage is displayed.",
			DICOM),
	WEASIS_ACQUIRE_DEST_AET("weasis.acquire.dest.aet", "AETitle of DICOM send destination for Dicomizer", DICOM),
	WEASIS_DICOM_VIEWER2D_ALL_ROTATIONTOOLBAR_ENABLE("weasis-dicom-viewer2d.all.rotationtoolbar.enable", "", WEASIS),
	WEASIS_DICOM_VIEWER2D_ALL_LUTTOOLBAR_ENABLE("weasis-dicom-viewer2d.all.luttoolbar.enable", "", WEASIS),
	WEASIS_DICOM_VIEEWER2D_ALL_DCMHEARDERTOOLBAR_VISIBLE("weasis-dicom-viewer2d.all.dcmheadertoolbar.visible", "",
			WEASIS),

	// Org
	ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGE_EXTRA("org.osgi.framework.system.packages.extra",
			"List of additional packages for OSGI framework (by default only java.*  is visible in main class loader)",
			FELIX_CONFIG),
	ORG_OSGI_FRAMEWORK_STORAGE("org.osgi.framework.storage",
			"Location of the bundle cache (if not absolute concatenate with felix.cache.rootdir)", FELIX_CONFIG),
	ORG_OSGI_FRAMEWORK_STORAGE_CLEAN("org.osgi.framework.storage.clean",
			"Controls whether the bundle cache is flushed the first time the framework is initialized (Possible values are \"none\" and \"onFirstInit\")",
			FELIX_CONFIG),
	ORG_OSGI_FRAMEWORK_START_LEVEL_BEGINNING("org.osgi.framework.startlevel.beginning",
			"The initial start level of the framework upon startup", FELIX_CONFIG),
	ORG_APACHE_SLING_COMMONS_LOG_LEVEL("org.apache.sling.commons.log.level",
			"Application logging level. This may be any of the defined logging levels TRACE, DEBUG, INFO, WARN, ERROR",
			LOG),
	ORG_WEASIS_BASE_EXPLORER_DEFAULT_EXPLORER_FACTORY("org.weasis.base.explorer.DefaultExplorerFactory",
			"Base thumbnail explorer", FACTORY),
	ORG_WEASIS_DICOM_EXPLORER_MIME_SYSTEM_APP_FACTORY("org.weasis.dicom.explorer.MimeSystemAppFactory",
			"Open a file with the default system application", FACTORY),
	ORG_WEASIS_DICOM_EXPLORER_DICOM_EXPLORER_FACTORY("org.weasis.dicom.explorer.DicomExplorerFactory",
			"DICOM series explorer", FACTORY),
	ORG_WEASIS_DICOM_EXPLORER_PREF_WADO_PREF_FACTORY("org.weasis.dicom.explorer.pref.WadoPrefFactory", "", ORG),
	ORG_WEASIS_DICOM_EXPLORER_PREF_DOWNLOAD_SERIES_DOWNLOAD_PREF_FACTORY(
			"org.weasis.dicom.explorer.pref.download.SeriesDownloadPrefFactory", "", ORG),
	ORG_WEASIS_DICOM_EXPLORER_PREF_NODE_DICOM_NODE_PREF_FACTORY(
			"org.weasis.dicom.explorer.pref.node.DicomNodePrefFactory", "DICOM node list preferences", FACTORY),
	ORG_WEASIS_BASE_EXPLORER_DEFAULTEXPLORERFACTORY("org.weasis.base.explorer.DefaultExplorerFactory", "", ORG),

	// Felix,
	FELIX_START_LEVEL_BUNDLE("felix.startlevel.bundle", "The start level of newly installed bundles", FELIX_CONFIG),
	FELIX_NATIVE_PROCESSOR_ALIAS_ARM_LE("felix.native.processor.alias.arm_le",
			"Processor alias for 32-bit arm native libraries", LAUNCH),
	FELIX_LOG_LEVEL("felix.log.level",
			"Set the logging levels for OSGI framework  0=None / 1(default)=Error / 2=Warning / 3=Information / 4=Debug",
			LOG),
	FELIX_CACHE_ROOT_DIR("felix.cache.rootdir", "Base directory of the bundle cache", FELIX_CONFIG),
	FELIX_AUTO_START_1("felix.auto.start.1", "OSGI runtime", FELIX_INSTALL),
	FELIX_AUTO_START_2("felix.auto.start.2", "OSGI logger", FELIX_INSTALL),
	FELIX_AUTO_START_3("felix.auto.start.3", "OSGI command and shell", FELIX_INSTALL),
	FELIX_AUTO_START_4("felix.auto.start.4", "OSGI bundle repository", FELIX_INSTALL),
	FELIX_AUTO_START_5("felix.auto.start.5", "OSGI configuration and preferences", FELIX_INSTALL),
	FELIX_AUTO_START_7("felix.auto.start.7", "JAXB and Miglayout", FELIX_INSTALL),
	FELIX_AUTO_START_10("felix.auto.start.10", "Docking-frames, core image, joml and zip", FELIX_INSTALL),
	FELIX_AUTO_START_12("felix.auto.start.12", "Core", FELIX_INSTALL),
	FELIX_AUTO_START_15("felix.auto.start.15", "ImageIO codec", FELIX_INSTALL),
	FELIX_AUTO_START_17("felix.auto.start.17", "", FELIX), FELIX_AUTO_INSTALL_25("felix.auto.install.25", "", FELIX),
	FELIX_AUTO_START_30("felix.auto.start.30", "DICOM Codec", FELIX_INSTALL),
	FELIX_AUTO_START_35("felix.auto.start.35", "Jackson databind and annotation", FELIX_INSTALL),
	FELIX_AUTO_START_40("felix.auto.start.40", "DICOM explorer", FELIX_INSTALL),
	FELIX_AUTO_START_60("felix.auto.start.60", "Base user interface", FELIX_INSTALL),
	FELIX_AUTO_START_45("felix.auto.start.45", "Thumbnail explorer", FELIX_INSTALL),
	FELIX_AUTO_START_70("felix.auto.start.70", "DICOM Viewer 2D and SR viewer", FELIX_INSTALL),
	FELIX_AUTO_START_75("felix.auto.start.75", "DICOM AU and ECG", FELIX_INSTALL),
	FELIX_AUTO_START_85("felix.auto.start.85", "Image editor", FELIX_INSTALL),
	FELIX_AUTO_START_90("felix.auto.start.90", "", FELIX),
	FELIX_AUTO_START_100("felix.auto.start.100", "Viewer 2D for standard images", FELIX_INSTALL),
	FELIX_AUTO_INSTALL_86("felix.auto.install.86", "", FELIX),
	FELIX_AUTO_START_110("felix.auto.start.110", "DICOM send, Q/R and ISO writer", FELIX_INSTALL),
	FELIX_AUTO_INSTALL_23("felix.auto.install.23", "OpenCV native package", FELIX_INSTALL),
	FELIX_AUTO_START_115("felix.auto.start.115", "DICOM RT", FELIX_INSTALL),
	FELIX_AUTO_START_120("felix.auto.start.120", "DICOM Viewer 3D and JOGL", FELIX_INSTALL),
	FELIX_AUTO_INSTALL_120("felix.auto.install.120", "", FELIX),
	FELIX_AUTO_INSTALL_121("felix.auto.install.121", "JOGL native package", FELIX_INSTALL),

	// Framework,
	FRAMEWORK_SYSTEM_PACKAGES_EXTRA_BASIC("framework.system.packages.extra.basic",
			"List of additional packages for Felix (by default only java.* is visible in main class loader)",
			FELIX_CONFIG),

	// JPEG,
	JPEG_LOSSY_RGB_MANUFACTURER_LIST("jpeg.lossy.rgb.manufacturer.list",
			"List of manufacturer DICOM values which using an RGB model for JPEG lossy", JPEG),

	// DOWNLOAD
	DOWNLOAD_CONCURRENT_SERIES_IMAGES("download.concurrent.series.images",
			"The number of concurrently downloaded images in a series", DICOM),
	DOWNLOAD_CONCURRENT_SERIES("download.concurrent.series", "The number of concurrently downloaded series", DICOM);

	/*
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Description of the enum -- GETTER -- Getter for description
	 * @return Description of the enum
	 *
	 */

	private final String description;

	/**
	 * Category
	 */
	private final WeasisPropertyCategory weasisPropertyCategory;

	/**
	 * Constructor
	 * @param code Code of the enum
	 * @param description Description of the enum
	 */
	WeasisProperties(final String code, final String description, final WeasisPropertyCategory weasisPropertyCategory) {
		this.code = code;
		this.description = description;
		this.weasisPropertyCategory = weasisPropertyCategory;
	}

	/**
	 * Get the enum from the code in parameter
	 * @param code Code of the enum
	 * @return weasisPropertyType found
	 */
	@JsonCreator
	public static WeasisProperties fromCode(final String code) {
		if (code != null) {
			return Arrays.stream(WeasisProperties.values())
				.filter(weasisPropertyType -> code.trim().equalsIgnoreCase(weasisPropertyType.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	/**
	 * Get the enum from the description in parameter
	 * @param description Description of the enum
	 * @return weasisPropertyType found
	 */
	public static WeasisProperties fromDescription(final String description) {
		if (description != null) {
			return Arrays.stream(WeasisProperties.values())
				.filter(weasisPropertyType -> description.trim().equalsIgnoreCase(weasisPropertyType.getDescription()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	@Override
	public String toString() {
		return this.description;
	}

}
