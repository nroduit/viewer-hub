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

package org.viewer.hub.back.model.asset;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class SearchAssetsResponseModel {

	private List<Item> items;

	private String continuationToken;

	public List<WeasisAssetModel> transformToWeasisAssetModels() {
		List<WeasisAssetModel> weasisAssetModels = new ArrayList<>();

		items.stream().filter(Objects::nonNull).forEach(item -> {
			WeasisAssetModel weasisAssetModel = new WeasisAssetModel();
			weasisAssetModel.setFileSize(item.getFileSize());
			weasisAssetModel.setUploader(item.getUploader());
			weasisAssetModel.setLastModified(item.getLastModified());
			weasisAssetModel.setLastDownloaded(item.getLastDownloaded());
			weasisAssetModel.setExtension(item.getMaven2() != null ? item.getMaven2().getExtension() : "");
			weasisAssetModel.setArtifactId(item.getMaven2() != null ? item.getMaven2().getArtifactId() : "");
			weasisAssetModel.setVersion(item.getMaven2() != null ? item.getMaven2().getVersion() : "");
			weasisAssetModels.add(weasisAssetModel);
		});

		return weasisAssetModels;
	}

}