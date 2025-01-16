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

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.constant.CacheName;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.service.CacheService;

import java.util.Collection;
import java.util.Objects;

@Service
public class CacheServiceImpl implements CacheService {

	private static final String KEY_SEPARATOR = "::";

	private final Cache manifestCache;

	private final Cache packageVersionCache;

	private final RedisTemplate<String, Manifest> manifestRedisTemplate;

	private final RedisTemplate<String, PackageVersionEntity> packageVersionRedisTemplate;

	private final RedisCacheManager redisCacheManager;

	private final String prefixKeySearchManifestCache;

	private final String prefixKeySearchPackageVersionCache;

	private final String patternSearchAllKeysManifestCache;

	private final String patternSearchAllKeysPackageVersionCache;

	@Autowired
	public CacheServiceImpl(RedisCacheManager redisCacheManager, RedisTemplate<String, Manifest> manifestRedisTemplate,
			RedisTemplate<String, PackageVersionEntity> packageVersionRedisTemplate) {
		this.redisCacheManager = redisCacheManager;
		this.manifestCache = redisCacheManager.getCache(CacheName.MANIFEST);
		this.packageVersionCache = redisCacheManager.getCache(CacheName.PACKAGE_VERSION);
		this.manifestRedisTemplate = manifestRedisTemplate;
		this.packageVersionRedisTemplate = packageVersionRedisTemplate;
		this.prefixKeySearchManifestCache = "%s%s".formatted(CacheName.MANIFEST, KEY_SEPARATOR);
		this.prefixKeySearchPackageVersionCache = "%s%s".formatted(CacheName.PACKAGE_VERSION, KEY_SEPARATOR);
		this.patternSearchAllKeysManifestCache = "%s*".formatted(this.prefixKeySearchManifestCache);
		this.patternSearchAllKeysPackageVersionCache = "%s*".formatted(this.prefixKeySearchPackageVersionCache);
	}

	// ================= Manifest ====================

	@Override
	public String constructManifestKeyDependingOnSearchParameters(@Valid SearchCriteria searchCriteria) {
		return String.valueOf(searchCriteria.hashCode());
	}

	@Override
	public Manifest putManifestIfAbsent(String key, Manifest manifest) {
		ValueWrapper valueFromCache = this.manifestCache.putIfAbsent(key, manifest);
		return valueFromCache != null ? (Manifest) valueFromCache.get() : null;
	}

	@Override
	public Manifest putManifest(String key, Manifest manifest) {
		this.manifestCache.put(key, manifest);
		return this.getManifest(key);
	}

	@Override
	public Manifest getManifest(String key) {
		ValueWrapper valueFromCache = this.manifestCache.get(key);
		return valueFromCache != null ? (Manifest) valueFromCache.get() : null;
	}

	@Override
	public void removeManifest(String key) {
		this.manifestCache.evictIfPresent(key);
	}

	@Override
	public Collection<Manifest> getAllManifest() {
		return Objects.requireNonNull(this.manifestRedisTemplate.keys(this.patternSearchAllKeysManifestCache))
			.stream()
			.filter(Objects::nonNull)
			.filter(c -> c.length() > this.prefixKeySearchManifestCache.length())
			.map(k -> {
				ValueWrapper keyValue = this.manifestCache.get(k.substring(this.prefixKeySearchManifestCache.length()));
				return keyValue != null ? (Manifest) keyValue.get() : null;
			})
			.toList();
	}

	@Override
	public void removeAllManifest() {
		Objects.requireNonNull(this.manifestRedisTemplate.keys(this.patternSearchAllKeysManifestCache))
			.stream()
			.filter(Objects::nonNull)
			.filter(c -> c.length() > this.prefixKeySearchManifestCache.length())
			.forEach(k -> this.removeManifest(k.substring(this.prefixKeySearchManifestCache.length())));
	}

	// ================= Package Version ====================

	@Override
	public PackageVersionEntity putPackageVersion(String versionRequested, PackageVersionEntity versionToUse) {
		ValueWrapper valueFromCache = this.packageVersionCache.putIfAbsent(versionRequested, versionToUse);
		return valueFromCache != null ? (PackageVersionEntity) valueFromCache.get() : null;
	}

	@Override
	public PackageVersionEntity getPackageVersion(String versionRequested) {
		ValueWrapper valueFromCache = this.packageVersionCache.get(versionRequested);
		return valueFromCache != null ? (PackageVersionEntity) valueFromCache.get() : null;
	}

	@Override
	public void removePackageVersion(String versionRequested) {
		this.packageVersionCache.evictIfPresent(versionRequested);
	}

	@Override
	public Collection<PackageVersionEntity> getAllPackageVersion() {
		return Objects
			.requireNonNull(this.packageVersionRedisTemplate.keys(this.patternSearchAllKeysPackageVersionCache))
			.stream()
			.filter(Objects::nonNull)
			.filter(c -> c.length() > this.prefixKeySearchPackageVersionCache.length())
			.map(k -> {
				ValueWrapper keyValue = this.packageVersionCache
					.get(k.substring(this.prefixKeySearchPackageVersionCache.length()));
				return keyValue != null ? (PackageVersionEntity) keyValue.get() : null;
			})
			.toList();
	}

	@Override
	public void removeAllPackageVersion() {
		Objects.requireNonNull(this.packageVersionRedisTemplate.keys(this.patternSearchAllKeysPackageVersionCache))
			.stream()
			.filter(Objects::nonNull)
			.filter(c -> c.length() > this.prefixKeySearchPackageVersionCache.length())
			.forEach(k -> this.removePackageVersion(k.substring(this.prefixKeySearchPackageVersionCache.length())));
	}

}
