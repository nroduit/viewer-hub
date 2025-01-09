package org.weasis.manager.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.weasis.manager.back.entity.PreferenceEntity;
import org.weasis.manager.back.entity.ProfileEntity;
import org.weasis.manager.back.model.WeasisProfile;
import org.weasis.manager.back.repository.PreferenceRepository;
import org.weasis.manager.back.repository.ProfileRepository;
import org.weasis.manager.back.service.ProfileService;

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
