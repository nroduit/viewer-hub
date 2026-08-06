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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.service.S3Service;
import org.viewer.hub.back.util.PackageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Garbage-collects obsolete build-stamped sub-directories.
 * <p>
 * Each package/i18n version is stored under immutable {@code <version>/<buildId>/...}
 * sub-directories and the active build is recorded in the {@code <version>/current} pointer. When a
 * version is re-uploaded, a new build id is published and the previous build becomes obsolete but is
 * kept so that clients pinned to it (their launch config still points at that build id) can finish
 * downloading. This service deletes those obsolete builds once a transition/grace period has elapsed
 * since the last publish.
 * <p>
 * The grace period is measured from the {@code current} pointer's last-modified instant (i.e. the
 * moment of the last pointer flip): every non-current build was superseded no later than that flip,
 * so once the flip itself is older than the grace period all obsolete builds are safe to remove -
 * regardless of when their individual files were written.
 */
@Service
@Slf4j
@RefreshScope
public class BuildRetentionService {

	/** buildId format used at upload time: {@link java.util.UUID#randomUUID()}. */
	private static final Pattern BUILD_ID_PATTERN = Pattern
		.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	@Value("${viewer-hub.resources-packages.weasis.package.path}")
	private String packagePath;

	@Value("${viewer-hub.resources-packages.weasis.i18n.path}")
	private String i18nPath;

	@Value("${viewer-hub.resources-packages.build-retention.enabled:true}")
	private boolean enabled;

	@Value("${viewer-hub.resources-packages.build-retention.grace-period:PT24H}")
	private Duration gracePeriod;

	private final S3Service s3Service;

	@Autowired
	public BuildRetentionService(final S3Service s3Service) {
		this.s3Service = s3Service;
	}

	/**
	 * Every 24h (after a 1h initial delay to avoid running during startup): remove obsolete builds
	 * of every package and i18n version.
	 */
	@Scheduled(fixedRate = 24L * 60 * 60 * 1000, initialDelay = 60L * 60 * 1000)
	public void cleanObsoleteBuilds() {
		if (!this.enabled) {
			LOG.debug("Build retention disabled, skipping obsolete builds cleanup");
			return;
		}
		this.cleanObsoleteBuildsForBasePath(this.packagePath);
		this.cleanObsoleteBuildsForBasePath(this.i18nPath);
	}

	/**
	 * Remove obsolete builds for every version found under the given base path.
	 * @param basePath Base S3 path (package or i18n)
	 */
	void cleanObsoleteBuildsForBasePath(String basePath) {
		if (basePath == null || basePath.isBlank()) {
			return;
		}
		Instant now = Instant.now();
		Map<String, Instant> objects = this.s3Service.retrieveS3ObjectsLastModifiedFromPrefix(basePath);

		// Group objects per version: last-modified of the <version>/current pointer, and the set of
		// build ids (immediate UUID sub-directories) present for the version.
		Map<String, Instant> pointerLastModifiedByVersion = new HashMap<>();
		Map<String, Set<String>> buildIdsByVersion = new HashMap<>();
		int prefixLength = basePath.length() + 1;
		for (Map.Entry<String, Instant> object : objects.entrySet()) {
			String key = object.getKey();
			if (key.length() <= prefixLength) {
				continue;
			}
			// <version>/<rest>
			String relative = key.substring(prefixLength);
			int firstSeparator = relative.indexOf('/');
			if (firstSeparator < 0) {
				// Object directly under the base path (not inside a version folder): ignore
				continue;
			}
			String version = relative.substring(0, firstSeparator);
			String rest = relative.substring(firstSeparator + 1);
			if (rest.equals(PackageUtil.CURRENT_BUILD_POINTER_FILE)) {
				pointerLastModifiedByVersion.put(version, object.getValue());
			}
			else {
				int secondSeparator = rest.indexOf('/');
				if (secondSeparator > 0) {
					String candidateBuildId = rest.substring(0, secondSeparator);
					// Only UUID sub-directories are considered builds: this leaves legacy top-level
					// content (bundle/, conf/, resources/, resources.zip, ...) untouched.
					if (BUILD_ID_PATTERN.matcher(candidateBuildId).matches()) {
						buildIdsByVersion.computeIfAbsent(version, v -> new HashSet<>()).add(candidateBuildId);
					}
				}
			}
		}

		buildIdsByVersion
			.forEach((version, buildIds) -> this.cleanObsoleteBuildsForVersion(basePath, version, buildIds,
					pointerLastModifiedByVersion.get(version), now));
	}

	/**
	 * Delete the obsolete builds of a single version, respecting the grace period.
	 * @param basePath Base S3 path
	 * @param version Version folder name
	 * @param buildIds Build ids present for the version
	 * @param pointerLastModified Last-modified of the &lt;version&gt;/current pointer (may be null)
	 * @param now Current instant
	 */
	private void cleanObsoleteBuildsForVersion(String basePath, String version, Set<String> buildIds,
			Instant pointerLastModified, Instant now) {
		if (pointerLastModified == null) {
			// No current pointer: do not risk deleting a build without knowing which one is active
			LOG.warn("Skipping build retention for {}/{}: no current pointer found", basePath, version);
			return;
		}
		// Transition window: keep every build until the last publish (pointer flip) is older than
		// the grace period, so clients pinned to the just-superseded build can finish downloading.
		if (Duration.between(pointerLastModified, now).compareTo(this.gracePeriod) < 0) {
			LOG.debug("Skipping build retention for {}/{}: last publish is within the grace period", basePath, version);
			return;
		}
		String currentBuildId = this.readCurrentBuildPointer(basePath, version);
		if (currentBuildId == null) {
			LOG.warn("Skipping build retention for {}/{}: current pointer unreadable", basePath, version);
			return;
		}
		for (String buildId : buildIds) {
			if (buildId.equals(currentBuildId)) {
				continue;
			}
			String obsoleteBuildPrefix = "%s/%s/%s/".formatted(basePath, version, buildId);
			LOG.info("Build retention: deleting obsolete build {}", obsoleteBuildPrefix);
			// Wait for the deletion so an error is surfaced and the cleanup stays sequential
			this.s3Service.deleteS3Objects(obsoleteBuildPrefix).join();
		}
	}

	/**
	 * Read the active build id from the &lt;version&gt;/current pointer object.
	 * @param basePath Base S3 path
	 * @param version Version folder name
	 * @return the active build id, or null when it cannot be read
	 */
	private String readCurrentBuildPointer(String basePath, String version) {
		String pointerKey = "%s/%s/%s".formatted(basePath, version, PackageUtil.CURRENT_BUILD_POINTER_FILE);
		if (!this.s3Service.doesS3KeyExists(pointerKey)) {
			return null;
		}
		try (InputStream is = this.s3Service.retrieveS3Object(pointerKey)) {
			if (is == null) {
				return null;
			}
			String buildId = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
			return buildId.isBlank() ? null : buildId;
		}
		catch (IOException e) {
			LOG.error("Issue when reading build pointer {}:{}", pointerKey, e.getMessage());
			return null;
		}
	}

}