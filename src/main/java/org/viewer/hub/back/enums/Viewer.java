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
import java.util.Objects;

/**
 * Type of connector
 */
public enum Viewer {

	WEASIS("weasis"),
	OHIF("ohif"),
	SLICER("slicer"),
	RADIANT("radiant"),
	MICRODICOM("microdicom");

	@Getter
	private final String code;

	Viewer(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return this.code;
	}

	public static Viewer fromString(String code) {
		if (code != null) {
			return Arrays.stream(Viewer.values())
					.filter(i -> Objects.equals(code.trim(), i.toString()))
					.findFirst()
					.orElse(null);
		}
		return null;
	}

}
