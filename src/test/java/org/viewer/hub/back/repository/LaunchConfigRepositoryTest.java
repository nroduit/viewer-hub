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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.viewer.hub.back.entity.LaunchConfigEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
public class LaunchConfigRepositoryTest {

	@Autowired
	private LaunchConfigRepository repository;

	@MockBean
	ClientRegistrationRepository clientRegistrationRepository;

	/**
	 * Test save and find by id.
	 */
	@Test
	public void shouldSaveAndFindARecord() {
		// Create an entity to save
		LaunchConfigEntity entity = new LaunchConfigEntity();
		entity.setName("Name");

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Test Save
		assertEquals("Name", entity.getName());
		assertNotNull(entity.getId());
		LOG.info("Entity with name [{}] and id [{}] saved", entity.getName(), entity.getId());

		// Find By Id
		Optional<LaunchConfigEntity> foundByIdOpt = this.repository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity found with name [{}] and id [{}]", foundByIdOpt.get().getName(), foundByIdOpt.get().getId());
		assertEquals(entity.getId(), foundByIdOpt.get().getId());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Create an entity to save
		LaunchConfigEntity entity = new LaunchConfigEntity();
		entity.setName("Name");

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.saveAndFlush(entity);

		// Find all
		List<LaunchConfigEntity> all = this.repository.findAll();

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

		String initialText = "InitialText";
		String modifiedText = "ModifiedText";

		// Create an entity to save
		LaunchConfigEntity entity = new LaunchConfigEntity();
		entity.setName(initialText);

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.save(entity);
		LOG.info("Id of the entity with name [{}]", entity.getId());

		// Test Save
		assertNotNull(entity);
		assertEquals(initialText, entity.getName());

		// Modify the record
		entity.setName(modifiedText);
		LOG.info("Modify entity name [{}] to [{}]", initialText, modifiedText);
		LaunchConfigEntity entityModified = this.repository.save(entity);

		// Test Modify
		assertNotNull(entityModified);
		assertEquals(entity.getId(), entityModified.getId());
		assertEquals(modifiedText, entityModified.getName());
		LOG.info("Name of the entity with id [{}]: [{}]", entityModified.getId(), entityModified.getName());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Create an entity to save
		LaunchConfigEntity entity = new LaunchConfigEntity();
		entity.setName("Name");

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Retrieve the entity
		Optional<LaunchConfigEntity> foundByIdOpt = this.repository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());

		// Delete the entity
		entity = foundByIdOpt.get();
		Long id = entity.getId();
		LOG.info("Deleting entity with id [{}]", id);
		this.repository.delete(entity);

		// Test Delete
		foundByIdOpt = this.repository.findById(id);
		LOG.info("Is deleted entity with id [{}] present: [{}]", id, foundByIdOpt.isPresent());
		assertFalse(foundByIdOpt.isPresent());
	}

	/**
	 * Test findByName method
	 */
	@Test
	void shouldFindConfigByName() {
		// Create entity
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("Name");

		LOG.info("Saving entity with name [{}]", launchConfigEntity.getName());
		launchConfigEntity = this.repository.saveAndFlush(launchConfigEntity);

		// Retrieve entity
		LaunchConfigEntity entity = this.repository.findByName("Name");

		// Test result
		assertEquals(launchConfigEntity.getId(), entity.getId());
		assertEquals(launchConfigEntity.getName(), entity.getName());
	}

	/**
	 * Test findByNameIn method
	 */
	@Test
	void shouldFindLaunchConfigByNames() {
		// Create entity
		LaunchConfigEntity launchConfigEntityFirst = new LaunchConfigEntity();
		launchConfigEntityFirst.setName("Name First");
		LOG.info("Saving entity with name [{}]", launchConfigEntityFirst.getName());
		launchConfigEntityFirst = this.repository.saveAndFlush(launchConfigEntityFirst);
		// Create entity
		LaunchConfigEntity launchConfigEntitySecond = new LaunchConfigEntity();
		launchConfigEntitySecond.setName("Name Second");
		LOG.info("Saving entity with name [{}]", launchConfigEntitySecond.getName());
		launchConfigEntitySecond = this.repository.saveAndFlush(launchConfigEntitySecond);

		// Retrieve entity
		List<LaunchConfigEntity> entities = this.repository
			.findByNameIn(Arrays.asList(launchConfigEntityFirst.getName(), launchConfigEntitySecond.getName()));

		// Test result
		assertEquals(2, entities.size());
		assertEquals("Name First", entities.get(0).getName());
		assertEquals("Name Second", entities.get(1).getName());
	}

}