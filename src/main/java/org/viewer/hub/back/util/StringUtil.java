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

import java.text.DecimalFormat;
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
	 * Convert size in a readable format
	 * @param size Size to convert
	 * @return Readable format
	 */
	public static String convertSizeToReadableFormat(Long size) {
		long ONE_KB = 1024;
		long ONE_MB = ONE_KB * 1024;
		long ONE_GB = ONE_MB * 1024;

		DecimalFormat df = new DecimalFormat("#.##");
		if (size >= ONE_GB) {
			return "%s Go".formatted(df.format((double) size / ONE_GB));
		}
		else if (size >= ONE_MB) {
			return "%s Mo".formatted(df.format((double) size / ONE_MB));
		}
		else if (size >= ONE_KB) {
			return "%s Ko".formatted(df.format((double) size / ONE_KB));
		}
		else {
			return size + " octets";
		}

	}

}
