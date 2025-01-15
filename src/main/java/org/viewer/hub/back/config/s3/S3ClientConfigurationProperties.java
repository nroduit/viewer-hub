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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@Component
public class S3ClientConfigurationProperties {

	@Value("${spring.cloud.aws.region.static}")
	private Region region;

	@Value("${spring.cloud.aws.s3.endpoint}")
	private URI endpoint;

	@Value("${spring.cloud.aws.credentials.access-key}")
	private String accessKeyId;

	@Value("${spring.cloud.aws.credentials.secret-key}")
	private String secretAccessKey;

	@Value("${spring.cloud.aws.s3.bucket.name}")
	private String bucket;

	private int multipartMinPartSize;

	private int maxConcurrency;

	public S3ClientConfigurationProperties() {
		this.multipartMinPartSize = 5242880;
		this.maxConcurrency = 64;
	}

	public Region getRegion() {
		return this.region;
	}

	public URI getEndpoint() {
		return this.endpoint;
	}

	public String getAccessKeyId() {
		return this.accessKeyId;
	}

	public String getSecretAccessKey() {
		return this.secretAccessKey;
	}

	public String getBucket() {
		return this.bucket;
	}

	public int getMultipartMinPartSize() {
		return this.multipartMinPartSize;
	}

	public int getMaxConcurrency() {
		return this.maxConcurrency;
	}

	public void setRegion(final Region region) {
		this.region = region;
	}

	public void setEndpoint(final URI endpoint) {
		this.endpoint = endpoint;
	}

	public void setAccessKeyId(final String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	public void setSecretAccessKey(final String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	public void setBucket(final String bucket) {
		this.bucket = bucket;
	}

	public void setMultipartMinPartSize(final int multipartMinPartSize) {
		this.multipartMinPartSize = multipartMinPartSize;
	}

	public void setMaxConcurrency(final int maxConcurrency) {
		this.maxConcurrency = maxConcurrency;
	}

}
