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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntityPK;
import org.viewer.hub.back.entity.PackageVersionEntity;

import java.util.List;

/**
 * Repository for the entity OverrideConfig.
 */
public interface OverrideConfigRepository extends JpaRepository<OverrideConfigEntity, OverrideConfigEntityPK>,
		JpaSpecificationExecutor<OverrideConfigEntity> {

	/**
	 * Check that the package version (ex: xx.xx.xx-QUALIFIER)/ config (ex:3d) / target
	 * exists in the OverrideConfig repository
	 * @param packageVersionId package version to evaluate
	 * @param launchConfigId launch Config to evaluate
	 * @param targetId target to evaluate
	 * @return true if the override config exists
	 */
	boolean existsByPackageVersionIdAndLaunchConfigIdAndTargetId(Long packageVersionId, Long launchConfigId,
			Long targetId);

	/**
	 * Find by Package Version Id, Launch ConfigId And Target Id
	 * @param packageVersionId poackage version Id
	 * @param launchConfigId launch Config Id
	 * @param targetId target Id
	 * @return overrideConfigEntity found
	 */
	OverrideConfigEntity findByPackageVersionIdAndLaunchConfigIdAndTargetId(Long packageVersionId, Long launchConfigId,
			Long targetId);

	/**
	 * Find by Package Version Id, Launch ConfigId And Target name
	 * @param packageVersionId package version Id
	 * @param launchConfigId launch Config Id
	 * @param targetName target Name
	 * @return overrideConfigEntity found
	 */
	OverrideConfigEntity findByPackageVersionIdAndLaunchConfigIdAndTargetName(Long packageVersionId,
			Long launchConfigId, String targetName);

	/**
	 * Find by Package Version Id
	 * @param packageVersionId package version Id
	 * @return overrideConfigEntities found
	 */
	List<OverrideConfigEntity> findByPackageVersionId(Long packageVersionId);

	/**
	 * Delete all OverrideConfig entities by package version
	 * @param packageVersion package version to evaluate
	 */
	void deleteByPackageVersion(PackageVersionEntity packageVersion);

	/**
	 * Delete all OverrideConfig entities by package version and launch config
	 * @param packageVersion package version to evaluate
	 * @param launchConfig launch Config to evaluate
	 */
	void deleteByPackageVersionAndLaunchConfig(PackageVersionEntity packageVersion, LaunchConfigEntity launchConfig);

}
