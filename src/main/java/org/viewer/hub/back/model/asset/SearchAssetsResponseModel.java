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