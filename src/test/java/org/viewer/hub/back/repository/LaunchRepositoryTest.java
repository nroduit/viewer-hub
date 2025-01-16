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
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.LaunchEntityPK;
import org.viewer.hub.back.entity.LaunchPreferredEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.PreferredType;
import org.viewer.hub.back.enums.TargetType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
public class LaunchRepositoryTest {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TargetRepository targetRepository;

	@Autowired
	private LaunchPreferredRepository launchPreferredRepository;

	@Autowired
	private LaunchConfigRepository launchConfigRepository;

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldSaveAndFindARecord() {

		// Save the entity
		LaunchEntity entity = this.buildAndSaveGenericLaunchEntity();

		// Test Save
		assertEquals("LaunchConfigName",
				this.launchConfigRepository.findById(entity.getLaunchEntityPK().getLaunchConfigId()).get().getName());
		assertNotNull(entity.getLaunchEntityPK().getTargetId());
		assertNotNull(entity.getLaunchEntityPK().getLaunchConfigId());
		assertNotNull(entity.getLaunchEntityPK().getLaunchPreferredId());
		LOG.info("Entity with ids target/launchConfig/launchPrefered [{}] [{}] [{}] saved",
				entity.getLaunchEntityPK().getTargetId(), entity.getLaunchEntityPK().getLaunchConfigId(),
				entity.getLaunchEntityPK().getLaunchPreferredId());

		// Find By Id
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setTargetId(entity.getLaunchEntityPK().getTargetId());
		launchEntityPK.setLaunchConfigId(entity.getLaunchEntityPK().getLaunchConfigId());
		launchEntityPK.setLaunchPreferredId(entity.getLaunchEntityPK().getLaunchPreferredId());
		Optional<LaunchEntity> foundByIdOpt = this.launchRepository.findById(launchEntityPK);

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity found with with ids target/launchConfig/launchPrefered [{}] [{}] [{}]",
				foundByIdOpt.get().getLaunchEntityPK().getTargetId(),
				foundByIdOpt.get().getLaunchEntityPK().getLaunchConfigId(),
				foundByIdOpt.get().getLaunchEntityPK().getLaunchPreferredId());
		assertEquals(entity.getLaunchEntityPK().getTargetId(), foundByIdOpt.get().getLaunchEntityPK().getTargetId());
		assertEquals(entity.getLaunchEntityPK().getLaunchConfigId(),
				foundByIdOpt.get().getLaunchEntityPK().getLaunchConfigId());
		assertEquals(entity.getLaunchEntityPK().getLaunchPreferredId(),
				foundByIdOpt.get().getLaunchEntityPK().getLaunchPreferredId());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Save the entity
		this.buildAndSaveGenericLaunchEntity();

		// Find all
		List<LaunchEntity> all = this.launchRepository.findAll();

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

		// Save the entity
		LaunchEntity entity = this.buildAndSaveGenericLaunchEntity();

		// Keep the primary key
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setTargetId(entity.getLaunchEntityPK().getTargetId());
		launchEntityPK.setLaunchConfigId(entity.getLaunchEntityPK().getLaunchConfigId());
		launchEntityPK.setLaunchPreferredId(entity.getLaunchEntityPK().getLaunchPreferredId());

		// Modify the record
		entity.setSelection("ModifiedText");
		LOG.info("Modify entity value to ModifiedText");
		LaunchEntity entityModified = this.launchRepository.saveAndFlush(entity);

		// Test Modify
		assertNotNull(entityModified);
		assertEquals(entity.getLaunchEntityPK().getTargetId(), entityModified.getLaunchEntityPK().getTargetId());
		assertEquals(entity.getLaunchEntityPK().getLaunchConfigId(),
				entityModified.getLaunchEntityPK().getLaunchConfigId());
		assertEquals(entity.getLaunchEntityPK().getLaunchPreferredId(),
				entityModified.getLaunchEntityPK().getLaunchPreferredId());
		assertEquals("ModifiedText", entityModified.getSelection());
		LOG.info("Name of the entity with id [{}]: [{}]", launchEntityPK, entityModified.getSelection());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Save the entity
		LaunchEntity entity = this.buildAndSaveGenericLaunchEntity();

