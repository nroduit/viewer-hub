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

package org.viewer.hub.back.repository.specification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntityPK;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.repository.OverrideConfigRepository;
import org.viewer.hub.back.repository.PackageVersionRepository;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.front.views.override.component.OverrideConfigFilter;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class OverrideConfigSpecificationTest {

	@Autowired
	private PackageVersionRepository packageVersionRepository;

	@Autowired
	private LaunchConfigRepository launchConfigRepository;

	@Autowired
	private TargetRepository targetRepository;

	@Autowired
	private OverrideConfigRepository overrideConfigRepository;

	@BeforeEach
	void setUp() {

		// -- First
		LaunchConfigEntity firstLaunchConfigEntity = new LaunchConfigEntity();
		firstLaunchConfigEntity.setName("firstLaunchConfig");
		PackageVersionEntity firstPackageVersionEntity = new PackageVersionEntity();
		firstPackageVersionEntity.setVersionNumber("1.0.0");
		firstPackageVersionEntity.setQualifier("-FIRST");
		TargetEntity firstGroup = new TargetEntity();
		firstGroup.setType(TargetType.USER_GROUP);
		firstGroup.setName("firstGroup");

		// -- Second
		LaunchConfigEntity secondLaunchConfigEntity = new LaunchConfigEntity();
		secondLaunchConfigEntity.setName("secondLaunchConfig");
		PackageVersionEntity secondPackageVersionEntity = new PackageVersionEntity();
		secondPackageVersionEntity.setVersionNumber("2.0.0");
		secondPackageVersionEntity.setQualifier("-SECOND");
		TargetEntity secondGroup = new TargetEntity();
		secondGroup.setType(TargetType.USER_GROUP);
		secondGroup.setName("secondGroup");

		// Save in DB
		firstPackageVersionEntity = this.packageVersionRepository.saveAndFlush(firstPackageVersionEntity);
		secondPackageVersionEntity = this.packageVersionRepository.saveAndFlush(secondPackageVersionEntity);

		firstLaunchConfigEntity = this.launchConfigRepository.saveAndFlush(firstLaunchConfigEntity);
		secondLaunchConfigEntity = this.launchConfigRepository.saveAndFlush(secondLaunchConfigEntity);

		firstGroup = this.targetRepository.saveAndFlush(firstGroup);
		secondGroup = this.targetRepository.saveAndFlush(secondGroup);

		// OverrideConfigEntities
		OverrideConfigEntity firstOverrideConfig = new OverrideConfigEntity();
		OverrideConfigEntityPK firstOverrideConfigEntityPk = new OverrideConfigEntityPK();
		firstOverrideConfigEntityPk.setLaunchConfigId(firstLaunchConfigEntity.getId());
		firstOverrideConfigEntityPk.setPackageVersionId(firstPackageVersionEntity.getId());
		firstOverrideConfigEntityPk.setTargetId(firstGroup.getId());
		firstOverrideConfig.setOverrideConfigEntityPK(firstOverrideConfigEntityPk);
		firstOverrideConfig.setLaunchConfig(firstLaunchConfigEntity);
		firstOverrideConfig.setPackageVersion(firstPackageVersionEntity);
		firstOverrideConfig.setTarget(firstGroup);

		OverrideConfigEntity secondOverrideConfig = new OverrideConfigEntity();
		OverrideConfigEntityPK secondOverrideConfigEntityPk = new OverrideConfigEntityPK();
		secondOverrideConfigEntityPk.setLaunchConfigId(secondLaunchConfigEntity.getId());
		secondOverrideConfigEntityPk.setPackageVersionId(secondPackageVersionEntity.getId());
		secondOverrideConfigEntityPk.setTargetId(secondGroup.getId());
		secondOverrideConfig.setOverrideConfigEntityPK(secondOverrideConfigEntityPk);
		secondOverrideConfig.setLaunchConfig(secondLaunchConfigEntity);
		secondOverrideConfig.setPackageVersion(secondPackageVersionEntity);
		secondOverrideConfig.setTarget(secondGroup);

		// Save in DB
		Arrays.asList(firstOverrideConfig, secondOverrideConfig)
			.forEach(o -> this.overrideConfigRepository.saveAndFlush(o));
	}

	@Test
	void shouldFilterByPackageVersion() {

		// Filter on number
		OverrideConfigFilter packageVersionFilter = new OverrideConfigFilter();
		packageVersionFilter.setPackageVersion("1");
		Specification<OverrideConfigEntity> overrideConfigEntitySpecification = new OverrideConfigSpecification(
				packageVersionFilter);

		// Call service
		List<OverrideConfigEntity> overrideConfigEntities = this.overrideConfigRepository
			.findAll(overrideConfigEntitySpecification);

		// Test results
		assertNotNull(overrideConfigEntities);
		assertFalse(overrideConfigEntities.isEmpty());
		assertEquals(1, overrideConfigEntities.size());
		assertEquals("1.0.0", overrideConfigEntities.get(0).getPackageVersion().getVersionNumber());

		// Filter on qualifier
		packageVersionFilter = new OverrideConfigFilter();
		packageVersionFilter.setPackageVersion("D");
		overrideConfigEntitySpecification = new OverrideConfigSpecification(packageVersionFilter);

		// Call service
		overrideConfigEntities = this.overrideConfigRepository.findAll(overrideConfigEntitySpecification);

		// Test results
		assertNotNull(overrideConfigEntities);
		assertFalse(overrideConfigEntities.isEmpty());
		assertEquals(1, overrideConfigEntities.size());
		assertEquals("-SECOND", overrideConfigEntities.get(0).getPackageVersion().getQualifier());
	}

	@Test
	void shouldFilterByLaunchConfig() {
		// Filter on launch config
		OverrideConfigFilter launchConfigFilter = new OverrideConfigFilter();
		launchConfigFilter.setLaunchConfig("first");
		Specification<OverrideConfigEntity> overrideConfigEntitySpecification = new OverrideConfigSpecification(
				launchConfigFilter);

		// Call service
		List<OverrideConfigEntity> overrideConfigEntities = this.overrideConfigRepository
			.findAll(overrideConfigEntitySpecification);

		// Test results
		assertNotNull(overrideConfigEntities);
		assertFalse(overrideConfigEntities.isEmpty());
		assertEquals(1, overrideConfigEntities.size());
		assertEquals("firstLaunchConfig", overrideConfigEntities.get(0).getLaunchConfig().getName());
	}

	@Test
	void shouldFilterByGroup() {
		// Filter on group
		OverrideConfigFilter groupFilter = new OverrideConfigFilter();
		groupFilter.setGroup("second");
		Specification<OverrideConfigEntity> overrideConfigEntitySpecification = new OverrideConfigSpecification(
				groupFilter);

		// Call service
		List<OverrideConfigEntity> overrideConfigEntities = this.overrideConfigRepository
			.findAll(overrideConfigEntitySpecification);

		// Test results
		assertNotNull(overrideConfigEntities);
		assertFalse(overrideConfigEntities.isEmpty());
		assertEquals(1, overrideConfigEntities.size());
		assertEquals("SECONDGROUP", overrideConfigEntities.get(0).getTarget().getName());
	}

}
