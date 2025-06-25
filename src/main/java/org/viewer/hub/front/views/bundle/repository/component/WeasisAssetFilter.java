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
package org.viewer.hub.front.views.bundle.repository.component;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.back.util.DateTimeUtil;
import org.viewer.hub.back.util.StringUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Filter for Weasis Assets
 */
@Getter
@Setter
public class WeasisAssetFilter {

	private String artifactName;

	private String artifactVersion;

	private String fileSize;

	private String uploader;

	private String lastModified;

	private String lastDownloaded;

	public WeasisAssetFilter() {
		this.artifactName = "";
		this.artifactVersion = "";
		this.fileSize = "";
		this.uploader = "";
		this.lastModified = "";
		this.lastDownloaded = "";
	}

	/**
	 * Apply filters on Weasis assets
	 * @param weasisAssetModels Model of Weasis Asset
	 * @param filters Filters to apply
	 * @return List of Weasis Assets filtered
	 */
	public static List<WeasisAssetModel> applyFilters(List<WeasisAssetModel> weasisAssetModels,
			WeasisAssetFilter filters) {

		if (StringUtils.isNotBlank(filters.getArtifactName())) {
			weasisAssetModels = weasisAssetModels.stream()
				.filter(weasisAssetModel -> weasisAssetModel.getArtifactId() != null
						&& weasisAssetModel.getArtifactId().contains(filters.getArtifactName().trim()))
				.toList();
		}

		if (StringUtils.isNotBlank(filters.getArtifactVersion())) {
			weasisAssetModels = weasisAssetModels.stream()
				.filter(weasisAssetModel -> weasisAssetModel.getVersion() != null
						&& weasisAssetModel.getVersion().contains(filters.getArtifactVersion().trim()))
				.toList();
		}

		if (StringUtils.isNotBlank(filters.getFileSize())) {
			weasisAssetModels = weasisAssetModels.stream()
				.filter(weasisAssetModel -> weasisAssetModel.getFileSize() != null
						&& StringUtil.convertSizeToReadableFormat(weasisAssetModel.getFileSize())
							.contains(filters.getFileSize().trim()))
				.toList();
		}

		if (StringUtils.isNotBlank(filters.getUploader())) {
			weasisAssetModels = weasisAssetModels.stream()
				.filter(weasisAssetModel -> weasisAssetModel.getUploader() != null
						&& weasisAssetModel.getUploader().contains(filters.getUploader().trim()))
				.toList();
		}

		if (StringUtils.isNotBlank(filters.getLastModified())) {
			weasisAssetModels = weasisAssetModels.stream()
				.filter(weasisAssetModel -> weasisAssetModel.getLastModified() != null && DateTimeUtil
					.parseIso8601DateStringToReadableFormat(weasisAssetModel.getLastModified(),
							DateTimeFormatter.ofPattern(WeasisAssetModel.DATE_FORMAT))
					.contains(filters.getLastModified().trim()))
				.toList();

		}

		if (StringUtils.isNotBlank(filters.getLastDownloaded())) {
			weasisAssetModels = weasisAssetModels.stream()
				.filter(weasisAssetModel -> weasisAssetModel.getLastDownloaded() != null && DateTimeUtil
					.parseIso8601DateStringToReadableFormat(weasisAssetModel.getLastDownloaded(),
							DateTimeFormatter.ofPattern(WeasisAssetModel.DATE_FORMAT))
					.contains(filters.getLastDownloaded().trim()))
				.toList();
		}

		return weasisAssetModels;
	}

	public boolean hasFilter() {
		return StringUtils.isNotBlank(this.artifactName) || StringUtils.isNotBlank(this.artifactVersion)
				|| StringUtils.isNotBlank(this.fileSize) || StringUtils.isNotBlank(this.uploader)
				|| StringUtils.isNotBlank(this.lastModified) || StringUtils.isNotBlank(this.lastDownloaded);
	}

}
