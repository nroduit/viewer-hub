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

package org.viewer.hub.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.viewer.hub.back.entity.ModuleEntity;

import java.util.List;

/**
 * Repository for the entity Module.
 */
public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {

	/**
	 * Retrieve the module corresponding to the name in parameter
	 * @param moduleName module name to look for
	 * @return the module corresponding to the name in parameter
	 */
	ModuleEntity findFirstByName(String moduleName);

	/**
	 * Retrieve the list of modules starting with the name in parameter
	 * @param moduleName module name to look for
	 * @return modules starting with the name in parameter
	 */
	List<ModuleEntity> findByNameStartsWith(String moduleName);

	/**
	 * Retrieve the list of modules not like the name in parameter
	 * @param moduleName module name to look for
	 * @return modules not like the name in parameter
	 */
	List<ModuleEntity> findByNameNotLike(String moduleName);

	/**
	 * Retrieve the module corresponding to the name in parameter
	 * @param moduleName module name
	 * @return the module found
	 */
	ModuleEntity findByName(String moduleName);

	/**
	 * Check if the module corresponding to the name in parameter exists
	 * @param moduleName module name to look for
	 * @return true if the module with the given name exists
	 */
	boolean existsByName(String moduleName);

}
