/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.service;

import org.weasis.manager.back.entity.PackageVersionEntity;
import org.weasis.manager.back.model.SearchCriteria;
import org.weasis.manager.back.model.manifest.Manifest;

import java.util.Collection;

/**
 * Use to manage cache
 */
public interface CacheService {

	// ================= Manifest ====================

	/**
	 * Determine the key depending on the search criteria in parameters
	 * @param searchCriteria Search criteria
	 * @return key built
	 */
	String constructManifestKeyDependingOnSearchParameters(SearchCriteria searchCriteria);

	/**
	 * Add the key and manifest in the cache if key is absent
	 * @param key Key to retrieve the manifest
	 * @param manifest Manifest to add in the cache
	 * @return Manifest set in the cache
	 */
	Manifest putManifestIfAbsent(String key, Manifest manifest);

	/**
	 * Add the key and manifest in the cache. If already existing replace it.
	 * @param key Key to retrieve the manifest
	 * @param manifest Manifest to add in the cache
	 * @return Manifest set in the cache
	 */
	Manifest putManifest(String key, Manifest manifest);

	/**
	 * Retrieve the manifest depending on the key in parameter
	 * @param key Key to retrieve
	 * @return Manifest found
	 */
	Manifest getManifest(String key);

	/**
	 * Remove the manifest corresponding to the key
	 * @param key Key to retrieve
	 */
	void removeManifest(String key);

	/**
	 * Get all manifests
	 * @return list of manifest found
	 */
	Collection<Manifest> getAllManifest();

	/**
	 * Remove all manifests
	 */
	void removeAllManifest();

	// ================= Package Version ====================

	/**
	 * Add the key and package version in the cache
	 * @param versionRequested Key which represents the version requested
	 * @param versionToUse Version to add in the cache
	 * @return Package version set in the cache
	 */
	PackageVersionEntity putPackageVersion(String versionRequested, PackageVersionEntity versionToUse);

	/**
	 * Retrieve the package version depending on the package version requested in
	 * parameter
	 * @param versionRequested Key to retrieve
	 * @return package version found
	 */
	PackageVersionEntity getPackageVersion(String versionRequested);

	/**
	 * Remove the package version corresponding to the key
	 * @param versionRequested Key to retrieve
	 */
	void removePackageVersion(String versionRequested);

	/**
	 * Get all package version
	 * @return list of package version found
	 */
	Collection<PackageVersionEntity> getAllPackageVersion();

	/**
	 * Remove all package version
	 */
	void removeAllPackageVersion();

}
