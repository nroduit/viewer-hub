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

package org.weasis.manager.back.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.weasis.manager.back.enums.TargetType;

/**
 * TargetType Converter: used to store enum in database
 */
@Converter(autoApply = true)
public class TargetTypeConverter implements AttributeConverter<TargetType, String> {

	@Override
	public String convertToDatabaseColumn(TargetType targetType) {
		return targetType.getCode();
	}

	@Override
	public TargetType convertToEntityAttribute(String code) {
		return TargetType.fromCode(code);
	}

}