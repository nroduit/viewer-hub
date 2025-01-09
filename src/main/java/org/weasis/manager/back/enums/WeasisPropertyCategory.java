/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum WeasisPropertyCategory {

	OSGI("Osgi"), FELIX("Felix"), WEASIS("Weasis"), ORG("Org"), FRAMEWORK("Framework"), JPEG("Jpeg"),
	DOWNLOAD("Download"), FELIX_CONFIG("Felix Config"), FELIX_INSTALL("Felix Install"), LOG("Log"), LOCK("Lock"),
	LAUNCH("Launch"), DOC("Doc"), GENERAL("General"), DICOM("Dicom"), VIEWER("Viewer"), UI("Ui"), METADATA("Metadata"),
	FACTORY("Factory");

	/**
	 * Label of the enum
	 */
	private final String label;

	/**
	 * Constructor
	 * @param label Label
	 */
	WeasisPropertyCategory(String label) {
		this.label = label;
	}

	public static WeasisPropertyCategory fromLabel(final String label) {
		if (label != null) {
			return Arrays.stream(WeasisPropertyCategory.values())
				.filter(weasisPropertyCategory -> label.trim().equalsIgnoreCase(weasisPropertyCategory.getLabel()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

}
