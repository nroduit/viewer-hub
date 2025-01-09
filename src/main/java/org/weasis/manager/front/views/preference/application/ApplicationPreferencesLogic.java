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

package org.weasis.manager.front.views.preference.application;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.Message;
import org.weasis.manager.back.model.MessageFormat;
import org.weasis.manager.back.model.MessageLevel;
import org.weasis.manager.back.model.MessageType;
import org.weasis.manager.back.model.WeasisModule;
import org.weasis.manager.back.model.WeasisProfile;
import org.weasis.manager.back.service.ApplicationPreferenceService;
import org.weasis.manager.back.service.ModuleService;
import org.weasis.manager.back.service.ProfileService;
import org.weasis.manager.back.service.TargetService;

import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@Service
public class ApplicationPreferencesLogic implements Serializable {

	@Serial
	private static final long serialVersionUID = -9186033345057468854L;

	// INFO/ERROR MESSAGES
	private static final String MSG_ERROR_READING_USERS = "Une erreur est survenue lors de la lecture des utilisateurs Weasis";

	private static final String MSG_ERROR_READING_WEASIS_PROFILES = "Une erreur est survenue lors de la lecture des profils Weasis";

	private static final String MSG_ERROR_READING_WEASIS_MODULES = "Une erreur est survenue lors de la lecture des modules Weasis";

	private static final String MSG_ERROR_READING_WEASIS_PREFERENCES = "Une erreur est survenue lors de la lecture des préférences Weasis";

	// VIEW
	private ApplicationPreferencesView view;

	// SERVICES

	private final ProfileService profileService;

	private final ModuleService moduleService;

	private final ApplicationPreferenceService applicationPreferenceService;

	private final TargetService targetService;

	/**
	 * Autowired constructor with parameters
	 * @param applicationPreferenceService Application Preference Service
	 * @param moduleService Module Service
	 * @param profileService Launch Profile Service
	 * @param targetService Target service
	 */
	@Autowired
	public ApplicationPreferencesLogic(ApplicationPreferenceService applicationPreferenceService,
			ModuleService moduleService, ProfileService profileService, TargetService targetService) {
		this.profileService = profileService;
		this.moduleService = moduleService;
		this.applicationPreferenceService = applicationPreferenceService;
		this.targetService = targetService;
		this.view = null;
	}

	public void viewOpened() {
		List<TargetEntity> users = this.targetService.retrieveTargetsByType(TargetType.USER);
		this.view.displayUsersField(users);
	}

	public void userSelection(@Valid TargetEntity user) {
		try {
			List<WeasisProfile> weasisProfiles = this.profileService.readProfiles(user.getName());
			this.view.displayWeasisProfilesField(weasisProfiles);
		}
		catch (SQLException e) {
			Message message = new Message(MessageLevel.ERROR, MessageFormat.TEXT, MSG_ERROR_READING_WEASIS_PROFILES);
			this.view.displayMessage(message, MessageType.NOTIFICATION_MESSAGE);
		}
	}

	public void weasisProfileSelection(@Valid TargetEntity user, WeasisProfile weasisProfile) {
		try {
			List<WeasisModule> weasisModules = this.moduleService.readWeasisModules(user.getName(),
					weasisProfile.getName());
			this.view.displayWeasisModulesField(weasisModules);
		}
		catch (SQLException e) {
			Message message = new Message(MessageLevel.ERROR, MessageFormat.TEXT, MSG_ERROR_READING_WEASIS_MODULES);
			this.view.displayMessage(message, MessageType.NOTIFICATION_MESSAGE);
		}
	}

	public void weasisModuleSelection(@Valid TargetEntity user, WeasisProfile weasisProfile,
			WeasisModule weasisModule) {
		try {
			String preferences = this.applicationPreferenceService.readWeasisPreferences(user.getName(),
					weasisProfile.getName(), weasisModule.getName(), true);

			if (preferences == null) {
				preferences = "No preferences found!";
			}

			this.view.displayPreferencesField(preferences);
		}
		catch (SQLException e) {
			Message message = new Message(MessageLevel.ERROR, MessageFormat.TEXT, MSG_ERROR_READING_WEASIS_PREFERENCES);
			this.view.displayMessage(message, MessageType.NOTIFICATION_MESSAGE);
		}
	}

	public ApplicationPreferencesView getView() {
		return this.view;
	}

	public void setView(ApplicationPreferencesView view) {
		this.view = view;
	}

}
