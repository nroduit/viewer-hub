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

import org.viewer.hub.back.model.WeasisModule;

import java.sql.SQLException;
import java.util.List;

public interface ModuleService {

	/**
	 * Returns the list of all Weasis Modules found for a User and a Weasis Profile (name)
	 * @param user User
	 * @param profileName Profile Name
	 * @return the list of all Weasis Modules found for a User and a Weasis Profile
	 * @throws SQLException
	 */
	List<WeasisModule> readWeasisModules(String user, String profileName) throws SQLException;

}
