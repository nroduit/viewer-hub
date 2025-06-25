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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.viewer.hub.back.entity.ModuleEntity;
import org.viewer.hub.back.entity.PreferenceEntity;
import org.viewer.hub.back.entity.ProfileEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.ModuleRepository;
import org.viewer.hub.back.repository.PreferenceRepository;
import org.viewer.hub.back.repository.ProfileRepository;
import org.viewer.hub.back.repository.TargetRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.data.jpa.domain.Specification.where;

@DataJpaTest
@Slf4j
class PreferenceByUserProfileModuleSpecificationTest {

	@Autowired
	private PreferenceRepository preferenceRepository;

	@Autowired
	private ModuleRepository moduleRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private TargetRepository targetRepository;

	@MockBean
	ClientRegistrationRepository clientRegistrationRepository;

	@BeforeEach
	public void init() {
		// Saving entities module and profile
		LOG.info("Saving entity module with name [{}]", "Module");
		ModuleEntity module = new ModuleEntity();
		module.setName("Module");
		this.moduleRepository.saveAndFlush(module);

		LOG.info("Saving entity profile with name [{}]", "Profile");
		ProfileEntity profile = new ProfileEntity();
		profile.setName("Profile");
		this.profileRepository.saveAndFlush(profile);

		LOG.info("Saving entity target with name [{}]", "Target");
		TargetEntity target = new TargetEntity();
		target.setName("Target");
		target.setType(TargetType.USER);
		this.targetRepository.saveAndFlush(target);
	}

	/**
	 * Create a new preference entity
	 * @return the build preference entity
	 */
	private PreferenceEntity buildPreference() {
		PreferenceEntity entity = new PreferenceEntity();
		entity.setTarget(this.targetRepository.findByNameIgnoreCase("Target"));
		entity.setModule(this.moduleRepository.findByName("Module"));
		entity.setProfile(this.profileRepository.findByName("Profile"));
		return entity;
	}

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldFindEntityWithSpecification() {
		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		// Save the entity
		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		entity = this.preferenceRepository.saveAndFlush(entity);

		// Build the specification
		LOG.info("Building specification");
		Specification<PreferenceEntity> preferenceSpecification = new PreferenceByUserProfileModuleSpecification(
				this.targetRepository.findByNameIgnoreCase("Target").getId(),
				this.profileRepository.findByName("Profile").getId(),
				Arrays.asList(this.moduleRepository.findByName("Module").getId(), 2L));

		// Try to retrieve the preference with the specification
		LOG.info("Retrieving entities thanks to the specification built");
		List<PreferenceEntity> entitiesFound = this.preferenceRepository.findAll(where(preferenceSpecification));
		assertNotNull(entitiesFound);
		assertEquals(1, entitiesFound.size());
		PreferenceEntity entityFound = entitiesFound.get(0);
		assertNotNull(entityFound.getProfile());
		assertNotNull(entityFound.getModule());
		assertNotNull(entityFound.getTarget());
		assertEquals("Profile", entityFound.getProfile().getName());
		assertEquals("Module", entityFound.getModule().getName());
		assertEquals("TARGET", entityFound.getTarget().getName());
	}

}