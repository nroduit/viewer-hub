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
import org.weasis.manager.back.entity.LaunchConfigEntity;

import java.util.List;

/**
 * Repository for the entity Launch Config.
 */
public interface LaunchConfigRepository extends JpaRepository<LaunchConfigEntity, Long> {

	/**
	 * Find config entity by name
	 * @param configName config Name
	 * @return config entity found
	 */
	LaunchConfigEntity findByName(String configName);

	/**
	 * Find config entity by name ignoring caase
	 * @param configName config Name
	 * @return config entity found
	 */
	LaunchConfigEntity findByNameIgnoreCase(String configName);

	/**
	 * Check if the LaunchConfig corresponding to the name in parameter exists
	 * @param name name to look for
	 * @return true if the launch config with the given name exists
	 */
	boolean existsByName(String name);

	/**
	 * Find LaunchConfigs by names
	 * @param configNames config names to look for
	 * @return list of LaunchConfig found
	 */
	List<LaunchConfigEntity> findByNameIn(List<String> configNames);

}
