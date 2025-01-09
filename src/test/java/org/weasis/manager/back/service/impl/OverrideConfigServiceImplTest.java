/*
 *
 *  * Copyright (c) 2022-20xx Weasis Team and other contributors.
 *  *
 *  * This program and the accompanying materials are made available under the terms of the Eclipse
 *  * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.OverrideConfigEntity;
import org.weasis.manager.back.entity.PackageVersionEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.OverrideConfigRepository;
import org.weasis.manager.back.repository.TargetRepository;
import org.weasis.manager.back.service.OverrideConfigService;
import org.weasis.manager.front.views.override.component.OverrideConfigFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverrideConfigServiceImplTest {

	private final OverrideConfigRepository overrideConfigRepositoryMock = mock(OverrideConfigRepository.class);

	private final TargetRepository targetRepositoryMock = mock(TargetRepository.class);

	private OverrideConfigService overrideConfigService;

	@BeforeEach
	public void setUp() {
		// Build the mocked override config service
		this.overrideConfigService = new OverrideConfigServiceImpl(this.overrideConfigRepositoryMock,
				this.targetRepositoryMock);
	}

	// @Test
	// TODO W-34: to set back and modify
	// void when_creatingNewConfig_should_callRepositoryToSave() {
	// // Init data
	// OverrideConfigEntity overrideConfig = new OverrideConfigEntity();
	// overrideConfig.setWeasisName("weasis");
	//
	// // Call service
	// overrideConfigService.create(overrideConfig);
	//
	// // Tests results
	// Mockito.verify(overrideConfigRepositoryMock, times(1)).save(any());
	// }

	// TODO W-34: to set back and modify
	// @Test
	// void when_updatingConfig_should_callRepositoryToSave() {
	// // Init data
	//
	// // -- First
	// LaunchConfigEntity firstLaunchConfigEntity = new LaunchConfigEntity();
	// firstLaunchConfigEntity.setName("firstLaunchConfig");
	// PackageVersionEntity firstPackageVersionEntity = new PackageVersionEntity();
	// firstPackageVersionEntity.setVersionNumber("1.0.0");
	// firstPackageVersionEntity.setQualifier("-FIRST");
	// TargetEntity firstGroup = new TargetEntity();
	// firstGroup.setType(TargetType.USER_GROUP);
	// firstGroup.setName("firstGroup");
	// OverrideConfigEntity overrideConfig = new OverrideConfigEntity();
	// overrideConfig.setLaunchConfig(firstLaunchConfigEntity);
	// overrideConfig.setPackageVersion(firstPackageVersionEntity);
	// overrideConfig.setTarget(firstGroup);
	// overrideConfig.setWeasisName("weasis");
	//
	// // Call service
	// overrideConfigService.update(overrideConfig);
	//
	// // Tests results
	// Mockito.verify(overrideConfigRepositoryMock, times(1)).save(any());
	// }

	@Test
	void when_retrievingProperties_should_callRepositoryToFind() {
		// Call service
		this.overrideConfigService.retrieveProperties(1L, 1L, 1L);

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1))
			.findByPackageVersionIdAndLaunchConfigIdAndTargetId(anyLong(), anyLong(), anyLong());
	}

	@Test
	void when_retrievingDefaultGroupProperties_should_callRepositoryToFind() {
		// Call service
		this.overrideConfigService.retrieveDefaultGroupProperties(1L, 1L);

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1))
			.findByPackageVersionIdAndLaunchConfigIdAndTargetName(anyLong(), anyLong(),
					Mockito.eq(TargetType.DEFAULT.getCode()));
	}

	@Test
	void when_checkingExistOverrideConfig_should_callCallCorrectRepositoryMethod() {
		// Init data
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setId(1L);
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setId(1L);
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setId(1L);

		// Call service
		this.overrideConfigService.existOverrideConfigWithVersionConfigTarget(packageVersionEntity, launchConfigEntity,
				targetEntity);

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1))
			.existsByPackageVersionIdAndLaunchConfigIdAndTargetId(anyLong(), anyLong(), anyLong());
	}

	@Test
	void when_saving_should_callCallCorrectRepositoryMethod() {
		// Init data
		Set<OverrideConfigEntity> overrideConfigEntities = new HashSet<>();

		// Call service
		this.overrideConfigService.saveAll(overrideConfigEntities);

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1)).saveAll(any());
	}

	@Test
	void when_retrievingDistinctPackageVersionEntities_should_callCallCorrectRepositoryMethodAndCollectPackage() {

		// Init data
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setId(1L);

		OverrideConfigEntity overrideConfigEntity = new OverrideConfigEntity();
		overrideConfigEntity.setPackageVersion(packageVersionEntity);

		// Mock
		when(this.overrideConfigRepositoryMock.findAll()).thenReturn(List.of(overrideConfigEntity));

		// Call service
		Set<PackageVersionEntity> packageVersionEntities = this.overrideConfigService
			.retrieveDistinctPackageVersionEntities();

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1)).findAll();
		assertEquals(1, packageVersionEntities.size());
		assertEquals(1L, packageVersionEntities.stream().findFirst().get().getId());
	}

	@Test
	void when_retrievingDistinctLaunchConfigEntities_should_callCallCorrectRepositoryMethodAndCollectLaunchConfig() {

		// Init data
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setId(1L);

		OverrideConfigEntity overrideConfigEntity = new OverrideConfigEntity();
		overrideConfigEntity.setLaunchConfig(launchConfigEntity);

		// Mock
		when(this.overrideConfigRepositoryMock.findAll()).thenReturn(List.of(overrideConfigEntity));

		// Call service
		Set<LaunchConfigEntity> launchConfigEntities = this.overrideConfigService
			.retrieveDistinctLaunchConfigEntities();

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1)).findAll();
		assertEquals(1, launchConfigEntities.size());
		assertEquals(1L, launchConfigEntities.stream().findFirst().get().getId());
	}

	@Test
	void when_retrievingDistinctGroupEntities_should_callCallCorrectRepositoryMethodAndCollectGroups() {

		// Init data
		TargetEntity targetUserGroup = new TargetEntity();
		targetUserGroup.setType(TargetType.USER_GROUP);
		TargetEntity targetHostGroup = new TargetEntity();
		targetHostGroup.setType(TargetType.HOST_GROUP);
		TargetEntity targetDefault = new TargetEntity();
		targetDefault.setType(TargetType.DEFAULT);

		// Mock
		when(this.targetRepositoryMock.findAll()).thenReturn(List.of(targetUserGroup, targetHostGroup, targetDefault));

		// Call service
		Set<TargetEntity> groupEntities = this.overrideConfigService.retrieveDistinctGroupEntities();

		// Tests results
		Mockito.verify(this.targetRepositoryMock, times(1)).findAll();
		assertEquals(3, groupEntities.size());
	}

	@Test
	void when_retrieveOverrideConfigsPageable_without_filter_should_callCorrectRepositoryMethod() {
		OverrideConfigFilter overrideConfigFilter = new OverrideConfigFilter();

		// Call service
		this.overrideConfigService.retrieveOverrideConfigsPageable(overrideConfigFilter, mock(Pageable.class));

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1)).findAll(any(Pageable.class));
	}

	@Test
	void when_retrieveOverrideConfigsPageable_with_filter_should_callCorrectRepositoryMethod() {
		OverrideConfigFilter overrideConfigFilter = new OverrideConfigFilter();
		overrideConfigFilter.setLaunchConfig("launchConfig");

		// Call service
		this.overrideConfigService.retrieveOverrideConfigsPageable(overrideConfigFilter, mock(Pageable.class));

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1))
			.findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	void when_countingOverrideConfigs_without_filter_should_callCorrectRepositoryMethod() {
		OverrideConfigFilter overrideConfigFilter = new OverrideConfigFilter();

		// Call service
		this.overrideConfigService.countOverrideConfigs(overrideConfigFilter);

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1)).count();
	}

	@Test
	void when_countingOverrideConfigs_with_filter_should_callCorrectRepositoryMethod() {
		OverrideConfigFilter overrideConfigFilter = new OverrideConfigFilter();
		overrideConfigFilter.setLaunchConfig("launchConfig");

		// Call service
		this.overrideConfigService.countOverrideConfigs(overrideConfigFilter);

		// Tests results
		Mockito.verify(this.overrideConfigRepositoryMock, times(1)).count(any(Specification.class));
	}

	@Test
	void when_checkingOverrideConfigAlreadyExists_withParameterNull_should_returnFalse() {
		// Call service
		boolean toTest = this.overrideConfigService.doesOverrideConfigAlreadyExists(new OverrideConfigEntity());

		// Tests results
		assertFalse(toTest);
	}

	@Test
	void when_checkingOverrideConfigAlreadyExists_withValidParameters_should_callCorrectRepositoryMethod() {

		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("firstLaunchConfig");
		launchConfigEntity.setId(1L);
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setVersionNumber("1.0.0");
		packageVersionEntity.setQualifier("-FIRST");
		packageVersionEntity.setId(1L);
		TargetEntity group = new TargetEntity();
		group.setType(TargetType.USER_GROUP);
		group.setName("group");
		group.setId(1L);
		OverrideConfigEntity overrideConfig = new OverrideConfigEntity();
		overrideConfig.setLaunchConfig(launchConfigEntity);
		overrideConfig.setPackageVersion(packageVersionEntity);
		overrideConfig.setTarget(group);

		// Mock
		when(this.overrideConfigRepositoryMock.existsByPackageVersionIdAndLaunchConfigIdAndTargetId(anyLong(),
				anyLong(), anyLong()))
			.thenReturn(true);

		// Call service
		boolean toTest = this.overrideConfigService.doesOverrideConfigAlreadyExists(overrideConfig);

		// Tests results
		assertTrue(toTest);
	}

}
