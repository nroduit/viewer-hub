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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.viewer.hub.back.entity.ModuleEntity;
import org.viewer.hub.back.entity.PreferenceEntity;
import org.viewer.hub.back.entity.ProfileEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.model.WeasisModule;
import org.viewer.hub.back.repository.ModuleRepository;
import org.viewer.hub.back.repository.PreferenceRepository;
import org.viewer.hub.back.repository.ProfileRepository;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.back.service.ModuleService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WeasisModuleServiceImplTest {

	private final PreferenceRepository preferenceRepositoryMock = Mockito.mock(PreferenceRepository.class);

	private final ModuleRepository moduleRepositoryMock = Mockito.mock(ModuleRepository.class);

	private final ProfileRepository profileRepositoryMock = Mockito.mock(ProfileRepository.class);

	private final TargetRepository targetRepositoryMock = Mockito.mock(TargetRepository.class);

	private ModuleService moduleService;

	@BeforeEach
	public void setUp() {

		// Init data
		// Module
		ModuleEntity moduleEntity = new ModuleEntity();
		moduleEntity.setId(1L);
		moduleEntity.setName("ModuleEntity Name");
		// Profile
		ProfileEntity profile = new ProfileEntity();
		profile.setId(1L);
		profile.setName("Profile Name");
		// Preference
		PreferenceEntity preferenceEntity = new PreferenceEntity();
		preferenceEntity.setModule(moduleEntity);
		// Target
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setId(1L);
		targetEntity.setName("target");
		targetEntity.setType(TargetType.USER);

		// Mock repositories
		// Module
		Mockito.when(this.moduleRepositoryMock.findAll()).thenReturn(Collections.singletonList(moduleEntity));
		// Profile
		Mockito.when(this.profileRepositoryMock.findByName(Mockito.anyString())).thenReturn(profile);
		// Preference
		Mockito.when(this.preferenceRepositoryMock.findAll(Mockito.any(Specification.class)))
			.thenReturn(Collections.singletonList(preferenceEntity));
		// Target
		Mockito.when(this.targetRepositoryMock.findByNameIgnoreCase(Mockito.any(String.class)))
			.thenReturn(targetEntity);

		// Create service
		this.moduleService = new ModuleServiceImpl(this.preferenceRepositoryMock, this.moduleRepositoryMock,
				this.profileRepositoryMock, this.targetRepositoryMock);
	}

	/**
	 * Test to retrieve all modules
	 */
	@Test
	void readWeasisModulesTest() throws SQLException {
		// Call service
		List<WeasisModule> weasisModules = this.moduleService.readWeasisModules("user", "profileName");

		// Test results
		assertEquals(1, weasisModules.size());
		Mockito.verify(this.preferenceRepositoryMock, Mockito.times(1)).findAll(Mockito.any(Specification.class));
		Mockito.verify(this.profileRepositoryMock, Mockito.times(1)).findByName(Mockito.anyString());
		Mockito.verify(this.moduleRepositoryMock, Mockito.times(1)).findAll();
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findByNameIgnoreCase(Mockito.anyString());
		assertEquals(Long.valueOf(1), weasisModules.get(0).getId());
		assertEquals("ModuleEntity Name", weasisModules.get(0).getName());
	}

}
