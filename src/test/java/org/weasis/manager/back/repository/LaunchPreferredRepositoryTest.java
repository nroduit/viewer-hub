package org.weasis.manager.back.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.weasis.manager.back.entity.LaunchPreferredEntity;
import org.weasis.manager.back.enums.PreferredType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
class LaunchPreferredRepositoryTest {

	@Autowired
	private LaunchPreferredRepository repository;

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldSaveAndFindARecord() {
		// Create an entity to save
		LaunchPreferredEntity entity = new LaunchPreferredEntity();
		entity.setName("Name");
		entity.setType(PreferredType.PROPERTY.getCode());

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Test Save
		assertEquals("Name", entity.getName());
		assertNotNull(entity.getId());
		LOG.info("Entity with name [{}] and id [{}] saved", entity.getName(), entity.getId());

		// Find By Id
		Optional<LaunchPreferredEntity> foundByIdOpt = this.repository.findById(entity.getId());

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
		LaunchPreferredEntity entity = new LaunchPreferredEntity();
		entity.setName("Name");
		entity.setType(PreferredType.PROPERTY.getCode());

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.saveAndFlush(entity);

		// Find all
		List<LaunchPreferredEntity> all = this.repository.findAll();

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
		LaunchPreferredEntity entity = new LaunchPreferredEntity();
		entity.setName(initialText);
		entity.setType(PreferredType.PROPERTY.getCode());

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
		LaunchPreferredEntity entityModified = this.repository.save(entity);

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
		LaunchPreferredEntity entity = new LaunchPreferredEntity();
		String name = "Name";
		entity.setName(name);
		entity.setType(PreferredType.PROPERTY.getCode());

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Retrieve the entity
		Optional<LaunchPreferredEntity> foundByIdOpt = this.repository.findById(entity.getId());

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
	void shouldFindPreferredByName() {
		// Create entity
		LaunchPreferredEntity launchPreferedEntity = new LaunchPreferredEntity();
		launchPreferedEntity.setName("Name");
		launchPreferedEntity.setType(PreferredType.PROPERTY.getCode());

		LOG.info("Saving entity with name [{}]", launchPreferedEntity.getName());
		launchPreferedEntity = this.repository.saveAndFlush(launchPreferedEntity);

		// Retrieve entity
		LaunchPreferredEntity entity = this.repository.findByName("Name");

		// Test result
		assertEquals(launchPreferedEntity.getId(), entity.getId());
		assertEquals(launchPreferedEntity.getName(), entity.getName());
	}

	/**
	 * Test findByType method
	 */
	@Test
	public void shouldFindPreferedByType() {
		// Create entity
		LaunchPreferredEntity launchPreferedEntityFirst = new LaunchPreferredEntity();
		launchPreferedEntityFirst.setName("Name First");
		launchPreferedEntityFirst.setType(PreferredType.PROPERTY.getCode());

		LOG.info("Saving entity with name [{}]", launchPreferedEntityFirst.getName());
		launchPreferedEntityFirst = this.repository.saveAndFlush(launchPreferedEntityFirst);

		// Create entity
		LaunchPreferredEntity launchPreferedEntitySecond = new LaunchPreferredEntity();
		launchPreferedEntitySecond.setName("Name Second");
		launchPreferedEntitySecond.setType(PreferredType.PROPERTY.getCode());

		LOG.info("Saving entity with name [{}]", launchPreferedEntitySecond.getName());
		launchPreferedEntitySecond = this.repository.saveAndFlush(launchPreferedEntitySecond);

		// Retrieve entity
		List<LaunchPreferredEntity> entities = this.repository.findByType(PreferredType.PROPERTY.getCode());

		// Test result
		assertEquals(2, entities.size());
	}

	/**
	 * Test method existsByName
	 */
	@Test
	void shouldCheckExistByName() {
		// Create entity
		LaunchPreferredEntity entity = new LaunchPreferredEntity();
		entity.setName("Test");
		entity.setType("Type");

		// Save the entity
		LOG.info("Saving entity with name [{}]", entity.getName());
		this.repository.saveAndFlush(entity);

		// Call service
		boolean exist = this.repository.existsByName("Test");

		// Test results
		assertTrue(exist);
	}

	/**
	 * Test findByNameIn method
	 */
	@Test
	void shouldFindLaunchPreferedByNames() {
		// Create entity
		LaunchPreferredEntity launchPreferedEntityFirst = new LaunchPreferredEntity();
		launchPreferedEntityFirst.setName("Name First");
		launchPreferedEntityFirst.setType("Type");
		LOG.info("Saving entity with name [{}]", launchPreferedEntityFirst.getName());
		launchPreferedEntityFirst = this.repository.saveAndFlush(launchPreferedEntityFirst);
		// Create entity
		LaunchPreferredEntity launchPreferedEntitySecond = new LaunchPreferredEntity();
		launchPreferedEntitySecond.setName("Name Second");
		launchPreferedEntitySecond.setType("Type");
		LOG.info("Saving entity with name [{}]", launchPreferedEntitySecond.getName());
		launchPreferedEntitySecond = this.repository.saveAndFlush(launchPreferedEntitySecond);

		// Retrieve entity
		List<LaunchPreferredEntity> entities = this.repository
			.findByNameIn(Arrays.asList(launchPreferedEntityFirst.getName(), launchPreferedEntitySecond.getName()));

		// Test result
		assertEquals(2, entities.size());
		assertEquals("Name First", entities.get(0).getName());
		assertEquals("Name Second", entities.get(1).getName());
	}

	/**
	 * Test method existsByType
	 */
	@Test
	public void shouldExistByType() {
		// Create entity
		LaunchPreferredEntity launchPreferedEntity = new LaunchPreferredEntity();
		launchPreferedEntity.setName("Name First");
		launchPreferedEntity.setType("Type");
		LOG.info("Saving entity with name [{}]", launchPreferedEntity.getName());
		launchPreferedEntity = this.repository.saveAndFlush(launchPreferedEntity);

		// Call repository
		assertTrue(this.repository.existsByType("Type"));
		assertFalse(this.repository.existsByType("NotExisting"));
	}

}