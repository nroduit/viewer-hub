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

package org.weasis.manager.back.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * Target types.
 * <p>
 * By order of priority (from highest priority to lowest priority): - HOST - HOSTGROUP -
 * USER - USERGROUP
 */
public enum TargetType {

	// Host
	HOST("HOST", "Host", 1),
	// Host group
	HOST_GROUP("HOSTGROUP", "Host Group", 2),
	// User
	USER("USER", "User", 3),
	// User group
	USER_GROUP("USERGROUP", "User Group", 4),
	// Default
	DEFAULT("DEFAULT", "Default", 5);

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Description of the enum
	 */
	private final String description;

	/**
	 * Order of the targets
	 */
	private final int order;

	/**
	 * Constructor
	 * @param code Code of the enum
	 * @param description Description of the enum
	 * @param order Order of the target for sorting purpose
	 */
	TargetType(final String code, final String description, final int order) {
		this.code = code;
		this.description = description;
		this.order = order;
	}

	/**
	 * Get the enum from the code in parameter
	 * @param code Code of the enum
	 * @return TargetType found
	 */
	@JsonCreator
	public static TargetType fromCode(final String code) {
		if (code != null) {
			return Arrays.stream(TargetType.values())
				.filter(targetType -> code.trim().equalsIgnoreCase(targetType.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	/**
	 * Get the enum from the description in parameter
	 * @param description Description of the enum
	 * @return TargetType found
	 */
	public static TargetType fromDescription(final String description) {
		if (description != null) {
			return Arrays.stream(TargetType.values())
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

	public int getOrder() {
		return this.order;
	}

	@Override
	public String toString() {
		return this.description;
	}

}
