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

import java.util.Arrays;
import java.util.Objects;

public enum IHERequestType {

	STUDY("STUDY"), PATIENT("PATIENT");

	private final String code;

	IHERequestType(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public static IHERequestType fromCode(String code) {
		if (code != null) {
			return Arrays.stream(IHERequestType.values())
				.filter(i -> Objects.equals(code.trim(), i.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

}
