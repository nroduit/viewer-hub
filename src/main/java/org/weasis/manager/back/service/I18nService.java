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

package org.weasis.manager.back.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.weasis.manager.back.entity.I18nEntity;
import org.weasis.manager.front.views.i18n.component.I18nFilter;

import java.io.InputStream;

/**
 * Service managing i18n versions
 */
public interface I18nService {

	/**
	 * Update the table i18n if a version is missing compared to the available versions in
	 * S3
	 */
	void refreshAvailableI18nVersion();

	/**
	 * Retrieve i18n versions depending on filter and pageable
	 * @param filter Filter to evaluate
	 * @param pageable Pageable to evaluate
	 * @return i18n versions entities found
	 */
	Page<I18nEntity> retrieveI18nVersionsPageable(I18nFilter filter, Pageable pageable);

	/**
	 * Count i18n versions depending on filter
	 * @param filter Filter to evaluate
	 * @return Count of i18n versions entities found
	 */
	int countI18nVersions(I18nFilter filter);

	/**
	 * Remove the folder i18n and update the db for i18n version
	 * @param i18nEntity I18nEntity to delete
	 */
	void deleteResourceI18nVersion(I18nEntity i18nEntity);

	/**
	 * Upload the zip file containing the i18n version to add
	 * @param fileData InputStream corresponding to the zip file to extract
	 * @param fileName File name ex: weasis-i18n-dist-3.0.0-SNAPSHOT.zip
	 */
	void handleI18nVersionToUpload(InputStream fileData, String fileName);

}
