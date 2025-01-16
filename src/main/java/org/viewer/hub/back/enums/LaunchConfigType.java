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
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LaunchConfigType {

	// Default
	DEFAULT("default", "Default");

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Description of the enum
	 */
	private final String description;

	/**
	 * Constructor
	 * @param code Code of the enum
	 * @param description Description of the enum
	 */
	LaunchConfigType(final String code, final String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * Get the enum from the code in parameter
	 * @param code Code of the enum
	 * @return LaunchConfigType found
	 */
	@JsonCreator
	public static LaunchConfigType fromCode(final String code) {
		if (code != null) {
			return Arrays.stream(LaunchConfigType.values())
				.filter(launchConfigType -> code.trim().equalsIgnoreCase(launchConfigType.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	/**
	 * Get the enum from the description in parameter
	 * @param description Description of the enum
	 * @return LaunchConfigType found
	 */
	public static LaunchConfigType fromDescription(final String description) {
		if (description != null) {
			return Arrays.stream(LaunchConfigType.values())
				.filter(targetType -> description.trim().equalsIgnoreCase(targetType.getDescription()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	/**
	 * Getter for code
	 * @return Code of the enum
	 */
	@JsonValue
	public String getCode() {
		return this.code;
	}

	/**
	 * Getter for description
	 * @return Description of the enum
	 */
	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return this.description;
	}

}
