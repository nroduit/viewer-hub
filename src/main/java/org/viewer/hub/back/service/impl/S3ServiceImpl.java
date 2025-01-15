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

package org.viewer.hub.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.config.s3.DeleteResource;
import org.viewer.hub.back.config.s3.DownloadResource;
import org.viewer.hub.back.config.s3.UploadResource;
import org.viewer.hub.back.service.S3Service;
import org.viewer.hub.back.util.StringUtil;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class S3ServiceImpl implements S3Service {

	private final UploadResource uploadResource;

	private final DownloadResource downloadResource;

	private final DeleteResource deleteResource;

	@Autowired
	public S3ServiceImpl(UploadResource uploadResource, DownloadResource downloadResource,
			DeleteResource deleteResource) {
		this.uploadResource = uploadResource;
		this.downloadResource = downloadResource;
		this.deleteResource = deleteResource;
	}

	@Override
	public boolean doesS3KeyExists(String key) {
		if (StringUtils.isNotBlank(key)) {
			return this.downloadResource.checkS3KeyExists(StringUtil.pathWithS3Separator(key));
		}
		return false;
	}

	@Override
	public Set<String> retrieveS3KeysFromPrefix(String prefix) {
		if (StringUtils.isNotBlank(prefix)) {
			return this.downloadResource.retrieveS3KeysFromPrefix(StringUtil.pathWithS3Separator(prefix));
		}
		return Collections.emptySet();
	}

	@Override
	public InputStream retrieveS3Object(String key) {
		if (StringUtils.isNotBlank(key)) {
			return this.downloadResource.retrieveS3ObjectInputStream(StringUtil.pathWithS3Separator(key));
		}
		return null;
	}

	@Override
	public CompletableFuture<PutObjectResponse> uploadObjectInS3(ByteArrayInputStream inputStream, String key) {
		if (inputStream != null && StringUtils.isNotBlank(key)) {
			return this.uploadResource.uploadObject(inputStream, StringUtil.pathWithS3Separator(key));
		}
		return null;
	}

	@Override
	public CompletableFuture<CompletedCopy> copyS3ObjectFromTo(String sourceKey, String destinationKey) {
		if (StringUtils.isNotBlank(sourceKey) && StringUtils.isNotBlank(destinationKey)) {
			return this.uploadResource.copyObjectFromTo(StringUtil.pathWithS3Separator(sourceKey),
					StringUtil.pathWithS3Separator(destinationKey));
		}
		return null;
	}

	@Override
	public CompletableFuture<DeleteObjectsResponse> deleteS3Objects(String prefixKey) {
		if (StringUtils.isNotBlank(prefixKey)) {
			return this.deleteResource.deleteObjects(
					this.downloadResource.retrieveS3KeysFromPrefix(StringUtil.pathWithS3Separator(prefixKey)));
		}
		return CompletableFuture.completedFuture(null);
	}

}
