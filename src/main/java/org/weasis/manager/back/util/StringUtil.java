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

package org.weasis.manager.back.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Helper for String
 */
public class StringUtil {

	public static final String COMMA = ",";

	public static final String SPACE = " ";

	public static final String HYPHEN = "-";

	public static final String DOT = ".";

	public static final String SLASH = "/";

	public static final String LEFT_PARENTHESIS = "(";

	public static final String EMPTY_STRING = "";

	private StringUtil() {
		// Private constructor to hide implicit one
	}

	/**
	 * Split comma separated values to Set
	 * @param listToEvaluate List to evaluate
	 * @return Set values
	 */
	public static Set<String> splitCommaSeparatedValuesToList(Set<String> listToEvaluate) {
		LinkedHashSet<String> listToReturn = new LinkedHashSet<>();
		listToEvaluate.stream()
			.filter(valueToFilter -> valueToFilter != null && !valueToFilter.isBlank())
			.forEach(valueToEvaluate -> {
				if (valueToEvaluate.contains(COMMA)) {
					listToReturn.addAll(Stream.of(valueToEvaluate.split(COMMA)).map(String::trim).toList());
				}
				else {
					listToReturn.add(valueToEvaluate.trim());
				}
			});
		return listToReturn;
	}

	/**
	 * Ensure that the path will use / separator even when testing on Windows: used for S3
	 * paths
	 * @param path Path to transform
	 * @return Paths updated
	 */
	public static String pathWithS3Separator(String path) {
		return path != null ? path.replace("\\", "/") : null;
	}

	/**
	 * Ensure that the prefix will not start with / separator
	 * @param prefix Prefix to transform
	 * @return Prefix updated
	 */
	public static String prefixWithoutStartingWithS3Separator(String prefix) {
		return prefix != null && prefix.startsWith("/") ? prefix.substring(1) : prefix;
	}

}
