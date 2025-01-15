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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.viewer.hub.back.constant.CacheName;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.service.CacheService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CacheServiceImplTest {

	// Mocks

	@Mock
	private Cache manifestCacheMock;

	@Mock
	private Cache packageVersionCacheMock;

	@Mock
	private RedisCacheManager redisCacheManagerMock;

	@Mock
	private RedisTemplate<String, Manifest> manifestRedisTemplateMock;

	@Mock
	private RedisTemplate<String, PackageVersionEntity> packageVersionRedisTemplateMock;

	private ValueWrapper manifestValueWrapper;

	private ValueWrapper packageVersionValueWrapper;

	private Manifest manifest;

	private PackageVersionEntity packageVersionEntity;

	// Service
	private CacheService cacheService;

	@BeforeEach
	public void setUp() {
		// Init data
		this.manifest = new Manifest();
		this.manifest.setUid("uid");
		this.manifestValueWrapper = new SimpleValueWrapper(this.manifest);

		this.packageVersionEntity = new PackageVersionEntity();
		this.packageVersionEntity.setVersionNumber("4.1.0");
		this.packageVersionValueWrapper = new SimpleValueWrapper(this.packageVersionEntity);

		// Mock cache manager
		when(this.redisCacheManagerMock.getCache(CacheName.MANIFEST)).thenReturn(this.manifestCacheMock);
		when(this.redisCacheManagerMock.getCache(CacheName.PACKAGE_VERSION)).thenReturn(this.packageVersionCacheMock);

		this.cacheService = new CacheServiceImpl(this.redisCacheManagerMock, this.manifestRedisTemplateMock,
				this.packageVersionRedisTemplateMock);
	}

	// ============================= Manifest ================================

	@Test
	void when_searchParametersChange_then_keyBuiltShouldBeDifferent() {
		// Build keys
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setArchive(new LinkedHashSet<>(List.of("A", "B")));
		String key = this.cacheService.constructManifestKeyDependingOnSearchParameters(weasisSearchCriteria);
		weasisSearchCriteria.getArchive().add("C");
		String keyToCompare = this.cacheService.constructManifestKeyDependingOnSearchParameters(weasisSearchCriteria);

		// Test keys
		assertNotEquals(key, keyToCompare);
	}

	@Test
	void given_newEntryInCache_when_addingInManifestCache_then_shouldReturnManifestAdded(){
		// Given
		when(this.manifestCacheMock.putIfAbsent(anyString(), any(Manifest.class))).thenReturn(this.manifestValueWrapper);

		// When
		Manifest manifestReturned = this.cacheService.putManifestIfAbsent("hash", this.manifest);

		// Then
		assertThat(manifestReturned).isNotNull();
		assertThat(manifestReturned.getUid()).isEqualTo("uid");
	}

	@Test
	void given_existingEntryInManifestCache_when_retrievingThisEntry_then_shouldReturnExistingManifest(){
		// Given
		when(this.manifestCacheMock.get(anyString())).thenReturn(this.manifestValueWrapper);

		// When
		Manifest manifestReturned = this.cacheService.getManifest("hash");

		// Then
		assertThat(manifestReturned).isNotNull();
		assertThat(manifestReturned.getUid()).isEqualTo("uid");
	}

	@Test
	void given_existingEntryInManifestCache_when_removingThisEntry_then_shouldRemoveEntry() {
		// Given
		doCallRealMethod().when(this.manifestCacheMock).evictIfPresent(any());

		// When
		this.cacheService.removeManifest("hash");

		// Then
		verify(this.manifestCacheMock, times(1)).evictIfPresent("hash");
	}

	@Test
	void given_existingEntriesInManifestCache_when_retrievingTheseEntries_then_shouldReturnExistingManifests(){
		// Given
		when(this.manifestCacheMock.get(anyString())).thenReturn(this.manifestValueWrapper);
		when(this.manifestRedisTemplateMock.keys(anyString())).thenReturn(Set.of(CacheName.MANIFEST+"::"+"hash"));

		// When
		Collection<Manifest> manifestsReturned = this.cacheService.getAllManifest();

		// Then
		assertNotNull(manifestsReturned);
		assertThat(manifestsReturned).hasSize(1);
		assertTrue(manifestsReturned.contains(this.manifest));
	}

	@Test
	void given_existingEntryInManifestCache_when_removingAllEntries_then_shouldRemoveEntries() {
		// Given
		doCallRealMethod().when(this.manifestCacheMock).evictIfPresent(any());
		when(this.manifestRedisTemplateMock.keys(anyString())).thenReturn(Set.of(CacheName.MANIFEST + "::" + "hash"));

		// When
		this.cacheService.removeAllManifest();

		// Then
		verify(this.manifestCacheMock, times(1)).evictIfPresent("hash");

	}

	// ============================= Package Version =========================

	@Test
	void given_newEntryInCache_when_addingInPackageVersionCache_then_shouldReturnPackageVersionAdded(){
		// Given
		when(this.packageVersionCacheMock.putIfAbsent(anyString(), any(PackageVersionEntity.class))).thenReturn(this.packageVersionValueWrapper);

		// When
		PackageVersionEntity packageVersionReturned = this.cacheService.putPackageVersion("versionRequested", this.packageVersionEntity);

		// Then
		assertThat(packageVersionReturned).isNotNull();
		assertThat(packageVersionReturned.getVersionNumber()).isEqualTo("4.1.0");
	}

	@Test
	void given_existingEntryInPackageVersionCache_when_retrievingThisEntry_then_shouldReturnExistingPackageVersion(){
		// Given
		when(this.packageVersionCacheMock.get(anyString())).thenReturn(this.packageVersionValueWrapper);

		// When
		PackageVersionEntity packageVersionReturned = this.cacheService.getPackageVersion("versionRequested");

		// Then
		assertThat(packageVersionReturned).isNotNull();
		assertThat(packageVersionReturned.getVersionNumber()).isEqualTo("4.1.0");
	}

	@Test
	void given_existingEntryInPackageVersionCache_when_removingThisEntry_then_shouldRemoveEntry() {
		// Given
		doCallRealMethod().when(this.packageVersionCacheMock).evictIfPresent(any());

		// When
		this.cacheService.removePackageVersion("versionRequested");

		// Then
		verify(this.packageVersionCacheMock, times(1)).evictIfPresent("versionRequested");
	}

	@Test
	void given_existingEntriesInPackageVersionCache_when_retrievingTheseEntries_then_shouldReturnExistingPackageVersions(){
		// Given
		when(this.packageVersionCacheMock.get(anyString())).thenReturn(this.packageVersionValueWrapper);
		when(this.packageVersionRedisTemplateMock.keys(anyString())).thenReturn(Set.of(CacheName.PACKAGE_VERSION+"::"+"versionRequested"));

		// When
		Collection<PackageVersionEntity> allPackageVersion = this.cacheService.getAllPackageVersion();

		// Then
		assertNotNull(allPackageVersion);
		assertThat(allPackageVersion).hasSize(1);
		assertTrue(allPackageVersion.contains(this.packageVersionEntity));
	}

	@Test
	void given_existingEntryInPackageVersionCache_when_removingAllEntries_then_shouldRemoveEntries() {
		// Given
		doCallRealMethod().when(this.packageVersionCacheMock).evictIfPresent(any());
		when(this.packageVersionRedisTemplateMock.keys(anyString()))
			.thenReturn(Set.of(CacheName.PACKAGE_VERSION + "::" + "versionRequested"));

		// When
		this.cacheService.removeAllPackageVersion();

		// Then
		verify(this.packageVersionCacheMock, times(1)).evictIfPresent("versionRequested");
	}

}
