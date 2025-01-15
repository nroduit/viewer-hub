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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class S3ClientConfiguration {

	private final S3ClientConfigurationProperties s3ClientConfigurationProperties;

	@Autowired
	public S3ClientConfiguration(S3ClientConfigurationProperties s3ClientConfigurationProperties) {
		this.s3ClientConfigurationProperties = s3ClientConfigurationProperties;
	}

	@Bean
	@RefreshScope
	public S3AsyncClient s3AsyncClient() {
		SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
			.writeTimeout(Duration.ZERO)
			.maxConcurrency(this.s3ClientConfigurationProperties.getMaxConcurrency())
			.build();
		S3Configuration serviceConfiguration = S3Configuration.builder()
			.checksumValidationEnabled(false)
			.chunkedEncodingEnabled(true)
			.pathStyleAccessEnabled(true)
			.build();

		// TODO: utiliser le crt s3 client pour multipart upload/download
		// S3AsyncClientBuilder clientBuilder = S3AsyncClient.crtBuilder()
		S3AsyncClientBuilder clientBuilder = S3AsyncClient.builder()
			.httpClient(httpClient)
			.region(this.s3ClientConfigurationProperties.getRegion())
			.credentialsProvider(StaticCredentialsProvider
				.create(AwsBasicCredentials.create(this.s3ClientConfigurationProperties.getAccessKeyId(),
						this.s3ClientConfigurationProperties.getSecretAccessKey())))
			.serviceConfiguration(serviceConfiguration);
		if (this.s3ClientConfigurationProperties.getEndpoint() != null) {
			clientBuilder = clientBuilder.endpointOverride(this.s3ClientConfigurationProperties.getEndpoint());
		}
		return clientBuilder.build();
	}

}
