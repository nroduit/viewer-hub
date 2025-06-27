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

package org.viewer.hub.back.constant;

/** Define the error message to send */
public final class Message {

	// Pacs API No Access
	public static final String PACS_API_NO_ACCESS = "Pacs API No Access";

	// Pacs API Client Error
	public static final String PACS_API_CLIENT_ERROR = "Pacs API Client error";

	// Pacs API Server Error
	public static final String PACS_API_SERVER_ERROR = "Pacs API Server error";

	// Pacs server not available
	public static final String PACS_SERVER_NOT_AVAILABLE = "Pacs server not available";

	/**
	 * Private constructor to hide the implicit public one
	 */
	private Message() {
		// private constructor to hide the implicit public one
	}

}