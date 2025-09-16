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

package org.viewer.hub.back.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.viewer.hub.back.enums.Viewer;

/**
 * TargetType Converter: used to store enum in database
 */
@Converter(autoApply = true)
public class TargetViewerTypeConverter implements AttributeConverter<Viewer, String> {

	@Override
	public String convertToDatabaseColumn(Viewer targetType) {
		return targetType.name();
	}

	@Override
	public Viewer convertToEntityAttribute(String name) {
		return Viewer.valueOf(name);
	}

}