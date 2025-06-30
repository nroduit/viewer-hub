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

import lombok.Getter;

@Getter
public enum HeaderType {

	// @formatter:off
	MULTIPART_RELATED_APPLICATION_DICOM("multipart/related;type=\"application/dicom\""),
	APPLICATION_DICOM("application/dicom"),
	APPLICATION_DICOM_JSON("application/dicom+json");
	// @formatter:on

	/** Code of the enum */
	private final String code;

	/**
	 * Constructor
	 * @param code Code of the enum
	 */
	HeaderType(String code) {
		this.code = code;
	}

}
