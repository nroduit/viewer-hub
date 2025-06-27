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

import java.util.Arrays;

/**
 * Define the type of connection to use when connecting to the nexus repository
 */
@Getter
public enum RepositoryAuthType {

	// Basic authentication
	BASIC("basic"),
	// No authentication
	NONE("none");

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Constructor
	 * @param code Code of the enum
	 */
	RepositoryAuthType(final String code) {
		this.code = code;
	}

	/**
	 * Get the enum from the code in parameter
	 * @param code Code of the enum
	 * @return RepositoryAuthType found
	 */
	public static RepositoryAuthType fromCode(final String code) {
		if (code != null) {
			return Arrays.stream(RepositoryAuthType.values())
				.filter(repositoryAuthType -> code.trim().equalsIgnoreCase(repositoryAuthType.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

}
