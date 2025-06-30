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

/**
 * Query params used to query Nexus repository
 */
@Getter
public enum NexusApiQueryParamType {

	GROUP("group"), REPOSITORY("repository"), MAVEN_EXTENSION("maven.extension"), NAME("name"), VERSION("version"),
	ZIP_EXTENSION("zip");

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Constructor
	 * @param code Code of the enum
	 */
	NexusApiQueryParamType(String code) {
		this.code = code;
	}

}
