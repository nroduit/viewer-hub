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

import java.util.Arrays;

public enum PreferredType {

	// Extended Config
	EXT_CFG("ext-cfg"),
	// Config
	CONFIG("config"),
	// Properties
	PROPERTY("pro"),
	// User
	ARGUMENT("arg"),
	// User group
	LAUNCH("launch"),
	// Qualifier
	QUALIFIER("qualifier");

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Constructor
	 * @param code Code of the enum
	 */
	PreferredType(final String code) {
		this.code = code;
	}

	/**
	 * Get the enum from the code in parameter
	 * @param code Code of the enum
	 * @return TargetType found
	 */
	public static PreferredType fromCode(final String code) {
		if (code != null) {
			return Arrays.stream(PreferredType.values())
				.filter(targetType -> code.trim().equalsIgnoreCase(targetType.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	/**
	 * Getter for code
	 * @return Code of the enum
	 */
	public String getCode() {
		return this.code;
	}

	@Override
	public String toString() {
		return "PreferredType{" + "code='" + this.code + '\'' + '}';
	}

}
