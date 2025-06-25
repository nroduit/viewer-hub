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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.viewer.hub.back.entity.ModuleEntity;
import org.viewer.hub.back.entity.PreferenceEntity;
import org.viewer.hub.back.entity.ProfileEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
class PreferenceRepositoryTest {

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
	void shouldSaveAndFindARecord() {
		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		// Save the entity
		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		entity = this.preferenceRepository.save(entity);

		// Test Save
		assertEquals("TARGET", entity.getTarget().getName());
		assertNotNull(entity.getId());
		LOG.info("Entity Preference with user [{}] and id [{}] saved", entity.getTarget().getName(), entity.getId());

		// Find By Id
		Optional<PreferenceEntity> foundByIdOpt = this.preferenceRepository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity Preference found with user [{}] and id [{}]", foundByIdOpt.get().getTarget().getName(),
				foundByIdOpt.get().getId());
		assertEquals(entity.getId(), foundByIdOpt.get().getId());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		this.preferenceRepository.saveAndFlush(entity);

		// Find all
		List<PreferenceEntity> all = this.preferenceRepository.findAll();

		// Test find all
		assertNotNull(all);
		assertTrue(all.size() > 0);
		assertEquals(1, all.size());
		LOG.info("Number of entities found [{}]", all.size());
	}

	/**
	 * Test modification of a record.
	 */
	@Test
	void shouldModifyRecord() {

		String initialText = "Target";
		String modifiedText = "ModifiedText";

		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		// Save the entity
		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		entity = this.preferenceRepository.save(entity);
		LOG.info("Id of the entity Preference with user [{}]", entity.getId());

		// Test Save
		assertNotNull(entity);
		assertEquals("TARGET", entity.getTarget().getName());

		// Modify the record
		entity.getTarget().setName(modifiedText);
		LOG.info("Modify entity Preference user [{}] to [{}]", initialText, modifiedText);
		PreferenceEntity entityModified = this.preferenceRepository.save(entity);

		// Test Modify
		assertNotNull(entityModified);
		assertEquals(entity.getId(), entityModified.getId());
		assertEquals("MODIFIEDTEXT", entityModified.getTarget().getName());
		LOG.info("User of the entity Preference with id [{}]: [{}]", entityModified.getId(),
				entityModified.getTarget().getName());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		// Save the entity
		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		entity = this.preferenceRepository.save(entity);

		// Retrieve the entity
		Optional<PreferenceEntity> foundByIdOpt = this.preferenceRepository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());

		// Delete the entity
		entity = foundByIdOpt.get();
		Long id = entity.getId();
		LOG.info("Deleting entity with id [{}]", id);
		this.preferenceRepository.delete(entity);

		// Test Delete
		foundByIdOpt = this.preferenceRepository.findById(id);
		LOG.info("Is deleted entity with id [{}] present: [{}]", id, foundByIdOpt.isPresent());
		assertFalse(foundByIdOpt.isPresent());
	}

	/**
	 * Test the existsByWeaPrefCUser method
	 */
	@Test
	void shouldCheckIfUserExist() {

		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		// Save the entity
		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		entity = this.preferenceRepository.saveAndFlush(entity);

		// Check if user exists
		assertTrue(this.preferenceRepository.existsByTargetName("TARGET"));

		// Delete the user
		this.preferenceRepository.delete(entity);
		this.preferenceRepository.flush();

		// Check if user exists
		assertFalse(this.preferenceRepository.existsByTargetName("TARGET"));
	}

	@Test
	void shouldFindPreferenceByTargetName() {
		// Create an entity to save
		PreferenceEntity entity = this.buildPreference();

		// Save the entity
		LOG.info("Saving entity Preference with user [{}]", entity.getTarget().getName());
		entity = this.preferenceRepository.saveAndFlush(entity);

		// Check if user exists
		assertEquals(entity.getTarget().getName(),
				this.preferenceRepository.findByTargetName("TARGET").get(0).getTarget().getName());
	}

}