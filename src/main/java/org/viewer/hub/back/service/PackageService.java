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

package org.viewer.hub.back.service;

import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Service managing package versions
 */
public interface PackageService {

	/**
	 * Used to refresh in the cache: - the mapping between the installed Weasis package
	 * versions in Viewer Hub and the version to used when launching Weasis. The mapping
	 * will take the latest available version installed in the Viewer-Hub according to the
	 * minimal release version - update the table package_version if a version is missing
	 * compared to the available packages
	 */
	void refreshAvailablePackageVersion() throws IOException, URISyntaxException;

	/**
	 * Check if we should use json parsing depending on the version number. Starting
	 * version 4.2.0 of Weasis, configuration files are in json instead of properties.
	 * @param packageVersionEntity Package version entity to evaluate
	 * @return true if json parsing should be used
	 */
	boolean shouldUseJsonParsing(PackageVersionEntity packageVersionEntity);

	/**
	 * Retrieve in the cache the package version to use for a Weasis version requested
	 * @param weasisVersionRequested Weasis version requested
	 * @param qualifier Qualifier depending on groups of the user/host
	 * @return package version to use
	 */
	PackageVersionEntity retrieveAvailablePackageVersionToUse(String weasisVersionRequested, String qualifier);

	/**
	 * Depending on the overrideConfig in parameter, remove the folder or config
	 * properties file and update the db for package version and override config table
	 * @param overrideConfigEntity OverrideConfigEntity to delete
	 */
	void deleteResourcePackageVersion(OverrideConfigEntity overrideConfigEntity);

	/**
	 * Upload the zip file containing the different bundles/package of the version to add
	 * @param fileData InputStream corresponding to the zip file to extract
	 * @param versionToUpload Version to upload
	 */
	void handlePackageVersionToUpload(InputStream fileData, String versionToUpload);

	/**
	 * Determine the version to upload: check if the zip file to import has the
	 * appropriate version format in the property weasis.version of the file
	 * config.properties and if yes return the version filled otherwise null
	 * @param fileData InputStream corresponding to the zip file to extract
	 * @return null if incorrect format of the version, otherwise return the version to
	 * upload
	 */
	String checkWeasisNativeVersionToUpload(InputStream fileData);

	/**
	 * Retrieve package version
	 * @param packageVersionId Id to retrieve
	 * @return PackageVersionEntity found
	 */
	PackageVersionEntity retrievePackageVersion(Long packageVersionId);

	/**
	 * Check if the version number already exists and has already been uploaded on the
	 * server
	 * @param version Version to evaluate
	 */
	boolean doesVersionNumberAlreadyExists(String version);

	/**
	 * Retrieve the list of PackageVersion depending on the version number in parameter
	 * @param version Version to evaluate
	 * @return List of PackageVersion found
	 */
	List<PackageVersionEntity> retrievePackageVersionByVersionNumber(String version);

	/**
	 * Check if the import of the package version is coherent: version already installed
	 * or incoherent version compatibility file
	 * @param fileData Import to check
	 * @return true if the import is coherent
	 */
	boolean isImportCoherent(InputStream fileData);

}
