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

package org.weasis.manager.back.service;

import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Service used to manage S3 requests
 */
public interface S3Service {

	/**
	 * Check if S3 key in parameter exists
	 * @param key Key to check
	 * @return true if the s3 key exists
	 */
	boolean doesS3KeyExists(String key);

	/**
	 * Retrieve S3 keys from prefix
	 * @param prefix Prefix to evaluate
	 * @return Set of S3 keys
	 */
	Set<String> retrieveS3KeysFromPrefix(String prefix);

	/**
	 * Retrieve InputStream of a S3 object from the key in parameter
	 * @param key Key to evaluate
	 * @return InputStream of the S3 object found
	 */
	InputStream retrieveS3Object(String key);

	/**
	 * Upload an object in S3
	 * @param inputStream InputStream to upload
	 * @param key Key used for upload
	 * @return CompletableFuture
	 */
	CompletableFuture<PutObjectResponse> uploadObjectInS3(ByteArrayInputStream inputStream, String key);

	/**
	 * Copy an S3 object from one key to another
	 * @param sourceKey Source
	 * @param destinationKey Destination
	 * @return CompletableFuture
	 */
	CompletableFuture<CompletedCopy> copyS3ObjectFromTo(String sourceKey, String destinationKey);

	/**
	 * Delete S3 objects from a prefix in parameter
	 * @param prefixKey Prefix key to evaluate
	 * @return CompletableFuture
	 */
	CompletableFuture<DeleteObjectsResponse> deleteS3Objects(String prefixKey);

}
