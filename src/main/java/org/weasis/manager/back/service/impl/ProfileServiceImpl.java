/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weasis.manager.back.entity.PreferenceEntity;
import org.weasis.manager.back.model.WeasisProfile;
import org.weasis.manager.back.repository.PreferenceRepository;
import org.weasis.manager.back.repository.ProfileRepository;
import org.weasis.manager.back.service.ProfileService;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD (Create, Read, Update and Delete) methods for Weasis Profiles
 */
@Service
@Transactional
@Slf4j
public class ProfileServiceImpl implements ProfileService, Serializable {

	private static final long serialVersionUID = -7924858773898049216L;

	// Repositories
	private final ProfileRepository profileRepository;

	private final PreferenceRepository preferenceRepository;

	@Autowired
	public ProfileServiceImpl(ProfileRepository profileRepository, PreferenceRepository preferenceRepository) {
		this.profileRepository = profileRepository;
		this.preferenceRepository = preferenceRepository;
	}

	@Override
	public List<WeasisProfile> readProfiles(String user) throws SQLException {
		LOG.debug("readProfiles");

		// Get the list of all preferences with the user name in parameter
		List<PreferenceEntity> preferenceEntities = this.preferenceRepository.findByTargetName(user);

		// Get the distinct list of all profiles from the selected preferences
		return preferenceEntities.stream()
			.map(preferenceEntity -> new WeasisProfile(preferenceEntity.getProfile().getId(),
					preferenceEntity.getProfile().getName()))
			.distinct()
			.collect(Collectors.toList());
	}

	@Override
	public List<WeasisProfile> getAllProfiles() throws SQLException {
		LOG.debug("getAllProfiles");
		return this.profileRepository.findAll()
			.stream()
			.map(p -> new WeasisProfile(p.getId(), p.getName()))
			.collect(Collectors.toList());
	}

}
