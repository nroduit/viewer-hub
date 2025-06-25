package org.viewer.hub.back.model.asset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WeasisAssetModel {

	public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

	private String artifactId;

	private String version;

	private String extension;

	private Long fileSize;

	private String uploader;

	private String lastModified;

	private String lastDownloaded;

	private boolean isAlreadyInstalled;

}
