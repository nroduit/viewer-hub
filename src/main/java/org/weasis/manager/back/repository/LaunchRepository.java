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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.LaunchEntityPK;

import java.util.List;

/**
 * Repository for the entity Launch.
 */
public interface LaunchRepository extends JpaRepository<LaunchEntity, LaunchEntityPK>, JpaSpecificationExecutor {

	/**
	 * Check if a target is associated to a launch
	 * @param targetId target id to check
	 * @return true if a target is associated to a launch
	 */
	boolean existsByLaunchEntityPKTargetId(Long targetId);

	/**
	 * Check if a launch config is associated to a launch
	 * @param launchConfigId launch Config id to check
	 * @return true if a launch config is associated to a launch
	 */
	boolean existsByLaunchEntityPKLaunchConfigId(Long launchConfigId);

	/**
	 * Check if a launch preferred is associated to a launch
	 * @param launchPreferredId launch Preferred id to check
	 * @return true if a launch preferred is associated to a launch
	 */
	boolean existsByLaunchEntityPKLaunchPreferredId(Long launchPreferredId);

	/**
	 * Retrieve launches depending on target id
	 * @param id id to look for
	 * @return launches found
	 */
	List<LaunchEntity> findByLaunchEntityPKTargetId(Long id);

}
