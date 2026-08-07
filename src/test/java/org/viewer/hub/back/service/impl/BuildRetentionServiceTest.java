/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.viewer.hub.back.service.S3Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class BuildRetentionServiceTest {

	private static final String BASE_PATH = "resources/packages/weasis/i18n";

	private static final String VERSION = "4.0.0-SNAPSHOT";

	private static final String CURRENT_BUILD = "11111111-1111-1111-1111-111111111111";

	private static final String OBSOLETE_BUILD = "22222222-2222-2222-2222-222222222222";

	private final S3Service s3Service = Mockito.mock(S3Service.class);

	private BuildRetentionService buildRetentionService;

	private void setUp(Duration gracePeriod) {
		this.buildRetentionService = new BuildRetentionService(this.s3Service);
		ReflectionTestUtils.setField(this.buildRetentionService, "packagePath", "resources/packages/weasis/package");
		ReflectionTestUtils.setField(this.buildRetentionService, "i18nPath", BASE_PATH);
		ReflectionTestUtils.setField(this.buildRetentionService, "enabled", true);
		ReflectionTestUtils.setField(this.buildRetentionService, "gracePeriod", gracePeriod);
		Mockito.when(this.s3Service.deleteS3Objects(anyString()))
			.thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
	}

	private void mockPointer(Instant lastModified) {
		Mockito.when(this.s3Service.retrieveS3ObjectsLastModifiedFromPrefix(BASE_PATH))
			.thenReturn(Map.of("%s/%s/%s".formatted(BASE_PATH, VERSION, "current"), lastModified,
					"%s/%s/%s/messages.properties".formatted(BASE_PATH, VERSION, CURRENT_BUILD), lastModified,
					"%s/%s/%s/messages.properties".formatted(BASE_PATH, VERSION, OBSOLETE_BUILD),
					lastModified.minus(Duration.ofDays(10))));
		String pointerKey = "%s/%s/%s".formatted(BASE_PATH, VERSION, "current");
		Mockito.when(this.s3Service.doesS3KeyExists(pointerKey)).thenReturn(true);
		Mockito.when(this.s3Service.retrieveS3Object(pointerKey))
			.thenAnswer(invocation -> new ByteArrayInputStream(CURRENT_BUILD.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void when_lastPublishOlderThanGrace_should_deleteObsoleteBuildKeepCurrent() {
		this.setUp(Duration.ofHours(24));
		// Last flip 48h ago -> past the 24h grace period
		this.mockPointer(Instant.now().minus(Duration.ofHours(48)));

		this.buildRetentionService.cleanObsoleteBuildsForBasePath(BASE_PATH);

		// Obsolete build deleted, current build kept
		Mockito.verify(this.s3Service)
			.deleteS3Objects(eq("%s/%s/%s/".formatted(BASE_PATH, VERSION, OBSOLETE_BUILD)));
		Mockito.verify(this.s3Service, Mockito.never())
			.deleteS3Objects(eq("%s/%s/%s/".formatted(BASE_PATH, VERSION, CURRENT_BUILD)));
	}

	@Test
	void when_lastPublishWithinGrace_should_keepAllBuilds() {
		this.setUp(Duration.ofHours(24));
		// Last flip 1h ago -> within the 24h grace period (transition window)
		this.mockPointer(Instant.now().minus(Duration.ofHours(1)));

		this.buildRetentionService.cleanObsoleteBuildsForBasePath(BASE_PATH);

		Mockito.verify(this.s3Service, Mockito.never()).deleteS3Objects(anyString());
	}

	@Test
	void when_legacyVersionWithoutBuildStamp_should_deleteNothing() {
		this.setUp(Duration.ofHours(24));
		// Legacy layout: files directly under <version>/ (bundle/, conf/), no build id, no pointer
		Mockito.when(this.s3Service.retrieveS3ObjectsLastModifiedFromPrefix(BASE_PATH))
			.thenReturn(Map.of("%s/%s/bundle/felix.jar".formatted(BASE_PATH, VERSION),
					Instant.now().minus(Duration.ofDays(10)), "%s/%s/conf/config.properties".formatted(BASE_PATH, VERSION),
					Instant.now().minus(Duration.ofDays(10))));

		this.buildRetentionService.cleanObsoleteBuildsForBasePath(BASE_PATH);

		Mockito.verify(this.s3Service, Mockito.never()).deleteS3Objects(anyString());
		// Never resolves a build id since there is nothing build-stamped to clean
		Mockito.verify(this.s3Service, Mockito.never()).retrieveS3Object(any());
	}

}