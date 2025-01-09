package org.weasis.manager.back.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.weasis.manager.back.entity.ProfileEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
class ProfileRepositoryTest {

	@Autowired
	private ProfileRepository repository;

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldSaveAndFindARecord() {
		// Create an entity to save
		ProfileEntity entity = new ProfileEntity();
		entity.setName("ProfileName");

		// Save the entity
		LOG.info("Saving entity Profile with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Test Save
		assertEquals("ProfileName", entity.getName());
		assertNotNull(entity.getId());
		LOG.info("Entity Profile with name [{}] and id [{}] saved", entity.getName(), entity.getId());

		// Find By Id
		Optional<ProfileEntity> foundByIdOpt = this.repository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity Profile found with name [{}] and id [{}]", foundByIdOpt.get().getName(),
				foundByIdOpt.get().getId());
		assertEquals(entity.getId(), foundByIdOpt.get().getId());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Create an entity to save
		ProfileEntity entity = new ProfileEntity();
		entity.setName("ProfileName");

		// Save the entity
		LOG.info("Saving entity Profile with name [{}]", entity.getName());
		entity = this.repository.saveAndFlush(entity);

		// Find all
		List<ProfileEntity> all = this.repository.findAll();

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
		ProfileEntity entity = new ProfileEntity();
		entity.setName(initialText);

		// Save the entity
		LOG.info("Saving entity Profile with name [{}]", entity.getName());
		entity = this.repository.save(entity);
		LOG.info("Id of the entity Profile with name [{}]", entity.getId());

		// Test Save
		assertNotNull(entity);
		assertEquals(initialText, entity.getName());

		// Modify the record
		entity.setName(modifiedText);
		LOG.info("Modify entity Profile name [{}] to [{}]", initialText, modifiedText);
		ProfileEntity entityModified = this.repository.save(entity);

		// Test Modify
		assertNotNull(entityModified);
		assertEquals(entity.getId(), entityModified.getId());
		assertEquals(modifiedText, entityModified.getName());
		LOG.info("Name of the entity Profile with id [{}]: [{}]", entityModified.getId(), entityModified.getName());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Create an entity to save
		ProfileEntity entity = new ProfileEntity();
		String profileName = "ProfileName";
		entity.setName(profileName);

		// Save the entity
		LOG.info("Saving entity Profile with name [{}]", entity.getName());
		entity = this.repository.save(entity);

		// Retrieve the entity
		Optional<ProfileEntity> foundByIdOpt = this.repository.findById(entity.getId());

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
	 * Test the existsByWeaProfileCName method
	 */
	@Test
	void shouldCheckIfProfileExist() {

		// Create an entity to save
		ProfileEntity entity = new ProfileEntity();
		entity.setName("ProfileName");

		// Save the entity
		LOG.info("Saving entity Profile with name [{}]", entity.getName());
		entity = this.repository.saveAndFlush(entity);

		// Check if Profile exists
		assertTrue(this.repository.existsByName("ProfileName"));

		// Delete the profile
		this.repository.delete(entity);
		this.repository.flush();

		// Check if profile exists
		assertFalse(this.repository.existsByName("ProfileName"));
	}

}