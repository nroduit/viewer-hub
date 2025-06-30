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