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

package org.viewer.hub.back.service;

import org.viewer.hub.back.enums.OperationType;

import java.sql.SQLException;

public interface ApplicationPreferenceService {

	/**
	 * Add new Weasis Preferences
	 * @param user User identifier
	 * @param profileName Weasis Profile
	 * @param moduleName Weasis Module
	 * @param preferences Preferences
	 * @throws SQLException
	 */
	void createWeasisPreferences(String user, String profileName, String moduleName, String preferences)
			throws SQLException;

	/**
	 * Returns the Preferences for a User, a Weasis Profile and a Weasis Module OR null if
	 * no preferences is found If no module name is transmitted then we assume that the
	 * request concerns the Weasis Application (module=weasis)
	 * @param user User identifier
	 * @param profileName Weasis Profile
	 * @param moduleName Weasis Module
	 * @param prettyPrint Pretty Print flag
	 * @return the Preferences for a User, a Weasis Profile and a Weasis Module OR null if
	 * no preferences is found
	 * @throws SQLException
	 */
	String readWeasisPreferences(String user, String profileName, String moduleName, boolean prettyPrint)
			throws SQLException;

	String readWeasisPreferences(String user, String profileName, String moduleName) throws SQLException;

	/**
	 * Updates Weasis Preferences If no module name is transmitted then we assume that the
	 * request concerns the Weasis Application (module=weasis) If User doesn't exists in
	 * the DB, preferences are inserted in the DB under this new user If Profile doesn't
	 * exists in the DB, If Module doesn't exists in the DB,
	 * @param user User identifier
	 * @param profileName Weasis Profile
	 * @param moduleName Weasis Module
	 * @param preferences
	 * @throws SQLException
	 */
	OperationType updateWeasisPreferences(String user, String profileName, String moduleName, String preferences)
			throws SQLException;

}
