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

package org.weasis.manager.front.views.i18n;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.entity.I18nEntity;
import org.weasis.manager.back.service.I18nService;
import org.weasis.manager.front.views.i18n.component.I18nFilter;

import java.io.InputStream;

/**
 * Logic managing i18n versions
 */
@Getter
@Service
public class I18nLogic {

	// View
	private I18nView i18nView;

	// Service
	private final I18nService i18nService;

	@Autowired
	public I18nLogic(final I18nService i18nService) {
		this.i18nService = i18nService;
		this.i18nView = null;
	}

	public void setI18nView(I18nView i18nView) {
		this.i18nView = i18nView;
	}

	/**
	 * Retrieve I18n versions
	 * @param filter Filter to apply
	 * @param pageable Pageable
	 * @return Page of I18n versions
	 */
	public Page<I18nEntity> retrieveI18nVersions(I18nFilter filter, Pageable pageable) {
		return this.i18nService.retrieveI18nVersionsPageable(filter, pageable);
	}

	/**
	 * Count number of Override Configs
	 * @param filter Filter to apply
	 * @return number of Override Configs
	 */
	public int countI18nVersions(I18nFilter filter) {
		return this.i18nService.countI18nVersions(filter);
	}

	/**
	 * Delete i18n version selected
	 * @param i18nEntity I18nEntity to delete in S3
	 */
	public void deleteVersion(I18nEntity i18nEntity) {
		// Delete resource in s3
		this.i18nService.deleteResourceI18nVersion(i18nEntity);
	}

	/**
	 * Manage the upload of the i18n version to add
	 * @param fileData InputStream corresponding to the zip file to extract
	 * @param fileName File name
	 */
	public void handleUploadI18n(InputStream fileData, String fileName) {
		this.i18nService.handleI18nVersionToUpload(fileData, fileName);
	}

}
