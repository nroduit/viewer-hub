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
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.LaunchEntityPK;
import org.viewer.hub.back.entity.LaunchPreferredEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.PreferredType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.repository.LaunchPreferredRepository;
import org.viewer.hub.back.repository.LaunchRepository;
import org.viewer.hub.back.repository.TargetRepository;

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

	@MockBean
	ClientRegistrationRepository clientRegistrationRepository;

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