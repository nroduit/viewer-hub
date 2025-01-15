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

}
