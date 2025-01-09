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

package org.weasis.manager.back.util;

import org.springframework.util.MultiValueMap;

public class MultiValueMapUtil {

	/**
	 * If not present add value at the first position in the list of values of the key
	 * selected otherwise make a regular add..
	 * @param map Map to process
	 * @param key Key selected
	 * @param value Value to add
	 */
	public static void multiValueMapAddFirst(MultiValueMap<String, String> map, String key, String value) {
		if (map.get(key) == null) {
			map.add(key, value);
		}
		else {
			map.get(key).add(0, value);
		}
	}

}
