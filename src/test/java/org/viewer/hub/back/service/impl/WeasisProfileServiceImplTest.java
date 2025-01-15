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
import org.viewer.hub.back.entity.PreferenceEntity;
import org.viewer.hub.back.entity.ProfileEntity;
import org.viewer.hub.back.model.WeasisProfile;
import org.viewer.hub.back.repository.PreferenceRepository;
import org.viewer.hub.back.repository.ProfileRepository;
import org.viewer.hub.back.service.ProfileService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WeasisProfileServiceImplTest {

	// Repositories
	private final ProfileRepository profileRepositoryMock = Mockito.mock(ProfileRepository.class);

	private final PreferenceRepository preferenceRepositoryMock = Mockito.mock(PreferenceRepository.class);

	// Service
	private ProfileService profileService;

	@BeforeEach
	public void setUp() {
		// Init data
		// Profile
		ProfileEntity profile = new ProfileEntity();
		profile.setId(1L);
		profile.setName("Profile Name");

		// Preference
		PreferenceEntity preferenceEntity = new PreferenceEntity();
		preferenceEntity.setProfile(profile);

		// Mock repositories
		// Profile
		Mockito.when(this.preferenceRepositoryMock.findByTargetName(Mockito.anyString()))
			.thenReturn(Collections.singletonList(preferenceEntity));
		Mockito.when(this.profileRepositoryMock.findAll()).thenReturn(Collections.singletonList(profile));

		// Build mocked service
		this.profileService = new ProfileServiceImpl(this.profileRepositoryMock, this.preferenceRepositoryMock);
	}

	/**
	 * Test readProfiles method
	 * <p>
	 * Expected: retrieve the mocked WeasisProfile
	 */
	@Test
	void readProfilesTest() throws SQLException {
		// Call service
		List<WeasisProfile> weasisProfiles = this.profileService.readProfiles("user");

		// Test results
		assertEquals(1, weasisProfiles.size());
		Mockito.verify(this.preferenceRepositoryMock, Mockito.times(1)).findByTargetName(Mockito.anyString());
		assertEquals(Long.valueOf(1), weasisProfiles.get(0).getId());
		assertEquals("Profile Name", weasisProfiles.get(0).getName());
	}

	/**
	 * Test method getAllProfiles
	 *
	 * Expected: retrieve the mocked WeasisProfile
	 */
	@Test
	void getAllProfilesTest() throws SQLException {
		// Call service
		List<WeasisProfile> profiles = this.profileService.getAllProfiles();

		// Test results
		assertEquals(1, profiles.size());
		Mockito.verify(this.profileRepositoryMock, Mockito.times(1)).findAll();
		assertEquals(Long.valueOf(1), profiles.get(0).getId());
		assertEquals("Profile Name", profiles.get(0).getName());
	}

}
