/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
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
import org.weasis.manager.back.entity.LaunchPreferredEntity;

import java.util.List;

/**
 * Repository for the entity Launch Preferred.
 */
public interface LaunchPreferredRepository extends JpaRepository<LaunchPreferredEntity, Long> {

	/**
	 * Find preferred entity by name
	 * @param preferredName preferred Name
	 * @return preferred entity found
	 */
	LaunchPreferredEntity findByName(String preferredName);

	/**
	 * Find preferred entity by type
	 * @param preferredType preferred type
	 * @return preferred entity found
	 */
	List<LaunchPreferredEntity> findByType(String preferredType);

	/**
	 * Check if the LaunchPreferred corresponding to the name in parameter exists
	 * @param name name to look for
	 * @return true if the launch preferred with the given name exists
	 */
	boolean existsByName(String name);

	/**
	 * Find LaunchPreferred by names
	 * @param preferredNames preferred names to look for
	 * @return list of LaunchPreferred found
	 */
	List<LaunchPreferredEntity> findByNameIn(List<String> preferredNames);

	/**
	 * Check if the preferred type exists in the launch preferred table
	 * @param preferredType preferred type to look for
	 * @return true if the preferred type exists in the launch preferred table
	 */
	boolean existsByType(String preferredType);

}
