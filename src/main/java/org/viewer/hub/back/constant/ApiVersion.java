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

package org.viewer.hub.back.constant;

/**
 * Define api versions
 */
public final class ApiVersion {

	// V1
	public static final String V1_APPLICATION_JSON_VALUE = "application/json;version=1";

	public static final String V1_APPLICATION_XML_VALUE = "application/xml;version=1";

	public static final String V1_TEXT_PLAIN_VALUE = "text/plain;version=1";

	public static final String V1_TEXT_X_JAVA_PROPERTIES_VALUE = "text/x-java-properties;version=1";

	/**
	 * Private constructor to hide the implicit public one
	 */
	private ApiVersion() {
		// private constructor to hide the implicit public one
	}

}