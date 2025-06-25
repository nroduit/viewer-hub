package org.viewer.hub.back.model.asset;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Item {

	private String downloadUrl;

	private String path;

	private String id;

	private String repository;

	private String format;

	private Map<String, String> checksum;

	private String contentType;

	private String lastModified;

	private String lastDownloaded;

	private String uploader;

	private String uploaderIp;

	private Long fileSize;

	private String blobCreated;

	private String blobStoreName;

	private Maven2 maven2;

}