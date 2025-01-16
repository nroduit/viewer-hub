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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.front.views.override.component.OverrideConfigFilter;

import java.util.Set;

public interface OverrideConfigService {

	/**
	 * Create an OverrideConfigEntity
	 * @param overrideConfig OverrideConfigEntity to save
	 * @return OverrideConfigEntity saved
	 */
	OverrideConfigEntity create(OverrideConfigEntity overrideConfig);

	/**
	 * Update an OverrideConfigEntity
	 * @param overrideConfig OverrideConfigEntity to update
	 * @return OverrideConfigEntity updated
	 */
	OverrideConfigEntity update(OverrideConfigEntity overrideConfig);

	/**
	 * Retrieve the configuration properties
	 * @param packageVersionId Package version id
	 * @param launchConfigId Launch config id
	 * @param groupId Group id
	 * @return the configuration found
	 */
	OverrideConfigEntity retrieveProperties(Long packageVersionId, Long launchConfigId, Long groupId);

	/**
	 * Retrieve the configuration properties for default group
	 * @param packageVersionId Package version id
	 * @param launchConfigId Launch config id
	 * @return the configuration found
	 */
	OverrideConfigEntity retrieveDefaultGroupProperties(Long packageVersionId, Long launchConfigId);

	/**
	 * Check that the package version (ex: xx.xx.xx-QUALIFIER)/ config (ex:3d) / target
	 * exists in the OverrideConfig repository
	 * @param packageVersion Package version to evaluate
	 * @param launchConfig Launch Config to evaluate
	 * @param target Target to evaluate
	 * @return true if the override config exists
	 */
	boolean existOverrideConfigWithVersionConfigTarget(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig, TargetEntity target);

	/**
	 * Save all OverrideConfigEntities in parameter
	 * @param overrideConfigEntities OverrideConfigEntities to save
	 */
	void saveAll(Set<OverrideConfigEntity> overrideConfigEntities);

	/**
	 * Retrieve distinct Package version entities
	 * @return Set of PackageVersionEntity found
	 */
	Set<PackageVersionEntity> retrieveDistinctPackageVersionEntities();

	/**
	 * Retrieve distinct LaunchConfigEntity
	 * @return Set of LaunchConfigEntity found
	 */
	Set<LaunchConfigEntity> retrieveDistinctLaunchConfigEntities();

	/**
	 * Retrieve distinct LaunchConfigEntity for a package version
	 * @param packageVersionEntity Package version to evaluate
	 * @return Set of LaunchConfigEntity found
	 */
	Set<LaunchConfigEntity> retrieveDistinctLaunchConfigEntitiesByPackageVersion(
			PackageVersionEntity packageVersionEntity);

	/**
	 * Retrieve distinct Group entities
	 * @return Set of TargetEntity found
	 */
	Set<TargetEntity> retrieveDistinctGroupEntities();

	/**
	 * Retrieve override configs depending on filter and pageable
	 * @param filter Filter to evaluate
	 * @param pageable Pageable to evaluate
	 * @return Override config entities found
	 */
	Page<OverrideConfigEntity> retrieveOverrideConfigsPageable(OverrideConfigFilter filter, Pageable pageable);

	/**
	 * Count override configs depending on filter
	 * @param filter Filter to evaluate
	 * @return Count of override configs entities found
	 */
	int countOverrideConfigs(OverrideConfigFilter filter);

	/**
	 * Check if the overrideConfigEntity in parameter already exists in DB
	 * @param overrideConfigEntity overrideConfigEntity to evaluate
	 * @return true if already existing
	 */
	boolean doesOverrideConfigAlreadyExists(OverrideConfigEntity overrideConfigEntity);

	/**
	 * Delete all OverrideConfigEntities By Package Version
	 * @param packageVersion Package version to delete
	 */
	void deleteAllOverrideConfigEntitiesByPackageVersion(PackageVersionEntity packageVersion);

	/**
	 * Delete all OverrideConfigEntities By Package Version and Launch Config
	 * @param packageVersion Package version to delete
	 * @param launchConfig Launch config to delete
	 */
	void deleteAllOverrideConfigEntitiesByPackageVersionAndLaunchConfig(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig);

	/**
	 * Delete selected overrideConfigEntity
	 * @param overrideConfigEntity to delete
	 */
	void deleteOverrideConfigEntity(OverrideConfigEntity overrideConfigEntity);

}
