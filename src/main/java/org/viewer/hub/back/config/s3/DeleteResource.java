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

package org.viewer.hub.back.config.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Class used to delete in S3
 */
@Slf4j
@Component
public class DeleteResource {

	private final S3AsyncClient s3AsyncClient;

	private final S3ClientConfigurationProperties s3config;

	@Autowired
	public DeleteResource(final S3AsyncClient s3AsyncClient, final S3ClientConfigurationProperties s3config) {
		this.s3AsyncClient = s3AsyncClient;
		this.s3config = s3config;
	}

	/**
	 * Delete objects in S3 based on keys in parameters
	 * @param keys Keys to delete
	 * @return CompletableFuture
	 */
	public CompletableFuture<DeleteObjectsResponse> deleteObjects(Set<String> keys) {
		// Retrieve the object identifiers
		List<ObjectIdentifier> objectIdentifiers = keys.stream()
			.map(key -> ObjectIdentifier.builder().key(key).build())
			.toList();

		// Build the delete objects request
		DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
			.bucket(this.s3config.getBucket())
			.delete(d -> d.objects(objectIdentifiers))
			.build();

		// Delete objects
		return this.s3AsyncClient.deleteObjects(deleteObjectsRequest);
	}

}