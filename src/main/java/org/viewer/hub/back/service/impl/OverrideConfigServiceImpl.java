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

package org.viewer.hub.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntityPK;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.OverrideConfigRepository;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.back.repository.specification.OverrideConfigSpecification;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.front.views.override.component.OverrideConfigFilter;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OverrideConfigServiceImpl implements OverrideConfigService {

	// Repositories
	private final OverrideConfigRepository overrideConfigRepository;

	private final TargetRepository targetRepository;

	@Autowired
	public OverrideConfigServiceImpl(final OverrideConfigRepository overrideConfigRepository,
			final TargetRepository targetRepository) {
		this.overrideConfigRepository = overrideConfigRepository;
		this.targetRepository = targetRepository;
	}

	@Override
	public OverrideConfigEntity create(OverrideConfigEntity overrideConfig) {
		return this.overrideConfigRepository.save(overrideConfig);
	}

	@Override
	public OverrideConfigEntity update(OverrideConfigEntity overrideConfig) {
		OverrideConfigEntityPK overrideConfigEntityPK = new OverrideConfigEntityPK();
		overrideConfigEntityPK.setPackageVersionId(overrideConfig.getPackageVersion().getId());
		overrideConfigEntityPK.setLaunchConfigId(overrideConfig.getLaunchConfig().getId());
		overrideConfigEntityPK.setTargetId(overrideConfig.getTarget().getId());
		overrideConfig.setOverrideConfigEntityPK(overrideConfigEntityPK);

		// Fill the link between the property and the overrideConfig parent before saving
		overrideConfig.getWeasisPropertyEntities().forEach(p -> p.setOverrideConfigEntity(overrideConfig));

		return this.overrideConfigRepository.save(overrideConfig);
	}

	@Override
	public OverrideConfigEntity retrieveProperties(Long packageVersionId, Long launchConfigId, Long groupId) {
		return this.overrideConfigRepository.findByPackageVersionIdAndLaunchConfigIdAndTargetId(packageVersionId,
				launchConfigId, groupId);
	}

	@Override
	public OverrideConfigEntity retrieveDefaultGroupProperties(Long packageVersionId, Long launchConfigId) {
		return this.overrideConfigRepository.findByPackageVersionIdAndLaunchConfigIdAndTargetName(packageVersionId,
				launchConfigId, TargetType.DEFAULT.getCode());
	}

	@Override
	public boolean existOverrideConfigWithVersionConfigTarget(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig, TargetEntity target) {
		return this.overrideConfigRepository.existsByPackageVersionIdAndLaunchConfigIdAndTargetId(
				packageVersion.getId(), launchConfig.getId(), target.getId());
	}

	@Override
	public void saveAll(Set<OverrideConfigEntity> overrideConfigEntities) {
		// Fill the link between the property and the overrideConfig parent before saving
		overrideConfigEntities.forEach(o -> o.getWeasisPropertyEntities().forEach(p -> p.setOverrideConfigEntity(o)));

		// Save
		this.overrideConfigRepository.saveAll(overrideConfigEntities);
	}

	@Override
	public Set<PackageVersionEntity> retrieveDistinctPackageVersionEntities() {
		return this.overrideConfigRepository.findAll()
			.stream()
			.map(OverrideConfigEntity::getPackageVersion)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<LaunchConfigEntity> retrieveDistinctLaunchConfigEntities() {
		return this.overrideConfigRepository.findAll()
			.stream()
			.map(OverrideConfigEntity::getLaunchConfig)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<LaunchConfigEntity> retrieveDistinctLaunchConfigEntitiesByPackageVersion(
			PackageVersionEntity packageVersionEntity) {
		if (packageVersionEntity != null) {
			return this.overrideConfigRepository.findByPackageVersionId(packageVersionEntity.getId())
				.stream()
				.map(OverrideConfigEntity::getLaunchConfig)
				.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	@Override
	public Set<TargetEntity> retrieveDistinctGroupEntities() {
		return this.targetRepository.findAll()
			.stream()
			.filter(t -> Objects.equals(t.getType(), TargetType.HOST_GROUP)
					|| Objects.equals(t.getType(), TargetType.USER_GROUP)
					|| Objects.equals(t.getType(), TargetType.DEFAULT))
			.collect(Collectors.toSet());
	}

	@Override
	public Page<OverrideConfigEntity> retrieveOverrideConfigsPageable(OverrideConfigFilter filter, Pageable pageable) {
		Page<OverrideConfigEntity> overrideConfigsFound;
		if (!filter.hasFilter()) {
			// No filter
			overrideConfigsFound = this.overrideConfigRepository.findAll(pageable);
		}
		else {
			// Create the specification and query the override_config table
			Specification<OverrideConfigEntity> overrideConfigSpecification = new OverrideConfigSpecification(filter);
			overrideConfigsFound = this.overrideConfigRepository.findAll(overrideConfigSpecification, pageable);
		}
		return overrideConfigsFound;
	}

	@Override
	public int countOverrideConfigs(OverrideConfigFilter filter) {
		int countOverrideConfigs;

		if (!filter.hasFilter()) {
			// No filter
			countOverrideConfigs = (int) this.overrideConfigRepository.count();
		}
		else {
			// Create the specification and query the override config table
			Specification<OverrideConfigEntity> overrideConfigSpecification = new OverrideConfigSpecification(filter);
			countOverrideConfigs = (int) this.overrideConfigRepository.count(overrideConfigSpecification);
		}
		return countOverrideConfigs;
	}

	@Override
	public boolean doesOverrideConfigAlreadyExists(OverrideConfigEntity overrideConfigEntity) {
		if (overrideConfigEntity != null && overrideConfigEntity.getPackageVersion() != null
				&& overrideConfigEntity.getLaunchConfig() != null && overrideConfigEntity.getTarget() != null) {
			return this.overrideConfigRepository.existsByPackageVersionIdAndLaunchConfigIdAndTargetId(
					overrideConfigEntity.getPackageVersion().getId(), overrideConfigEntity.getLaunchConfig().getId(),
					overrideConfigEntity.getTarget().getId());
		}
		return false;
	}

	@Override
	@Transactional
	public void deleteAllOverrideConfigEntitiesByPackageVersion(PackageVersionEntity packageVersion) {
		this.overrideConfigRepository.deleteByPackageVersion(packageVersion);
	}

	@Override
	@Transactional
	public void deleteAllOverrideConfigEntitiesByPackageVersionAndLaunchConfig(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig) {
		this.overrideConfigRepository.deleteByPackageVersionAndLaunchConfig(packageVersion, launchConfig);
	}

	@Override
	@Transactional
	public void deleteOverrideConfigEntity(OverrideConfigEntity overrideConfigEntity) {
		this.overrideConfigRepository.delete(overrideConfigEntity);
	}

}
