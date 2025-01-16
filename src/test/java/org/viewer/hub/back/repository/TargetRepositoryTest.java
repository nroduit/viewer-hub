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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
class TargetRepositoryTest {

	@Autowired
	private TargetRepository repository;

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldSaveAndFindARecord() {
		// Create an entity to save
		TargetEntity entity = new TargetEntity();
		entity.setName("TargetName");
		entity.setType(TargetType.USER);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Test Save
		assertEquals("TARGETNAME", entity.getName());
		assertEquals(TargetType.USER, entity.getType());
		assertNotNull(entity.getId());
		LOG.info("Entity Target with name [{}] and id [{}] saved", entity.getName(), entity.getId());

		// Find By Id
		Optional<TargetEntity> foundByIdOpt = this.repository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity Target found with name [{}] and id [{}]", foundByIdOpt.get().getName(),
				foundByIdOpt.get().getId());
		assertEquals(entity.getId(), foundByIdOpt.get().getId());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Create an entity to save
		TargetEntity entity = new TargetEntity();
		entity.setName("TargetName");
		entity.setType(TargetType.HOST);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entity.getName());
		this.repository.saveAndFlush(entity);

		// Find all
		List<TargetEntity> all = this.repository.findAll();

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
		TargetEntity entity = new TargetEntity();
		entity.setName(initialText);
		entity.setType(TargetType.USER_GROUP);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entity.getName());
		entity = this.repository.save(entity);
		LOG.info("Id of the entity Target with name [{}]", entity.getId());

		// Test Save
		assertNotNull(entity);
		assertEquals(initialText.toUpperCase(), entity.getName());

		// Modify the record
		entity.setName(modifiedText);
		LOG.info("Modify entity Target name [{}] to [{}]", initialText, modifiedText);
		TargetEntity entityModified = this.repository.save(entity);

		// Test Modify
		assertNotNull(entityModified);
		assertEquals(entity.getId(), entityModified.getId());
		assertEquals(modifiedText.toUpperCase(), entityModified.getName());
		LOG.info("Name of the entity Target with id [{}]: [{}]", entityModified.getId(), entityModified.getName());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Create an entity to save
		TargetEntity entity = new TargetEntity();
		String targetName = "TargetName";
		entity.setName(targetName);
		entity.setType(TargetType.HOST_GROUP);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Retrieve the entity
		Optional<TargetEntity> foundByIdOpt = this.repository.findById(entity.getId());

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
	 * Test findByType method
	 */
	@Test
	void shouldFindTargetsByType() {
		// Create entities to save
		TargetEntity entityHostGroup = new TargetEntity();
		entityHostGroup.setName("HOST_GROUP");
		entityHostGroup.setType(TargetType.HOST_GROUP);
		TargetEntity entityHost = new TargetEntity();
		entityHost.setName("HOST");
		entityHost.setType(TargetType.HOST);

		// Save the entities
		LOG.info("Saving entity Target with name [{}]", entityHostGroup.getName());
		entityHostGroup = this.repository.saveAndFlush(entityHostGroup);
		LOG.info("Saving entity Target with name [{}]", entityHost.getName());
		entityHost = this.repository.saveAndFlush(entityHost);

		// Retrieve entities
		List<TargetEntity> byTypeHost = this.repository.findByType(TargetType.HOST);
		List<TargetEntity> byTypeHostGroup = this.repository.findByType(TargetType.HOST_GROUP);

		// Test results
		assertEquals(1, byTypeHost.size());
		assertEquals(1, byTypeHostGroup.size());
		assertEquals(entityHost.getId(), byTypeHost.get(0).getId());
		assertEquals(entityHostGroup.getId(), byTypeHostGroup.get(0).getId());
	}

	/**
	 * Test findByName method
	 */
	@Test
	void shouldFindTargetByName() {
		// Create entity
		TargetEntity entityHost = new TargetEntity();
		entityHost.setName("HOST");
		entityHost.setType(TargetType.HOST);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entityHost.getName());
		entityHost = this.repository.saveAndFlush(entityHost);

		// Retrieve entity
		TargetEntity entity = this.repository.findByNameIgnoreCase("HOST");

		// Test result
		assertEquals(entityHost.getId(), entity.getId());
		assertEquals(entityHost.getName(), entity.getName());
		assertEquals(entityHost.getType(), entity.getType());
	}

	/**
	 * Test method existsByName
	 */
	@Test
	void shouldCheckExistByName() {
		// Create entity
		TargetEntity entity = new TargetEntity();
		entity.setName("Test");
		entity.setType(TargetType.USER);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entity.getName());
		this.repository.saveAndFlush(entity);

		// Call service
		boolean exist = this.repository.existsByNameIgnoreCase(entity.getName());

		// Test results
		assertTrue(exist);
	}

	/**
	 * Test method ExistsByNameAndType
	 */
	@Test
	void shouldCheckExistsByNameAndType() {
		// Create entity
		TargetEntity entity = new TargetEntity();
		entity.setName("Test");
		entity.setType(TargetType.USER);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entity.getName());
		this.repository.saveAndFlush(entity);

		// Call service: case right parameters
		boolean exist = this.repository.existsByNameIgnoreCaseAndType(entity.getName(), TargetType.USER);

		// Test results
		assertTrue(exist);

		// Call service: case wrong name
		exist = this.repository.existsByNameIgnoreCaseAndType("XXX", TargetType.USER);

		// Test results
		assertFalse(exist);

		// Call service: case wrong target type
		exist = this.repository.existsByNameIgnoreCaseAndType("Test", TargetType.HOST);

		// Test results
		assertFalse(exist);
	}

	/**
	 * Test method FindByNameAndType
	 */
	@Test
	void shouldFindByNameAndType() {
		// Create entity
		TargetEntity entityHost = new TargetEntity();
		entityHost.setName("HOST");
		entityHost.setType(TargetType.HOST);

		// Save the entity
		LOG.info("Saving entity Target with name [{}]", entityHost.getName());
		entityHost = this.repository.saveAndFlush(entityHost);

		// Retrieve entity: case right parameters
		TargetEntity entity = this.repository.findByNameIgnoreCaseAndType("HOST", TargetType.HOST);

		// Test result
		assertEquals(entityHost.getId(), entity.getId());
		assertEquals(entityHost.getName(), entity.getName());
		assertEquals(entityHost.getType(), entity.getType());

		// Retrieve entity: case wrong parameters: wrong type
		entity = this.repository.findByNameIgnoreCaseAndType("HOST", TargetType.USER);

		// Test result
		assertNull(entity);
	}

	/**
	 * Test findByNameIn method
	 */
	@Test
	void shouldFindTargetsByNames() {
		// Create entity
		TargetEntity targetEntityFirst = new TargetEntity();
		targetEntityFirst.setName("Name First");
		targetEntityFirst.setType(TargetType.HOST);
		LOG.info("Saving entity with name [{}]", targetEntityFirst.getName());
		targetEntityFirst = this.repository.saveAndFlush(targetEntityFirst);
		// Create entity
		TargetEntity targetEntitySecond = new TargetEntity();
		targetEntitySecond.setName("Name Second");
		targetEntitySecond.setType(TargetType.HOST);
		LOG.info("Saving entity with name [{}]", targetEntitySecond.getName());
		targetEntitySecond = this.repository.saveAndFlush(targetEntitySecond);

		// Retrieve entity
		List<TargetEntity> entities = this.repository
			.findByNameIn(Arrays.asList(targetEntityFirst.getName(), targetEntitySecond.getName()));

		// Test result
		assertEquals(2, entities.size());
		assertEquals("NAME FIRST", entities.get(0).getName());
		assertEquals("NAME SECOND", entities.get(1).getName());
	}

	/**
	 * Test findByNameContainingIgnoreCase method
	 */
	@Test
	void shouldFindByNameContainingIgnoreCase() {

		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.HOST);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);

		// Retrieve entity
		List<TargetEntity> entities = this.repository.findByNameContainingIgnoreCase("name");

		// Test result
		assertEquals(1, entities.size());
		assertEquals("NAME", entities.get(0).getName());
	}

	/**
	 * Test findByNameContainingIgnoreCase method
	 */
	@Test
	void shouldFindByNameContainingIgnoreCasePageable() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.HOST);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);
		Pageable pageable = PageRequest.of(0, 3);

		// Retrieve entity
		Page<TargetEntity> entities = this.repository.findByNameContainingIgnoreCase("name", pageable);

		// Test result
		assertEquals(1, entities.getNumberOfElements());
		assertEquals("NAME", entities.get().findFirst().get().getName());
	}

	/**
	 * Test findByNameContainingIgnoreCaseAndType method
	 */
	@Test
	void shouldFindByNameContainingIgnoreCaseAndTypePageable() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);
		Pageable pageable = PageRequest.of(0, 3);

		// Retrieve entity
		Page<TargetEntity> entities = this.repository.findByNameContainingIgnoreCaseAndType("name",
				TargetType.USER_GROUP, pageable);

		// Test result
		assertEquals(1, entities.getNumberOfElements());
		assertEquals("NAME", entities.get().findFirst().get().getName());
	}

	/**
	 * Test findByIdIn method
	 */
	@Test
	void shouldFindByIdInPageable() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);
		Pageable pageable = PageRequest.of(0, 3);

		// Retrieve entity
		Page<TargetEntity> entities = this.repository.findByIdIn(Collections.singleton(targetEntity.getId()), pageable);

		// Test result
		assertEquals(1, entities.getNumberOfElements());
		assertEquals("NAME", entities.get().findFirst().get().getName());
	}

	/**
	 * Test findByNameContainingIgnoreCaseAndIdIn method
	 */
	@Test
	void shouldFindByNameContainingIgnoreCaseAndIdInPageable() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);
		Pageable pageable = PageRequest.of(0, 3);

		// Retrieve entity
		Page<TargetEntity> entities = this.repository.findByNameContainingIgnoreCaseAndIdIn(targetEntity.getName(),
				Collections.singleton(targetEntity.getId()), pageable);

		// Test result
		assertEquals(1, entities.getNumberOfElements());
		assertEquals("NAME", entities.get().findFirst().get().getName());
	}

	/**
	 * Test findByNameContainingIgnoreCaseAndTypeAndIdIn method
	 */
	@Test
	void shouldFindByNameContainingIgnoreCaseAndTypeAndIdInPageable() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);
		Pageable pageable = PageRequest.of(0, 3);

		// Retrieve entity
		Page<TargetEntity> entities = this.repository.findByNameContainingIgnoreCaseAndTypeAndIdIn(
				targetEntity.getName(), TargetType.USER_GROUP, Collections.singleton(targetEntity.getId()), pageable);

		// Test result
		assertEquals(1, entities.getNumberOfElements());
		assertEquals("NAME", entities.get().findFirst().get().getName());
	}

	/**
	 * Test countByNameContainingIgnoreCase method
	 */
	@Test
	void shouldCountByNameContainingIgnoreCase() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);

		// Test result
		assertEquals(1, this.repository.countByNameContainingIgnoreCase("name"));
	}

	/**
	 * Test countByNameContainingIgnoreCaseAndType method
	 */
	@Test
	void shouldCountByNameContainingIgnoreCaseAndType() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);

		// Test result
		assertEquals(1, this.repository.countByNameContainingIgnoreCaseAndType("name", TargetType.USER_GROUP));
	}

	/**
	 * Test countByIdIn method
	 */
	@Test
	void shouldCountByIdIn() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);

		// Test result
		assertEquals(1, this.repository.countByIdIn(Collections.singleton(targetEntity.getId())));
	}

	/**
	 * Test countByNameContainingIgnoreCaseAndIdIn method
	 */
	@Test
	void shouldCountByNameContainingIgnoreCaseAndIdIn() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);

		// Test result
		assertEquals(1, this.repository.countByNameContainingIgnoreCaseAndIdIn("name",
				Collections.singleton(targetEntity.getId())));
	}

	/**
	 * Test countByNameContainingIgnoreCaseAndTypeAndIdIn method
	 */
	@Test
	void shouldCountByNameContainingIgnoreCaseAndTypeAndIdIn() {
		// Create entity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("NAME");
		targetEntity.setType(TargetType.USER_GROUP);
		LOG.info("Saving entity with name [{}]", targetEntity.getName());
		targetEntity = this.repository.saveAndFlush(targetEntity);

		// Test result
		assertEquals(1, this.repository.countByNameContainingIgnoreCaseAndTypeAndIdIn("name", TargetType.USER_GROUP,
				Collections.singleton(targetEntity.getId())));
	}

}