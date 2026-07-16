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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlSanitizeSerializerTest {

	/**
	 * Tests all control characters U+0000 to U+001F individually. Allowed chars
	 * tab(0x09), LF(0x0A), CR(0x0D) remain, others removed.
	 */
	@ParameterizedTest(name = "Control char U+{0}")
	@CsvSource({ "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10",
			"11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F" })
	void testControlCharacters(String hexCode) {
		char controlChar = (char) Integer.parseInt(hexCode, 16);
		String input = "A" + controlChar + "B";

		String expected;
		if (controlChar == '\t' || controlChar == '\n' || controlChar == '\r') {
			expected = input; // allowed control chars remain
		}
		else {
			expected = "AB"; // others removed
		}

		String actual = XmlSanitizeSerializer.sanitizeForXml(input);

		assertEquals(expected, actual, "Failed for control char U+" + hexCode);
	}

	/**
	 * Tests valid strings that should remain unchanged.
	 */
	@ParameterizedTest
	@MethodSource("validStringsProvider")
	void testValidStringsRemainUnchanged(String input) {
		assertEquals(input, XmlSanitizeSerializer.sanitizeForXml(input));
	}

	static Stream<String> validStringsProvider() {
		return Stream.of("Normal ASCII text", "Tab\tLF\nCR\r", "Emoji 👍😊🚀", "", null);
	}

}