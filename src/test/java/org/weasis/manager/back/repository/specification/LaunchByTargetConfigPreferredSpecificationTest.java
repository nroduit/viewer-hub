package org.weasis.manager.back.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.LaunchEntityPK;
import org.weasis.manager.back.entity.LaunchPreferredEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.PreferredType;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.LaunchConfigRepository;
import org.weasis.manager.back.repository.LaunchPreferredRepository;
import org.weasis.manager.back.repository.LaunchRepository;
import org.weasis.manager.back.repository.TargetRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.data.jpa.domain.Specification.where;

@DataJpaTest
@Slf4j
class LaunchByTargetConfigPreferredSpecificationTest {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private LaunchConfigRepository launchConfigRepository;

	@Autowired
	private LaunchPreferredRepository launchPreferredRepository;

	@Autowired
	private TargetRepository targetRepository;

	@BeforeEach
	public void init() {
		// Saving entities config / preferred / target
		// Config
		LOG.info("Saving entity config with name [{}]", "Config");
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("Config");
		this.launchConfigRepository.saveAndFlush(launchConfigEntity);
		// Preferred
		LOG.info("Saving entity preferred with name [{}]", "Preferred");
		LaunchPreferredEntity launchPreferredEntity = new LaunchPreferredEntity();
		launchPreferredEntity.setName("Preferred");
		launchPreferredEntity.setType(PreferredType.PROPERTY.getCode());
		this.launchPreferredRepository.saveAndFlush(launchPreferredEntity);
		// Target
		LOG.info("Saving entity target with name [{}]", "Target");
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("Target");
		targetEntity.setType(TargetType.USER);
		this.targetRepository.saveAndFlush(targetEntity);
	}

	/**
	 * Create a new launch entity
	 * @param selection Launch selection to set
	 * @return the built launch entity
	 */
	private LaunchEntity buildLaunch(String selection) {
		LaunchEntity entity = new LaunchEntity();
		entity.setSelection(selection);
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setTargetId(this.targetRepository.findByNameIgnoreCase("TARGET").getId());
		launchEntityPK.setLaunchConfigId(this.launchConfigRepository.findByName("Config").getId());
		launchEntityPK.setLaunchPreferredId(this.launchPreferredRepository.findByName("Preferred").getId());
		entity.setLaunchEntityPK(launchEntityPK);
		return entity;
	}

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldFindEntityWithSpecification() {
		// Create an entity to save
		LaunchEntity entity = this.buildLaunch("Launch");

		// Save the entity
		LOG.info("Saving entity Launch with selection [{}]", entity.getSelection());
		entity = this.launchRepository.saveAndFlush(entity);

		// Create the specification to query the launch table
		Specification<LaunchEntity> launchSpecification = new LaunchByTargetConfigPreferredSpecification(
				Collections.singletonList(this.targetRepository.findByNameIgnoreCase("TARGET")),
				Collections.singletonList(this.launchConfigRepository.findByName("Config")),
				Collections.singletonList(this.launchPreferredRepository.findByName("Preferred")));

		// Try to retrieve the preference with the specification
		LOG.info("Retrieving entities thanks to the specification built");
		List<LaunchEntity> entitiesFound = this.launchRepository.findAll(where(launchSpecification));

		// Test results
		assertNotNull(entitiesFound);
		assertEquals(1, entitiesFound.size());
		LaunchEntity entityFound = entitiesFound.get(0);
		assertNotNull(entityFound.getLaunchEntityPK());
		assertNotNull(entityFound.getSelection());
		assertEquals("Launch", entityFound.getSelection());
	}

}