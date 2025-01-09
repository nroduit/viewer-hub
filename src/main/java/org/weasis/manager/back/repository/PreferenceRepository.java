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
import org.weasis.manager.back.entity.PreferenceEntity;

import java.util.List;

/**
 * Repository for the entity Preference.
 */
public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long>, JpaSpecificationExecutor {

	/**
	 * Get all the PreferenceEntity with target name in parameter
	 * @param targetName target name to look for
	 * @return list of preference entities matching the target name in parameter
	 */
	List<PreferenceEntity> findByTargetName(String targetName);

	/**
	 * Check if the preference corresponding to the name in parameter exists
	 * @param targetName preference name to look for
	 * @return true if the preference with the given username exists
	 */
	boolean existsByTargetName(String targetName);

}
