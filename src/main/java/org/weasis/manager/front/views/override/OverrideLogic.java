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

package org.weasis.manager.front.views.override;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.controller.exception.TechnicalException;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.OverrideConfigEntity;
import org.weasis.manager.back.entity.PackageVersionEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.model.Message;
import org.weasis.manager.back.model.MessageFormat;
import org.weasis.manager.back.model.MessageLevel;
import org.weasis.manager.back.model.MessageType;
import org.weasis.manager.back.service.OverrideConfigService;
import org.weasis.manager.back.service.PackageService;
import org.weasis.manager.front.views.override.component.OverrideConfigFilter;
import org.weasis.manager.front.views.override.component.PackageVersionFileUpload;
import org.weasis.manager.front.views.override.component.RefreshPackageGridEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Logic managing override of package configuration
 */
@Getter
@Service
public class OverrideLogic {

	// View
	private OverrideView overrideView;

	// Service
	private final OverrideConfigService overrideConfigService;

	private final PackageService packageService;

	@Autowired
	public OverrideLogic(final OverrideConfigService overrideConfigService, final PackageService packageService) {
		this.overrideConfigService = overrideConfigService;
		this.packageService = packageService;
		this.overrideView = null;
	}

	@Async
	@EventListener
	public void onRefreshPackageGridEvent(RefreshPackageGridEvent refreshPackageGridEvent) {
		this.overrideView.clearUploadedFileAndRefresh();
	}

	public void setOverrideView(OverrideView overrideView) {
		this.overrideView = overrideView;
	}

	public Set<PackageVersionEntity> retrievePackageVersions() {
		return this.overrideConfigService.retrieveDistinctPackageVersionEntities();
	}

	public Set<LaunchConfigEntity> retrieveLaunchConfigsByPackageVersion(PackageVersionEntity packageVersionEntity) {
		return this.overrideConfigService.retrieveDistinctLaunchConfigEntitiesByPackageVersion(packageVersionEntity);
	}

	public Set<TargetEntity> retrieveGroups() {
		return this.overrideConfigService.retrieveDistinctGroupEntities();
	}

	/**
	 * Retrieve Override Configs
	 * @param filter Filter to apply
	 * @param pageable Pageable
	 * @return Page of Override Configs
	 */
	public Page<OverrideConfigEntity> retrieveOverrideConfigs(OverrideConfigFilter filter, Pageable pageable) {
		return this.overrideConfigService.retrieveOverrideConfigsPageable(filter, pageable);
	}

	/**
	 * Count number of Override Configs
	 * @param filter Filter to apply
	 * @return number of Override Configs
	 */
	public int countOverrideConfigs(OverrideConfigFilter filter) {
		return this.overrideConfigService.countOverrideConfigs(filter);
	}

	/**
	 * Update an OverrideConfigEntity in DB
	 * @param overrideConfigEntity OverrideConfigEntity
	 * @return updated OverrideConfigEntity
	 */
	public OverrideConfigEntity updateOverrideConfig(OverrideConfigEntity overrideConfigEntity) {
		return this.overrideConfigService.update(overrideConfigEntity);
	}

	/**
	 * Check if an OverrideConfigEntity already exists
	 * @param overrideConfigEntity OverrideConfigEntity
	 * @return true if the OverrideConfigEntity already exists
	 */
	public boolean doesOverrideConfigAlreadyExists(OverrideConfigEntity overrideConfigEntity) {
		return this.overrideConfigService.doesOverrideConfigAlreadyExists(overrideConfigEntity);
	}

	/**
	 * For the package version/launch config of the entity in parameter, retrieve the
	 * values from default group and set them in the entity in parameter
	 * @param overrideConfig Override config to modify
	 */
	public void copyAllValuesFromDefaultGroupExceptId(OverrideConfigEntity overrideConfig) {
		if (overrideConfig != null && overrideConfig.getPackageVersion() != null
				&& overrideConfig.getPackageVersion().getId() != null && overrideConfig.getLaunchConfig() != null
				&& overrideConfig.getLaunchConfig().getId() != null) {
			overrideConfig
				.replaceNullOrNotExistingPropertiesByDefault(this.overrideConfigService.retrieveDefaultGroupProperties(
						overrideConfig.getPackageVersion().getId(), overrideConfig.getLaunchConfig().getId()));
		}
	}

	/**
	 * Delete package version selected
	 * @param overrideConfigEntity OverrideConfigEntity to delete in the volume
	 */
	public void deleteVersion(OverrideConfigEntity overrideConfigEntity) {
		// Delete resource in volume
		this.packageService.deleteResourcePackageVersion(overrideConfigEntity);
	}

	/**
	 * Manage the upload of the package version to add
	 * @param fileData InputStream corresponding to the zip file to extract
	 * @param versionToUpload Version to upload
	 */
	public void handlePackageVersionToUpload(InputStream fileData, String versionToUpload) {
		this.packageService.handlePackageVersionToUpload(fileData, versionToUpload);
	}

	/**
	 * Handle upload of weasis-native package version
	 */
	public void handleUploadWeasisNative(PackageVersionFileUpload packageVersionFileUpload) {
		try (InputStream fileDataInputStream = packageVersionFileUpload.getMemoryBuffer().getInputStream()) {

			// Determine the version to upload, if incorrect format return null
			String versionToUpload = this.checkVersionToUpload(fileDataInputStream);

			if (versionToUpload != null) {
				this.handlePackageVersionToUpload(fileDataInputStream, versionToUpload);
			}
			else {
				this.overrideView.getPackageVersionUpload().getPackageVersionFileUpload().clearFileList();
				this.overrideView.displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
						"Issue when importing: rebuild before importing your weasis-native zip file with appropriate version in the property weasis.version of the file config.properties "),
						MessageType.NOTIFICATION_MESSAGE);
			}
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when getting the input stream of the upload component:%s".formatted(e.getMessage()));
		}
	}

	/**
	 * Determine the version to upload, if incorrect format return null.
	 * @param fileData To evaluate
	 * @return null if incorrect format of the version, otherwise return the version to
	 * upload
	 */
	public String checkVersionToUpload(InputStream fileData) {
		return this.packageService.checkWeasisNativeVersionToUpload(fileData);
	}

}
