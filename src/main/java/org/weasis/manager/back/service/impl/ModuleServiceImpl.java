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

package org.weasis.manager.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weasis.manager.back.entity.ModuleEntity;
import org.weasis.manager.back.entity.PreferenceEntity;
import org.weasis.manager.back.entity.ProfileEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.model.WeasisModule;
import org.weasis.manager.back.repository.ModuleRepository;
import org.weasis.manager.back.repository.PreferenceRepository;
import org.weasis.manager.back.repository.ProfileRepository;
import org.weasis.manager.back.repository.TargetRepository;
import org.weasis.manager.back.repository.specification.PreferenceByUserProfileModuleSpecification;
import org.weasis.manager.back.service.ModuleService;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD (Create, Read, Update and Delete) methods for Weasis Modules
 */
@Service
@Transactional
@Slf4j
public class ModuleServiceImpl implements ModuleService, Serializable {

	private static final long serialVersionUID = -2271967328590714083L;

	// Repositories
	private final PreferenceRepository preferenceRepository;

	private final ModuleRepository moduleRepository;

	private final ProfileRepository profileRepository;

	private final TargetRepository targetRepository;

	/**
	 * Autowired constructor
	 * @param preferenceRepository Preference Repository
	 * @param moduleRepository Module Repository
	 * @param profileRepository Profile Repository
	 */
	@Autowired
	public ModuleServiceImpl(final PreferenceRepository preferenceRepository, final ModuleRepository moduleRepository,
			final ProfileRepository profileRepository, final TargetRepository targetRepository) {
		this.preferenceRepository = preferenceRepository;
		this.moduleRepository = moduleRepository;
		this.profileRepository = profileRepository;
		this.targetRepository = targetRepository;
	}

	@Override
	public List<WeasisModule> readWeasisModules(String user, String profileName) throws SQLException {
		LOG.debug("readWeasisModules");

		// Get all the module ids
		List<Long> modulesIds = this.moduleRepository.findAll()
			.stream()
			.map(ModuleEntity::getId)
			.collect(Collectors.toList());

		// Get the profile corresponding to the name requested
		ProfileEntity profile = this.profileRepository.findByName(profileName);

		// Get the target corresponding to the name requested
		TargetEntity target = this.targetRepository.findByNameIgnoreCase(user);

		// Create the specification to query the preference table
		Specification<PreferenceEntity> preferenceSpecification = new PreferenceByUserProfileModuleSpecification(
				target.getId(), profile.getId(), modulesIds);

		// Apply the specification to retrieve results
		List<PreferenceEntity> preferences = this.preferenceRepository
			.findAll(Specification.where(preferenceSpecification));

		// Transform preference entities into WeasisModule
		return preferences.stream()
			.map(p -> new WeasisModule(p.getModule().getId(), p.getModule().getName()))
			.collect(Collectors.toList());
	}

}