		// Retrieve the entity
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setTargetId(entity.getLaunchEntityPK().getTargetId());
		launchEntityPK.setLaunchConfigId(entity.getLaunchEntityPK().getLaunchConfigId());
		launchEntityPK.setLaunchPreferredId(entity.getLaunchEntityPK().getLaunchPreferredId());
		Optional<LaunchEntity> foundByIdOpt = this.launchRepository.findById(launchEntityPK);

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());

		// Delete the entity
		entity = foundByIdOpt.get();
		// Long id = entity.getId();
		LOG.info("Deleting entity with id [{}]", launchEntityPK);
		this.launchRepository.delete(entity);

		// Test Delete
		foundByIdOpt = this.launchRepository.findById(launchEntityPK);
		LOG.info("Is deleted entity with id [{}] present: [{}]", launchEntityPK, foundByIdOpt.isPresent());
		assertFalse(foundByIdOpt.isPresent());
	}

	/**
	 * Build a generic launch entity
	 * @return Generic Launch Entity saved
	 */
	public static LaunchEntity buildLaunchEntity(Long targetId, String targetName, TargetType targetType, Long configId,
			String configName, Long preferedId, String preferedName, PreferredType preferredType,
			String launchSelection) {
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setId(targetId);
		targetEntity.setName(targetName);
		targetEntity.setType(targetType);
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName(configName);
		launchConfigEntity.setId(configId);
		LaunchPreferredEntity launchPreferredEntity = new LaunchPreferredEntity();
		launchPreferredEntity.setName(preferedName);
		launchPreferredEntity.setId(preferedId);
		launchPreferredEntity.setType(preferredType.getCode());

		return buildLaunchEntityWithTargetLaunchConfigLaunchPrefered(targetEntity, launchConfigEntity,
				launchPreferredEntity, launchSelection);
	}

	/**
	 * Build and save launch entity
	 * @param targetEntity Target to save
	 * @param launchConfigEntity Launch Config to save
	 * @param launchPreferedEntity Launch Prefered to save
	 * @param selection Selection to save
	 * @return Saved Launch entity
	 */
	private LaunchEntity buildAndSaveLaunchEntity(TargetEntity targetEntity, LaunchConfigEntity launchConfigEntity,
			LaunchPreferredEntity launchPreferedEntity, String selection) {

		// Save entities
		targetEntity = this.targetRepository.saveAndFlush(targetEntity);
		launchConfigEntity = this.launchConfigRepository.saveAndFlush(launchConfigEntity);
		launchPreferedEntity = this.launchPreferredRepository.saveAndFlush(launchPreferedEntity);

		// Build primary key
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setTargetId(targetEntity.getId());
		launchEntityPK.setLaunchConfigId(launchConfigEntity.getId());
		launchEntityPK.setLaunchPreferredId(launchPreferedEntity.getId());

		// Build launch entity
		LaunchEntity launchEntity = new LaunchEntity();
		launchEntity.setLaunchEntityPK(launchEntityPK);
		launchEntity.setSelection(selection);

		// Save Launch Entity
		return this.launchRepository.saveAndFlush(launchEntity);
	}

	/**
	 * Build launch entity
	 * @param targetEntity Target to save
	 * @param launchConfigEntity Launch Config to save
	 * @param launchPreferedEntity Launch Prefered to save
	 * @param selection Selection to save
	 * @return Saved Launch entity
	 */
	static LaunchEntity buildLaunchEntityWithTargetLaunchConfigLaunchPrefered(TargetEntity targetEntity,
			LaunchConfigEntity launchConfigEntity, LaunchPreferredEntity launchPreferedEntity, String selection) {
		// Build primary key
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setTargetId(targetEntity.getId());
		launchEntityPK.setLaunchConfigId(launchConfigEntity.getId());
		launchEntityPK.setLaunchPreferredId(launchPreferedEntity.getId());

		// Build launch entity
		LaunchEntity launchEntity = new LaunchEntity();
		launchEntity.setLaunchEntityPK(launchEntityPK);
		launchEntity.setSelection(selection);

		// Set associated entities
		launchEntity.setAssociatedTarget(targetEntity);
		launchEntity.setAssociatedConfig(launchConfigEntity);
		launchEntity.setAssociatedPreferred(launchPreferedEntity);

		// return Launch Entity
		return launchEntity;
	}

	/**
	 * Build and save a generic launch entity
	 * @return Generic Launch Entity saved
	 */
	private LaunchEntity buildAndSaveGenericLaunchEntity() {
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("TargetName");
		targetEntity.setType(TargetType.USER);
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("LaunchConfigName");
		LaunchPreferredEntity launchPreferredEntity = new LaunchPreferredEntity();
		launchPreferredEntity.setName("LaunchPreferredName");
		launchPreferredEntity.setType(PreferredType.PROPERTY.getCode());

		return this.buildAndSaveLaunchEntity(targetEntity, launchConfigEntity, launchPreferredEntity, "selection");
	}

	/**
	 * Test on method existsByLaunchEntityPKTargetId
	 */
	@Test
	void shouldCheckExistsByLaunchEntityPKTargetId() {
		// Build launch
		LaunchEntity launch = LaunchRepositoryTest.buildLaunchEntity(867L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchSelection");

		// Save launch
		this.launchRepository.saveAndFlush(launch);

		// Check result
		assertTrue(this.launchRepository.existsByLaunchEntityPKTargetId(launch.getLaunchEntityPK().getTargetId()));
	}

	/**
	 * Test on method existsByLaunchEntityPKLaunchConfigId
	 */
	@Test
	void shouldCheckExistsByLaunchEntityPKLaunchConfigId() {
		// Build launch
		LaunchEntity launch = LaunchRepositoryTest.buildLaunchEntity(867L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchSelection");

		// Save launch
		this.launchRepository.saveAndFlush(launch);

		// Check result
		assertTrue(this.launchRepository
			.existsByLaunchEntityPKLaunchConfigId(launch.getLaunchEntityPK().getLaunchConfigId()));
	}

	/**
	 * Test on method existsByLaunchEntityPKLaunchPreferedId
	 */
	@Test
	void shouldCheckExistsByLaunchEntityPKLaunchPreferedId() {
		// Build launch
		LaunchEntity launch = LaunchRepositoryTest.buildLaunchEntity(867L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchSelection");

		// Save launch
		this.launchRepository.saveAndFlush(launch);

		// Check result
		assertTrue(this.launchRepository
			.existsByLaunchEntityPKLaunchPreferredId(launch.getLaunchEntityPK().getLaunchPreferredId()));
	}

	/**
	 * Test on method findByLaunchEntityPKTargetId
	 */
	@Test
	void shouldFindLaunchByTargetId() {
		// Build launch
		LaunchEntity launch = LaunchRepositoryTest.buildLaunchEntity(867L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchSelection");

		// Save launch
		this.launchRepository.saveAndFlush(launch);

		// Call method
		List<LaunchEntity> launchesFound = this.launchRepository
			.findByLaunchEntityPKTargetId(launch.getLaunchEntityPK().getTargetId());

		// Check result
		assertNotNull(launchesFound);
		assertFalse(launchesFound.isEmpty());
		assertEquals(launch, launchesFound.get(0));
	}

}