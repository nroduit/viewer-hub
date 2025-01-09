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
import org.springframework.data.jpa.domain.Specification;
import org.weasis.manager.back.entity.ModuleEntity;
import org.weasis.manager.back.entity.PreferenceEntity;
import org.weasis.manager.back.entity.ProfileEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.OperationType;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.ModuleRepository;
import org.weasis.manager.back.repository.PreferenceRepository;
import org.weasis.manager.back.repository.ProfileRepository;
import org.weasis.manager.back.repository.TargetRepository;
import org.weasis.manager.back.service.ApplicationPreferenceService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ApplicationPreferenceServiceImplTest {

	private final PreferenceRepository preferenceRepositoryMock = Mockito.mock(PreferenceRepository.class);

	private final ModuleRepository moduleRepositoryMock = Mockito.mock(ModuleRepository.class);

	private final ProfileRepository profileRepositoryMock = Mockito.mock(ProfileRepository.class);

	private final TargetRepository targetRepositoryMock = Mockito.mock(TargetRepository.class);

	ApplicationPreferenceService applicationPreferenceService;

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
		preferenceEntity.setId(1L);
		preferenceEntity.setModule(moduleEntity);
		preferenceEntity.setContent("Content");
		// Target
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("targetEntity");
		targetEntity.setType(TargetType.USER);
		targetEntity.setId(1L);

		// Mock repositories
		// Module
		Mockito.when(this.moduleRepositoryMock.existsByName(Mockito.anyString())).thenReturn(true);
		Mockito.when(this.moduleRepositoryMock.findByName(Mockito.anyString())).thenReturn(moduleEntity);
		Mockito.when(this.moduleRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(moduleEntity));
		// Profile
		Mockito.when(this.profileRepositoryMock.findByName(Mockito.anyString())).thenReturn(profile);
		Mockito.when(this.profileRepositoryMock.existsByName(Mockito.anyString())).thenReturn(true);
		Mockito.when(this.profileRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(profile));
		// Preference
		Mockito.when(this.preferenceRepositoryMock.findAll(Mockito.any(Specification.class)))
			.thenReturn(Collections.singletonList(preferenceEntity));
		Mockito.when(this.preferenceRepositoryMock.save(Mockito.any())).thenReturn(null);
		Mockito.when(this.preferenceRepositoryMock.findOne(Mockito.any(Specification.class)))
			.thenReturn(Optional.of(preferenceEntity));
		Mockito.when(this.preferenceRepositoryMock.existsByTargetName(Mockito.anyString())).thenReturn(true);
		// Target
		Mockito.when(this.targetRepositoryMock.findOptionalByNameIgnoreCase(Mockito.anyString()))
			.thenReturn(Optional.of(targetEntity));
		Mockito.when(this.targetRepositoryMock.findByNameIgnoreCase(Mockito.anyString())).thenReturn(targetEntity);

		// Build the mocked target service
		this.applicationPreferenceService = new ApplicationPreferenceServiceImpl(this.preferenceRepositoryMock,
				this.profileRepositoryMock, this.moduleRepositoryMock, this.targetRepositoryMock);
	}

	/**
	 * Test method createWeasisPreferences
	 * <p>
	 * Expected: - check that the desired methods have been called (exist / find / save)
	 */
	@Test
	void createWeasisPreferencesTest() throws SQLException {

		// Call service
		this.applicationPreferenceService.createWeasisPreferences("user", "profileName", "moduleName", "preferences");

		// Test results
		Mockito.verify(this.moduleRepositoryMock, Mockito.times(1)).existsByName(Mockito.anyString());
		Mockito.verify(this.profileRepositoryMock, Mockito.times(1)).existsByName(Mockito.anyString());
		Mockito.verify(this.profileRepositoryMock, Mockito.times(1)).findByName(Mockito.anyString());
		Mockito.verify(this.moduleRepositoryMock, Mockito.times(1)).findByName(Mockito.anyString());
		Mockito.verify(this.preferenceRepositoryMock, Mockito.times(1)).save(Mockito.any());
	}

	/**
	 * Test method readWeasisPreferences
	 * <p>
	 * Expected: - check that the desired methods have been called
	 */
	@Test
	void readWeasisPreferencesTest() throws SQLException {
		// Call service
		String toTest = this.applicationPreferenceService.readWeasisPreferences("user", "profileName", "moduleName",
				true);

		// Test results
		assertEquals("Content", toTest);
	}

	/**
	 * Test method updateWeasisPreferences
	 * <p>
	 * Expected: - check that the desired methods have been called
	 */
	@Test
	void updateWeasisPreferencesTest() throws SQLException {
		// Call service
		OperationType toTest = this.applicationPreferenceService.updateWeasisPreferences("user", "profileName",
				"moduleName", "preferences");

		// Test results
		assertEquals(OperationType.UPDATE, toTest);
	}

}
