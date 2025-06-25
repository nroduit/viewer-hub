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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtil {

	public static final String RETRIEVE_VERSION_QUALIFIER_REGEX = "(\\d+(\\.\\d+)++)(?=\\D|$)";

	public static String retrieveVersionWithoutQualifier(String versionToEvaluate) {
		Pattern pattern = Pattern.compile(RETRIEVE_VERSION_QUALIFIER_REGEX);
		Matcher matcher = pattern.matcher(versionToEvaluate);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public static String retrieveQualifierWithoutVersion(String versionToEvaluate) {
		Pattern pattern = Pattern.compile(RETRIEVE_VERSION_QUALIFIER_REGEX);
		Matcher matcher = pattern.matcher(versionToEvaluate);
		if (matcher.find()) {
			return versionToEvaluate.substring(matcher.end());
		}
		return null;
	}

	/**
	 * Method to count groups of digits separated by dots
	 * @param version to evaluate
	 */
	public static int countDigitsGroups(String version) {
		// Split the string by "."
		String[] groups = version.split("\\.");
		return groups.length;
	}

	/**
	 * Retrieve the 3 first groups of digits of a 4 digits version
	 * @param version Version to evaluate
	 * @return 3 first groups of digits
	 */
	public static String extract3GroupsDigitsOf4GroupsDigitsVersion(String version) {
		// Regular expression to match the first three groups of digits
		Pattern pattern = Pattern.compile("^(\\w+\\.\\w+\\.\\w+)");
		Matcher matcher = pattern.matcher(version);

		if (matcher.find()) {
			return matcher.group(1); // Return the matched portion
		}
		return ""; // Return empty if no match
	}

}
