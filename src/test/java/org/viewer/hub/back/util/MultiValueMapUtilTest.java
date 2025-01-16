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

package org.viewer.hub.back.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiValueMapUtilTest {

	/**
	 * Test addFirst functionality for MultiValueMap
	 * <p>
	 * Initial data: Map: 1: List { 1, 2 } 2: List { 3, 4 }
	 * <p>
	 * Expected:
	 * <p>
	 * Add value 5 at the first position of values of the key 2 Result: Map: 1: List { 1,
	 * 2 } 2: List { 5, 3, 4 }
	 */
	@Test
	void shouldAddValueAtFirstPositionInListMultiValueMap() {
		// Init data
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("1", "1");
		map.add("1", "2");
		map.add("2", "3");
		map.add("2", "4");

		// Call util
		MultiValueMapUtil.multiValueMapAddFirst(map, "2", "5");

		// Test results
		assertEquals(2, map.size());
		assertEquals(3, map.get("2").size());
		assertEquals("5", map.get("2").get(0));
		assertEquals("3", map.get("2").get(1));
		assertEquals("4", map.get("2").get(2));
	}

	/**
	 * Test addFirst functionality for MultiValueMap
	 * <p>
	 * Initial data: Map: 1: List { 1, 2 } 2: List { 3, 4 }
	 * <p>
	 * Expected:
	 * <p>
	 * Add value key/value 3: List { 5, 6 } Result: Map: 1: List { 1, 2 } 2: List { 3, 4 }
	 * 3: List { 5 }
	 */
	@Test
	void shouldDoARegularAddMultiValueMap() {

		// Init data
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("1", "1");
		map.add("1", "2");
		map.add("2", "3");
		map.add("2", "4");

		// Call util
		MultiValueMapUtil.multiValueMapAddFirst(map, "3", "5");

		// Test results
		assertEquals(3, map.size());
		assertEquals("5", map.get("3").get(0));
	}

}
