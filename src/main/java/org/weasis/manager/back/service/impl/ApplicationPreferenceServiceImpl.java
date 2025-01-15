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
import org.weasis.manager.back.enums.OperationType;
import org.weasis.manager.back.repository.ModuleRepository;
import org.weasis.manager.back.repository.PreferenceRepository;
import org.weasis.manager.back.repository.ProfileRepository;
import org.weasis.manager.back.repository.TargetRepository;
import org.weasis.manager.back.repository.specification.PreferenceByUserProfileModuleSpecification;
import org.weasis.manager.back.service.ApplicationPreferenceService;
import org.weasis.manager.back.util.XmlUtil;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

/**
 * CRUD (Create, Read, Update and Delete) methods for Weasis Application Preferences
 */
@Service
@Transactional
@Slf4j
public class ApplicationPreferenceServiceImpl implements ApplicationPreferenceService, Serializable {

	private static final long serialVersionUID = -4268671207559252688L;

	private static final String WEASIS_MODULE_FOR_WEASIS_APPLICATION = "weasis";

	// Repositories
	private final PreferenceRepository preferenceRepository;

	private final ProfileRepository profileRepository;

	private final ModuleRepository moduleRepository;

	private final TargetRepository targetRepository;

	/**
	 * Autowired constructor with parameters
	 * @param preferenceRepository Preference Repository
	 * @param profileRepository Profile Repository
	 * @param moduleRepository Module Repository
	 */
	@Autowired
	public ApplicationPreferenceServiceImpl(PreferenceRepository preferenceRepository,
			ProfileRepository profileRepository, ModuleRepository moduleRepository, TargetRepository targetRepository) {
		this.preferenceRepository = preferenceRepository;
		this.profileRepository = profileRepository;
		this.moduleRepository = moduleRepository;
		this.targetRepository = targetRepository;
	}

	@Override
	public void createWeasisPreferences(String user, String profileName, String moduleName, String preferences)
			throws SQLException {
		LOG.debug("createWeasisPreferences");

		// Check if we have a profile and module entities
		if (this.moduleRepository.existsByName(moduleName) && this.profileRepository.existsByName(profileName)) {
			Long profileId = this.profileRepository.findByName(profileName).getId();
			Long moduleId = this.moduleRepository.findByName(moduleName).getId();

			// Save a new preference in DB
			this.preferenceRepository.save(this.buildNewApplicationPreference(user, profileId, moduleId, preferences));
		}
	}

	@Override
	public String readWeasisPreferences(String user, String profileName, String moduleName, boolean prettyPrint)
			throws SQLException {
		LOG.debug("readWeasisPreferences");

		moduleName = (moduleName != null) ? moduleName : WEASIS_MODULE_FOR_WEASIS_APPLICATION;

		// Retrieve Preference By User/Module/Profile names
		PreferenceEntity preference = this.retrievePreferenceByUserModuleProfileNames(user, moduleName, profileName);

		// Get the preference content
		String preferenceContent = preference != null ? preference.getContent() : null;

		if (preferenceContent != null && prettyPrint) {
			preferenceContent = XmlUtil.prettyPrint(preferenceContent);
		}

		return preferenceContent;
	}

	@Override
	public String readWeasisPreferences(String user, String profileName, String moduleName) throws SQLException {
		LOG.debug("readWeasisPreferences");
		return this.readWeasisPreferences(user, profileName, moduleName, false);
	}

	@Override
	public OperationType updateWeasisPreferences(String user, String profileName, String moduleName, String preferences)
			throws SQLException {
		LOG.debug("updateWeasisPreferences");
		OperationType operationType;
		moduleName = (moduleName != null) ? moduleName : WEASIS_MODULE_FOR_WEASIS_APPLICATION;

		if (!this.moduleRepository.existsByName(moduleName)) {
			ModuleEntity entity = new ModuleEntity();
			entity.setName(moduleName);
			this.moduleRepository.save(entity);
		}

		if (!this.profileRepository.existsByName(profileName)) {
			ProfileEntity profileEntity = new ProfileEntity();
			profileEntity.setName(profileName);
			this.profileRepository.save(profileEntity);
		}

		if (!this.preferenceRepository.existsByTargetName(user)) {
			this.createWeasisPreferences(user, profileName, moduleName, preferences);
			operationType = OperationType.CREATION;
		}
		else {
			// Retrieve Preference By User/Module/Profile names
			PreferenceEntity preference = this.retrievePreferenceByUserModuleProfileNames(user, moduleName,
					profileName);
			Long preferencesId = preference != null ? preference.getId() : null;

			if (preferencesId == null) {
				this.createWeasisPreferences(user, profileName, moduleName, preferences);
				operationType = OperationType.CREATION;
			}
			else {
				preference.setContent(preferences);
				preference.setUpdateDate(LocalDateTime.now());
				this.preferenceRepository.save(preference);
				operationType = OperationType.UPDATE;
			}
		}

		return operationType;
	}

	/**
	 * Create a new preference entity
	 * @param user User
	 * @param profileId Profile Id
	 * @param moduleId Module Id
	 * @param preference Preference value
	 * @return Preference entity to save
	 * @throws SQLException
	 */
	private PreferenceEntity buildNewApplicationPreference(String user, Long profileId, Long moduleId,
			String preference) throws SQLException {
		PreferenceEntity preferenceEntity = new PreferenceEntity();
		preferenceEntity
			.setTarget(this.targetRepository.findOptionalByNameIgnoreCase(user).orElseThrow(SQLException::new));
		preferenceEntity.setProfile(this.profileRepository.findById(profileId).orElseThrow(SQLException::new));
		preferenceEntity.setModule(this.moduleRepository.findById(moduleId).orElseThrow(SQLException::new));
		preferenceEntity.setContent(preference);
		preferenceEntity.setCreationDate(LocalDateTime.now());
		preferenceEntity.setUpdateDate(LocalDateTime.now());
		return preferenceEntity;
	}

	/**
	 * Retrieve Preference By User/Module/Profile names
	 * @param user User
	 * @param moduleName Module Name
	 * @param profileName Profile Name
	 * @return PreferenceEntity found
	 */
	private PreferenceEntity retrievePreferenceByUserModuleProfileNames(String user, String moduleName,
			String profileName) {
		// Get the module corresponding to the name requested
		ModuleEntity module = this.moduleRepository.findByName(moduleName);
		// Get the profile corresponding to the name requested
		ProfileEntity profile = this.profileRepository.findByName(profileName);
		// Get the target corresponding to the name requested
		TargetEntity target = this.targetRepository.findByNameIgnoreCase(user);

		Optional<PreferenceEntity> oPreference = Optional.empty();

		if (module != null && profile != null && target != null) {

			// Create the specification to query the preference table
			Specification<PreferenceEntity> preferenceSpecification = new PreferenceByUserProfileModuleSpecification(
					target.getId(), profile.getId(), Collections.singletonList(module.getId()));

			// Apply the specification to retrieve results
			oPreference = this.preferenceRepository.findOne(Specification.where(preferenceSpecification));
		}

		// Return the entity found
		return oPreference.orElse(null);
	}

}
