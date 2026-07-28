/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.config.xml;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class allow to sanitize the values when serializing an object in xml
 */
public class XmlSanitizeSerializer extends StdSerializer<String> {

	public XmlSanitizeSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) {
		gen.writeString(Objects.isNull(value) ? null : sanitizeForXml(value));
	}

	static String sanitizeForXml(String input) {
		return input == null ? null
				: input.codePoints()
					.filter(XmlSanitizeSerializer::isValidXmlCodePoint)
					.mapToObj(Character::toString)
					.collect(Collectors.joining());
	}

	private static boolean isValidXmlCodePoint(int cp) {
		return cp == 0x9 || cp == 0xA || cp == 0xD || (cp >= 0x20 && cp <= 0xD7FF) || (cp >= 0xE000 && cp <= 0xFFFD)
				|| (cp >= 0x10000 && cp <= 0x10FFFF);
	}

}
