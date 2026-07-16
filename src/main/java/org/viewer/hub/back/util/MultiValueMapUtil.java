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

package org.viewer.hub.back.util;

import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.constant.ParamName;

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

	/**
	 * Clean input params
	 * @param params Parameters to evaluate
	 */
	public static void cleanInputParameters(MultiValueMap<String, String> params) {
		// Remove following authentication "continue" param
		params.remove(ParamName.CONTINUE_PARAM);
	}

}
