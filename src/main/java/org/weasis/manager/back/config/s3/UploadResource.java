
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

package org.weasis.manager.back.config.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weasis.manager.back.controller.exception.TechnicalException;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Class used to upload in S3
 */
@Slf4j
@Component
public class UploadResource {

	private final S3AsyncClient s3AsyncClient;

	private final S3ClientConfigurationProperties s3config;

	private final S3TransferManager s3TransferManager;

	@Autowired
	public UploadResource(final S3AsyncClient s3AsyncClient, final S3ClientConfigurationProperties s3config) {
		this.s3AsyncClient = s3AsyncClient;
		this.s3config = s3config;
		this.s3TransferManager = S3TransferManager.builder().s3Client(this.s3AsyncClient).build();
	}

	/**
	 * Copy existing S3 object to another S3 destination
	 * @param sourceKey Source key
	 * @param destinationKey Destination key
	 * @return CompletableFuture
	 */
	public CompletableFuture<CompletedCopy> copyObjectFromTo(String sourceKey, String destinationKey) {
		return this.s3TransferManager.copy(c -> c.copyObjectRequest(r -> r.sourceBucket(this.s3config.getBucket())
			.sourceKey(sourceKey)
			.destinationBucket(this.s3config.getBucket())
			.destinationKey(destinationKey))).completionFuture();
	}

	/**
	 * Upload object in S3
	 * @param inputStream Object to load
	 * @param key Key used to load the object
	 * @return CompletableFuture
	 */
	public CompletableFuture<PutObjectResponse> uploadObject(ByteArrayInputStream inputStream, String key) {
		try (inputStream) {
			// Push object in S3
			return this.s3AsyncClient.putObject(
					PutObjectRequest.builder().bucket(this.s3config.getBucket()).key(key).build(),
					AsyncRequestBody.fromInputStream(inputStream, (long) inputStream.available(),
							Executors.newSingleThreadExecutor()));
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when uploading object in S3:%s".formatted(e.getMessage()));
		}
	}

}
