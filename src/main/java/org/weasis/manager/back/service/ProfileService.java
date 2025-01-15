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

package org.weasis.manager.back.service;

import org.weasis.manager.back.model.WeasisProfile;

import java.sql.SQLException;
import java.util.List;

public interface ProfileService {

	/**
	 * Returns the list of all Weasis Profiles for a User
	 * @param user User
	 * @return the list of all Weasis Profiles for a User
	 * @throws SQLException
	 */
	List<WeasisProfile> readProfiles(String user) throws SQLException;

	/**
	 * Get all profiles from DB and convert them into WeasisProfile object
	 * @return a List of WeasisProfile
	 * @throws SQLException
	 */
	List<WeasisProfile> getAllProfiles() throws SQLException;

}
