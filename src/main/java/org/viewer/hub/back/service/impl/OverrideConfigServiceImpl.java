/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.service.impl;

import org.apache.commons.lang3.StringUtils;
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
import org.viewer.hub.back.entity.WeasisPropertyEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.OverrideConfigRepository;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.back.repository.specification.OverrideConfigSpecification;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.back.util.PageUtil;
import org.viewer.hub.front.views.weasis.bundle.override.component.OverrideConfigFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
	public OverrideConfigEntity createUpdate(OverrideConfigEntity overrideConfig) {
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
		return this.overrideConfigRepository
			.findOptionalByPackageVersionIdAndLaunchConfigIdAndTargetId(packageVersionId, launchConfigId, groupId)
			.orElse(null);
	}

	@Override
	public OverrideConfigEntity retrieveDefaultGroupProperties(Long packageVersionId, Long launchConfigId) {
		return this.overrideConfigRepository
			.findOptionalByPackageVersionIdAndLaunchConfigIdAndTargetName(packageVersionId, launchConfigId,
					TargetType.DEFAULT.getCode())
			.orElse(null);
	}

	@Override
	public boolean existOverrideConfigWithVersionConfigTarget(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig, TargetEntity target) {
		return this.overrideConfigRepository.existsByPackageVersionIdAndLaunchConfigIdAndTargetId(
				packageVersion.getId(), launchConfig.getId(), target.getId());
	}

	@Override
	public boolean existOverrideConfigWithVersionConfigTargetAndBuildId(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig, TargetEntity target, String buildId) {
		if (packageVersion == null || packageVersion.getId() == null || launchConfig == null
				|| launchConfig.getId() == null || target == null || target.getId() == null) {
			return false;
		}
		// Null safe comparison: a legacy configuration and a legacy version both have a
		// null build id and are therefore considered up-to-date
		return this.overrideConfigRepository
			.findOptionalByPackageVersionIdAndLaunchConfigIdAndTargetId(packageVersion.getId(), launchConfig.getId(),
					target.getId())
			.filter(overrideConfig -> Objects.equals(overrideConfig.getBuildId(), buildId))
			.isPresent();
	}

	@Override
	@Transactional
	public void saveAll(Set<OverrideConfigEntity> overrideConfigEntities) {
		if (overrideConfigEntities == null) {
			return;
		}

		// Upsert: an OverrideConfigEntity already in db is updated with the properties in
		// parameter (case of a version re-uploaded with a new build id: the primary key
		// package version/launch config/target does not change) instead of being inserted
		// a second time
		Set<OverrideConfigEntity> entitiesToSave = overrideConfigEntities.stream()
			.filter(Objects::nonNull)
			.map(this::refreshExistingOrPrepareNew)
			.collect(Collectors.toCollection(LinkedHashSet::new));

		// Save
		this.overrideConfigRepository.saveAll(entitiesToSave);
	}

	/**
	 * If the OverrideConfigEntity in parameter is already in db, replace the properties
	 * of the entity found by the ones in parameter (orphan removal deletes the properties
	 * of the previous build), otherwise prepare the new entity to be inserted.
	 * @param overrideConfig OverrideConfigEntity built from the configuration files
	 * @return the entity to save
	 */
	private OverrideConfigEntity refreshExistingOrPrepareNew(OverrideConfigEntity overrideConfig) {
		OverrideConfigEntity existing = overrideConfig.getPackageVersion() != null
				&& overrideConfig.getPackageVersion().getId() != null && overrideConfig.getLaunchConfig() != null
				&& overrideConfig.getLaunchConfig().getId() != null && overrideConfig.getTarget() != null
				&& overrideConfig.getTarget().getId() != null
						? this.overrideConfigRepository
							.findOptionalByPackageVersionIdAndLaunchConfigIdAndTargetId(
									overrideConfig.getPackageVersion().getId(),
									overrideConfig.getLaunchConfig().getId(), overrideConfig.getTarget().getId())
							.orElse(null)
						: null;

		// Nothing to merge when the entity is not in db yet or when it is already the
		// managed instance (in that case its properties are the ones to keep)
		if (existing == null || existing == overrideConfig) {
			// Fill the link between the property and the overrideConfig parent before
			// saving
			overrideConfig.getWeasisPropertyEntities().forEach(p -> p.setOverrideConfigEntity(overrideConfig));
			return overrideConfig;
		}

		// Update in place the entity already managed: the properties of the previous
		// build are removed (orphan removal) and replaced by the new ones
		existing.setBuildId(overrideConfig.getBuildId());
		List<WeasisPropertyEntity> propertiesToSet = new ArrayList<>(overrideConfig.getWeasisPropertyEntities());
		propertiesToSet.forEach(p -> p.setOverrideConfigEntity(existing));
		existing.getWeasisPropertyEntities().clear();
		existing.getWeasisPropertyEntities().addAll(propertiesToSet);

		return existing;
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
		else if (StringUtils.isNotBlank(filter.getWeasisProfile())) {
			// Create the specification, query the override_config table and filter
			// depending on the value of weasis profile
			Specification<OverrideConfigEntity> overrideConfigSpecification = new OverrideConfigSpecification(filter);
			overrideConfigsFound = PageUtil
				.convertToPage(this.overrideConfigRepository.findAll(overrideConfigSpecification)
					.stream()
					.filter(o -> doesOverrideConfigPropertyContainsCodeFilter(o, "weasis.profile",
							filter.getWeasisProfile()))
					.toList(), pageable);
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
		else if (StringUtils.isNotBlank(filter.getWeasisProfile())) {
			// Create the specification, query the override config table and filter
			// depending on the value of weasis profile
			Specification<OverrideConfigEntity> overrideConfigSpecification = new OverrideConfigSpecification(filter);
			countOverrideConfigs = this.overrideConfigRepository.findAll(overrideConfigSpecification)
				.stream()
				.filter(o -> doesOverrideConfigPropertyContainsCodeFilter(o, "weasis.profile",
						filter.getWeasisProfile()))
				.toList()
				.size();
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

	@Override
	@Transactional
	public OverrideConfigEntity modifyTarget(OverrideConfigEntity overrideConfigEntity, TargetEntity targetEntity) {
		OverrideConfigEntity overrideConfigEntityModified = new OverrideConfigEntity();
		overrideConfigEntityModified.setLaunchConfig(overrideConfigEntity.getLaunchConfig());
		overrideConfigEntityModified.setPackageVersion(overrideConfigEntity.getPackageVersion());
		overrideConfigEntityModified.setTarget(targetEntity);
		// Keep the build the properties have been extracted from
		overrideConfigEntityModified.setBuildId(overrideConfigEntity.getBuildId());
		OverrideConfigEntityPK overrideConfigEntityPK = new OverrideConfigEntityPK();
		overrideConfigEntityPK.setPackageVersionId(overrideConfigEntity.getPackageVersion().getId());
		overrideConfigEntityPK.setLaunchConfigId(overrideConfigEntity.getLaunchConfig().getId());
		overrideConfigEntityPK.setTargetId(targetEntity.getId());
		overrideConfigEntityModified.setOverrideConfigEntityPK(overrideConfigEntityPK);

		// Fill the link between the property and the new overrideConfig before saving
		overrideConfigEntity.getWeasisPropertyEntities()
			.forEach(p -> overrideConfigEntityModified.getWeasisPropertyEntities().add(WeasisPropertyEntity.copy(p)));
		overrideConfigEntityModified.getWeasisPropertyEntities()
			.forEach(p -> p.setOverrideConfigEntity(overrideConfigEntityModified));

		// Replace entity
		OverrideConfigEntity overrideConfigUpdated = this.overrideConfigRepository.save(overrideConfigEntityModified);
		this.overrideConfigRepository.delete(overrideConfigEntity);

		return overrideConfigUpdated;
	}

	/**
	 * Check if the override config contains a property with the code in parameter and if
	 * the value of this code contains a part of the filter in parameter
	 * @param overrideConfigEntity Override Config to evaluate
	 * @param code Code of the property to look for
	 * @param filter Filter to check
	 * @return true if the filter is part of the value of the property
	 */
	private static boolean doesOverrideConfigPropertyContainsCodeFilter(OverrideConfigEntity overrideConfigEntity,
			String code, String filter) {
		WeasisPropertyEntity w = overrideConfigEntity.getWeasisPropertyEntities()
			.stream()
			.filter(p -> Objects.equals(p.getCode(), code))
			.findFirst()
			.orElse(null);
		return w != null && w.getValue() != null && w.getValue().contains(filter);
	}

}
