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

package org.weasis.manager.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weasis.manager.back.entity.ProfileEntity;

/**
 * Repository for the entity Profile.
 */
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

	/**
	 * Retrieve the profile corresponding to the name in parameter
	 * @param profileName profile name
	 * @return the profile found
	 */
	ProfileEntity findByName(String profileName);

	/**
	 * Check if the profile corresponding to the name in parameter exists
	 * @param profileName profile name to look for
	 * @return true if the profile with the given name exists
	 */
	boolean existsByName(String profileName);

}
