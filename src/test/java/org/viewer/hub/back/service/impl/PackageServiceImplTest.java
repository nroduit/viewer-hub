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

import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;
import org.viewer.hub.back.config.properties.EnvironmentOverrideProperties;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.model.version.MinimalReleaseVersion;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.repository.PackageVersionRepository;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.back.service.S3Service;
import org.viewer.hub.back.service.TargetService;
import org.viewer.hub.back.util.PackageUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;

@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

	@Mock
	private CacheService cacheService;

	@Mock
	private OverrideConfigService overrideConfigService;

	@Mock
	private TargetService targetService;

	@Mock
	private PackageVersionRepository packageVersionRepository;

	@Mock
	private LaunchConfigRepository launchConfigRepository;

	@Mock
	private S3Service s3Service;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private EnvironmentOverrideProperties environmentOverrideProperties;

	@InjectMocks
	private PackageServiceImpl packageService;

	@NotNull
	private static Set<String> buildAvailableWeasisPackageVersions() {
		return Set.of("4.0.3-TEST", "3.8.2-MGR", "4.0.2-TEST", "4.0.2-MGR", "3.8.2-TEST", "4.0.1", "4.1.0-MGR", "4.5.0",
				"4.5.0.1-MGR", "4.5.0.2-TEST");
	}

	@NotNull
	private static List<MinimalReleaseVersion> buildMinimalReleaseVersions() {
		return List.of(new MinimalReleaseVersion("3.6.0", "3.6.0", "2.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.6.1", "3.6.0", "2.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.6.2", "3.6.0", "2.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.7.0", "3.7.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.7.1", "3.7.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.8.0", "3.7.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.8.1", "3.7.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("3.8.2", "3.7.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.0.0", "4.0.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.0.1", "4.0.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.0.2", "4.0.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.0.3", "4.0.0", "3.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.1.0", "4.1.0", "4.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.5.0", "4.5.0", "4.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.5.0.1", "4.5.0", "4.0.0-SNAPSHOT"),
				new MinimalReleaseVersion("4.5.0.2", "4.5.0", "4.0.0-SNAPSHOT"));
	}

	@NotNull
	private static Map<String, String> buildAvailablePackageVersionMappingToCompare() {
		Map<String, String> toCompare = new HashMap<>();
		toCompare.put("3.8.2-MGR", "3.8.2-MGR");
		toCompare.put("4.0.2-TEST", "4.0.3-TEST");
		toCompare.put("4.0.0-MGR", "4.0.2-MGR");
		toCompare.put("4.0.1-MGR", "4.0.2-MGR");
		toCompare.put("4.5.0.2-MGR", "4.5.0.1-MGR");
		toCompare.put("3.6.1-TEST", null);
		toCompare.put("3.7.1-TEST", "3.8.2-TEST");
		toCompare.put("3.7.0-MGR", "3.8.2-MGR");
		toCompare.put("3.6.0-NO_QUALIFIER", null);
		toCompare.put("4.5.0-NO_QUALIFIER", "4.5.0");
		toCompare.put("4.0.2-NO_QUALIFIER", "4.0.1");
		toCompare.put("4.0.3-NO_QUALIFIER", "4.0.1");
		toCompare.put("4.0.0-TEST", "4.0.3-TEST");
		toCompare.put("3.6.2-NO_QUALIFIER", null);
		toCompare.put("4.0.3-TEST", "4.0.3-TEST");
		toCompare.put("3.6.1-NO_QUALIFIER", null);
		toCompare.put("3.7.1-MGR", "3.8.2-MGR");
		toCompare.put("3.7.0-NO_QUALIFIER", null);
		toCompare.put("4.5.0.2-NO_QUALIFIER", "4.5.0");
		toCompare.put("4.5.0.1-NO_QUALIFIER", "4.5.0");
		toCompare.put("4.0.2-MGR", "4.0.2-MGR");
		toCompare.put("3.6.2-TEST", null);
		toCompare.put("3.8.2-TEST", "3.8.2-TEST");
		toCompare.put("4.1.0-TEST", null);
		toCompare.put("4.5.0-TEST", "4.5.0.2-TEST");
		toCompare.put("4.1.0-MGR", "4.1.0-MGR");
		toCompare.put("3.8.0-MGR", "3.8.2-MGR");
		toCompare.put("4.0.3-MGR", "4.0.2-MGR");
		toCompare.put("3.6.1-MGR", null);
		toCompare.put("3.7.1-NO_QUALIFIER", null);
		toCompare.put("4.5.0.2-TEST", "4.5.0.2-TEST");
		toCompare.put("3.8.1-NO_QUALIFIER", null);
		toCompare.put("4.1.0-NO_QUALIFIER", null);
		toCompare.put("4.0.1-TEST", "4.0.3-TEST");
		toCompare.put("3.6.0-MGR", null);
		toCompare.put("3.8.0-NO_QUALIFIER", null);
		toCompare.put("3.6.2-MGR", null);
		toCompare.put("3.8.1-MGR", "3.8.2-MGR");
		toCompare.put("3.8.1-TEST", "3.8.2-TEST");
		toCompare.put("4.5.0.1-MGR", "4.5.0.1-MGR");
		toCompare.put("4.0.1-NO_QUALIFIER", "4.0.1");
		toCompare.put("4.5.0-MGR", "4.5.0.1-MGR");
		toCompare.put("3.8.2-NO_QUALIFIER", null);
		toCompare.put("4.0.0-NO_QUALIFIER", "4.0.1");
		toCompare.put("3.6.0-TEST", null);
		toCompare.put("3.7.0-TEST", "3.8.2-TEST");
		toCompare.put("3.8.0-TEST", "3.8.2-TEST");
		toCompare.put("4.5.0.1-TEST", "4.5.0.2-TEST");
		return toCompare;
	}

	@Test
	void when_retrieveAvailableWeasisPackageVersions_should_retrieveFolderNamesInWeasisPackage()
			throws IOException, URISyntaxException {
		// Call method
		Set<String> availableWeasisPackageVersions = this.packageService
			.retrieveAvailableWeasisPackageVersions(Files.list(Paths.get(
					Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("weasis/package"))
						.toURI())));

		// Test results
		assertThat(availableWeasisPackageVersions.size()).isEqualTo(10);
		assertThat(availableWeasisPackageVersions.containsAll(buildAvailableWeasisPackageVersions())).isTrue();
	}

	@Test
	void when_retrievingMinimalVersions_should_deserializeFileContainingVersionsMapping() throws IOException {
		// Mock
		Mockito.when(this.s3Service.retrieveS3Object(any()))
			.thenReturn(new FileInputStream(ResourceUtils.getFile("classpath:weasis/mapping-minimal-version.json")));

		// Call method
		List<MinimalReleaseVersion> minimalReleaseVersions = this.packageService
			.retrieveS3MinimalReleaseVersions("key");

		// Test results
		MinimalReleaseVersion minimalReleaseVersionExample = new MinimalReleaseVersion();
		minimalReleaseVersionExample.setReleaseVersion("4.0.3");
		minimalReleaseVersionExample.setMinimalVersion("4.0.0");
		minimalReleaseVersionExample.setI18nVersion("3.0.0-SNAPSHOT");
		assertThat(minimalReleaseVersions.size()).isEqualTo(16);
		assertThat(minimalReleaseVersions.contains(minimalReleaseVersionExample)).isTrue();
	}

	@Test
	void when_determiningAvailablePackageVersionMapping_should_mapCoherentVersions() {

		// init data
		Set<String> availableWeasisPackageVersions = buildAvailableWeasisPackageVersions();
		List<MinimalReleaseVersion> minimalReleaseVersions = buildMinimalReleaseVersions();

		// To compare
		Map<String, String> toCompare = buildAvailablePackageVersionMappingToCompare();

		// Call method
		Map<String, String> availablePackageVersionMapping = this.packageService
			.determineAvailablePackageVersionMapping(availableWeasisPackageVersions, minimalReleaseVersions);

		// Test results
		assertThat(availablePackageVersionMapping.size()).isEqualTo(48);
		assertThat(availablePackageVersionMapping.equals(toCompare)).isTrue();
	}

	@Test
	void when_refreshingAvailablePackageVersionMapping_should_updateDbAndCache() throws FileNotFoundException {
		// Mock
		Mockito.when(this.s3Service.doesS3KeyExists(any())).thenReturn(true);
		ReflectionTestUtils.setField(this.packageService, "viewerHubResourcesPackagesWeasisPackagePath",
				"resources/packages/weasis/package");
		ReflectionTestUtils.setField(this.packageService, "viewerHubResourcesPackagesWeasisMappingMinimalVersionPath",
				"resources/packages/weasis/mapping-minimal-version.json");
		Mockito.when(this.s3Service.retrieveS3KeysFromPrefix(any()))
			.thenReturn(Set.of("resources/packages/weasis/package/4.1.0-QUALIFIER/test"));
		Mockito.when(this.s3Service.retrieveS3Object(any()))
			.thenReturn(new FileInputStream(ResourceUtils.getFile("classpath:weasis/mapping-minimal-version.json")));
		Mockito.when(this.overrideConfigService.existOverrideConfigWithVersionConfigTarget(any(), any(), any()))
			.thenReturn(true);

		// Call method
		this.packageService.refreshAvailablePackageVersion();

		// Test results
		Mockito.verify(this.packageVersionRepository, Mockito.atLeastOnce()).saveAll(any());
		Mockito.verify(this.overrideConfigService, Mockito.atLeastOnce()).saveAll(anySet());
		Mockito.verify(this.cacheService, Mockito.atLeastOnce()).removeAllPackageVersion();
		Mockito.verify(this.cacheService, Mockito.atLeast(1)).putPackageVersion(any(), any());
	}

	@Test
	void when_retrievingAvailablePackageVersionToUse_should_returnCorrectVersion() {
		// Mock
		PackageVersionEntity packageVersionEntity401 = new PackageVersionEntity();
		packageVersionEntity401.setVersionNumber("4.0.1");
		PackageVersionEntity packageVersionEntity402MGR = new PackageVersionEntity();
		packageVersionEntity402MGR.setVersionNumber("4.0.2");
		packageVersionEntity402MGR.setQualifier("-MGR");

		Mockito.when(this.cacheService.getPackageVersion(Mockito.eq("4.0.3-MGR")))
			.thenReturn(packageVersionEntity402MGR);
		Mockito.when(this.cacheService.getPackageVersion(Mockito.eq("4.0.2" + PackageUtil.NO_QUALIFIER)))
			.thenReturn(packageVersionEntity401);
		Mockito.when(this.cacheService.getPackageVersion(Mockito.eq("4.0.3" + PackageUtil.NO_QUALIFIER)))
			.thenReturn(packageVersionEntity401);
		Mockito.when(this.cacheService.getPackageVersion(Mockito.eq("4.0.2-MGR")))
			.thenReturn(packageVersionEntity402MGR);
		ReflectionTestUtils.setField(this.packageService, "defaultPackageVersionNumber", "4.0.2");

		// Call and test method
		PackageVersionEntity toTest = this.packageService.retrieveAvailablePackageVersionToUse("4.0.3", "-MGR");
		assertThat(toTest.getVersionNumber() + toTest.getQualifier()).isEqualTo("4.0.2-MGR");

		toTest = this.packageService.retrieveAvailablePackageVersionToUse(null, null);
		assertThat(toTest.getVersionNumber()).isEqualTo("4.0.1");
		assertThat(toTest.getQualifier()).isNull();

		toTest = this.packageService.retrieveAvailablePackageVersionToUse("4.0.3", null);
		assertThat(toTest.getVersionNumber()).isEqualTo("4.0.1");
		assertThat(toTest.getQualifier()).isNull();

		toTest = this.packageService.retrieveAvailablePackageVersionToUse(null, "-MGR");
		assertThat(toTest.getVersionNumber() + toTest.getQualifier()).isEqualTo("4.0.2-MGR");
	}

}
