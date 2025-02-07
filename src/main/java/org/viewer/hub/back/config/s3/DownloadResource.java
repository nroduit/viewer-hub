
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
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.util.StringUtil;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Class used to download in S3
 */
@Slf4j
@Component
public class DownloadResource {

	private final S3AsyncClient s3AsyncClient;

	private final S3ClientConfigurationProperties s3config;

	@Autowired
	public DownloadResource(final S3AsyncClient s3AsyncClient, final S3ClientConfigurationProperties s3config) {
		this.s3config = s3config;
		this.s3AsyncClient = s3AsyncClient;
	}

	/**
	 * Check existence of S3 objects with prefix key in parameter
	 * @param key Key to retrieve
	 * @return true if objects are present with the prefix key path in parameter
	 */
	public boolean checkS3KeyExists(String key) {
		try {
			return this.retrieveS3KeysFromPrefix(key).stream().findFirst().isPresent();
		}
		catch (TechnicalException e) {
			throw new TechnicalException(
					"Issue when checking existence of the S3 key %s:%s".formatted(key, e.getMessage()));
		}
	}

	/**
	 * Retrieve S3 keys from prefix
	 * @param prefix Prefix to evaluate
	 * @return Set of keys
	 */
	public Set<String> retrieveS3KeysFromPrefix(String prefix) {
		return this.retrieveS3ObjectsFromPrefix(prefix).stream().map(S3Object::key).collect(Collectors.toSet());
	}

	/**
	 * Retrieve S3 objects from prefix
	 * @param prefix Prefix to evaluate
	 * @return List of S3 objects found
	 */
	public List<S3Object> retrieveS3ObjectsFromPrefix(String prefix) {
		List<S3Object> s3Objects = new ArrayList<>();
		try {
			// Retrieve objects with key prefix in parameter
			ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
				.bucket(this.s3config.getBucket())
				.prefix(prefix)
				.build();

			// As response is limited to max 1000 objects (cf maxKeys): use
			// listObjectsV2Paginator and ListObjectsV2Publisher to retrieve all
			// matching S3Objects
			this.s3AsyncClient.listObjectsV2Paginator(listObjectsV2Request)
				.subscribe(response -> s3Objects.addAll(response.contents()))
				.get();
		}
		catch (InterruptedException | ExecutionException e) {
			LOG.error("Issue when retrieving S3 objects from prefix %s:%s".formatted(prefix, e.getMessage()));
			Thread.currentThread().interrupt();
			throw new TechnicalException(
					"Issue when retrieving S3 objects from prefix %s:%s".formatted(prefix, e.getMessage()));
		}
		return s3Objects;
	}

	/**
	 * Retrieve S3 object from the key in parameter
	 * @param key Key ot retrieve
	 * @return InputStream corresponding to the S3 object
	 */
	public InputStream retrieveS3ObjectInputStream(String key) {
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(this.s3config.getBucket())
				.key(key)
				.build();
			return this.s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBlockingInputStream())
				.get();
		}
		catch (ExecutionException | InterruptedException e) {
			LOG.error("Issue when retrieving S3 object from key %s:%s".formatted(key, e.getMessage()));
			Thread.currentThread().interrupt();
			throw new TechnicalException(
					"Issue when retrieving S3 object from key %s:%s".formatted(key, e.getMessage()));
		}
	}

}
