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

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.config.properties.EnvironmentOverrideProperties;
import org.viewer.hub.back.constant.PropertiesFileName;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntityPK;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.entity.WeasisPropertyEntity;
import org.viewer.hub.back.enums.LaunchConfigType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.enums.WeasisProperties;
import org.viewer.hub.back.model.version.MinimalReleaseVersion;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.repository.PackageVersionRepository;
import org.viewer.hub.back.service.CacheService;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.back.service.PackageService;
import org.viewer.hub.back.service.S3Service;
import org.viewer.hub.back.service.TargetService;
import org.viewer.hub.back.util.JacksonUtil;
import org.viewer.hub.back.util.PackageUtil;
import org.viewer.hub.back.util.PathUrlUtil;
import org.viewer.hub.back.util.StringUtil;
import org.viewer.hub.back.util.VersionUtil;
import org.viewer.hub.front.views.weasis.bundle.override.component.RefreshPackageGridEvent;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.viewer.hub.back.constant.PropertiesFileName.BIN_DIST_WEASIS_PATH;
import static org.viewer.hub.back.constant.PropertiesFileName.BIN_DIST_WEASIS_RESOURCES_PATH;
import static org.viewer.hub.back.constant.PropertiesFileName.CONF_FOLDER_NAME;
import static org.viewer.hub.back.constant.PropertiesFileName.EXT_CONFIG_PROPERTIES_FILENAME;
import static org.viewer.hub.back.constant.PropertiesFileName.RESOURCES_ZIP_FILE_NAME;
import static org.viewer.hub.back.constant.PropertiesFileName.VERSION_COMPATIBILITY_FILE_NAME;

@Service
@Slf4j
@RefreshScope
public class PackageServiceImpl implements PackageService {

	public static final String VERIFY_WEASIS_NATIVE_VERSION_REGEX = "^\\d+\\.\\d+\\.\\d+(\\.\\d+)?(-[A-Za-z0-9]+)?$";

	public static final String STARTING_VERSION_TO_USE_JSON_PARSING = "4.2.0";

	@Value("${weasis.package.version.default.number}")
	private String defaultPackageVersionNumber;

	@Value("${weasis.package.version.default.qualifier}")
	private String defaultPackageVersionQualifier;

	@Value("${viewer-hub.resources-packages.weasis.package.path}")
	private String viewerHubResourcesPackagesWeasisPackagePath;

	@Value("${viewer-hub.resources-packages.weasis.mapping-minimal-version.path}")
	private String viewerHubResourcesPackagesWeasisMappingMinimalVersionPath;

	// Services
	private final CacheService cacheService;

	private final PackageVersionRepository packageVersionRepository;

	private final OverrideConfigService overrideConfigService;

	private final LaunchConfigRepository launchConfigRepository;

	private final TargetService targetService;

	private final S3Service s3Service;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final EnvironmentOverrideProperties environmentOverrideProperties;

	/**
	 * Autowired constructor
	 */
	@Autowired
	public PackageServiceImpl(final CacheService cacheService, final PackageVersionRepository packageVersionRepository,
			final OverrideConfigService overrideConfigService, final LaunchConfigRepository launchConfigRepository,
			final TargetService targetService, final S3Service s3Service,
			final ApplicationEventPublisher applicationEventPublisher,
			final EnvironmentOverrideProperties environmentOverrideProperties) {
		this.cacheService = cacheService;
		this.packageVersionRepository = packageVersionRepository;
		this.overrideConfigService = overrideConfigService;
		this.launchConfigRepository = launchConfigRepository;
		this.targetService = targetService;
		this.s3Service = s3Service;
		this.applicationEventPublisher = applicationEventPublisher;
		this.environmentOverrideProperties = environmentOverrideProperties;
	}

	@Override
	// Every 24h
	@Scheduled(fixedRate = 24 * 60 * 60 * 1000)
	public void refreshAvailablePackageVersion() {
		// Check if json file containing the mapping of minimal versions is present
		if (this.doesMappingMinimalVersionFileExists()) {

			// Retrieve list of available Weasis package versions
			Set<String> availableWeasisPackageVersions = this.retrieveS3AvailableWeasisPackageVersions();

			// Read mapping minimal version from existing releases
			List<MinimalReleaseVersion> minimalReleaseVersions = this
				.retrieveS3MinimalReleaseVersions(this.viewerHubResourcesPackagesWeasisMappingMinimalVersionPath);

			// Check if a package version is missing in DB and add it if necessary
			this.refreshPackageVersionInDb(availableWeasisPackageVersions, minimalReleaseVersions);

			// Refresh cache
			this.refreshPackageVersionCache(this.determineAvailablePackageVersionMapping(availableWeasisPackageVersions,
					minimalReleaseVersions));

			// Load configurations properties in db if not already present
			this.loadS3ConfigurationPropertiesInDb(availableWeasisPackageVersions);
		}
	}

	@Override
	public void handlePackageVersionToUpload(InputStream fileData, String versionToUpload) {
		try (fileData) {
			if (versionToUpload != null && !versionToUpload.isBlank()) {
				// Each upload lands in its own immutable build-stamped sub-directory
				// (<version>/<buildId>/...) so a re-uploaded (e.g. SNAPSHOT) version
				// never
				// overwrites files a client may currently be downloading.
				String buildId = UUID.randomUUID().toString();
				Path outDir = Paths.get(this.viewerHubResourcesPackagesWeasisPackagePath)
					.resolve(versionToUpload)
					.resolve(buildId);

				// Upload version package in S3, then - strictly after every file is
				// durably
				// written - zip the resources folder, replace the
				// mapping-minimal-version.json
				// if a more recent one is provided, flip the <version>/current pointer to
				// this
				// build, and only then refresh the cache/db and the grid. Chaining the
				// stages
				// (instead of racing two independent allOf callbacks on the same futures
				// list)
				// guarantees the version never becomes visible/launchable until all of
				// its files
				// - including the generated resources.zip - are present in S3, so a
				// client can
				// never fetch a half-written package folder.

				// 1. Upload version package files in S3
				List<CompletableFuture<PutObjectResponse>> completableFutures = this.uploadVersionInS3(fileData,
						outDir);

				CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
					// 2. Zip the resources folder at the root of the package folder
					.thenCompose(unused -> {
						resetInputStream(fileData);
						return this.zipResourcesFolderToRootPackageFolder(fileData, outDir);
					})
					// 3. Replace mapping-minimal-version.json if a more recent one was
					// imported
					.thenCompose(unused -> this.compareReplaceMappingMinimalVersion(outDir))
					// 4. Atomic publish: flip the <version>/current pointer to this build
					// id
					.thenCompose(unused -> this.writeCurrentBuildPointer(versionToUpload, buildId))
					// 5. Everything is durably in S3: refresh cache/db and the front grid
					.whenComplete((result, throwable) -> {
						if (throwable == null) {
							this.refreshAvailablePackageVersion();
							this.applicationEventPublisher.publishEvent(new RefreshPackageGridEvent());
						}
						else {
							throw new TechnicalException(
									"Issue when uploading package version in S3, at least one future didn't end well:%s"
										.formatted(throwable.getMessage()));
						}
					});
			}
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when uploading package version:%s".formatted(e.getMessage()));
		}
	}

	@Override
	public String checkWeasisNativeVersionToUpload(InputStream fileData) {
		// Check if we should use json or properties files parsing for the import
		// depending on the presence of the base.json in the zip file
		boolean useJsonParsing = this.shouldUseJsonParsing(fileData);

		// Reset the input stream in order to start again the browsing of the zip file
		// to import
		resetInputStream(fileData);

		// Retrieve and check the version to import
		String versionToReturn = useJsonParsing ? this.checkWeasisNativeVersionToUploadJsonFile(fileData)
				: checkWeasisNativeVersionToUploadPropertiesFile(fileData);

		// Reset the input stream in order to start again the browsing of the zip file
		// to import
		resetInputStream(fileData);

		return versionToReturn;
	}

	@Override
	public boolean shouldUseJsonParsing(PackageVersionEntity packageVersionEntity) {
		if (packageVersionEntity != null && packageVersionEntity.getVersionNumber() != null) {
			ComparableVersion startingVersionToUseJson = new ComparableVersion(STARTING_VERSION_TO_USE_JSON_PARSING);
			ComparableVersion versionToEvaluate = new ComparableVersion(packageVersionEntity.getVersionNumber());
			return versionToEvaluate.compareTo(startingVersionToUseJson) >= 0;
		}
		return false;
	}

	@Override
	public PackageVersionEntity retrievePackageVersion(Long packageVersionId) {
		PackageVersionEntity toReturn = null;
		if (packageVersionId != null) {
			Optional<PackageVersionEntity> packageVersionEntityOptional = this.packageVersionRepository
				.findById(packageVersionId);
			if (packageVersionEntityOptional.isPresent()) {
				toReturn = packageVersionEntityOptional.get();
			}
		}
		return toReturn;
	}

	@Override
	public PackageVersionEntity retrieveAvailablePackageVersionToUse(String weasisVersionRequested, String qualifier) {
		PackageVersionEntity availablePackageVersionToUse;
		// Retrieve default qualifier (handle no qualifier case)
		String defaultQualifierToUse = this.defaultPackageVersionQualifier == null
				|| this.defaultPackageVersionQualifier.isBlank() ? PackageUtil.NO_QUALIFIER
						: this.defaultPackageVersionQualifier;

		if (qualifier == null && weasisVersionRequested == null) {
			availablePackageVersionToUse = this.cacheService
				.getPackageVersion("%s%s".formatted(this.defaultPackageVersionNumber, defaultQualifierToUse));
		}
		else if (qualifier == null && weasisVersionRequested != null) {
			availablePackageVersionToUse = this.cacheService
				.getPackageVersion("%s%s".formatted(weasisVersionRequested, defaultQualifierToUse));
		}
		else if (qualifier != null && weasisVersionRequested == null) {
			availablePackageVersionToUse = this.cacheService
				.getPackageVersion("%s%s".formatted(this.defaultPackageVersionNumber, qualifier));
		}
		else {
			availablePackageVersionToUse = this.cacheService
				.getPackageVersion("%s%s".formatted(weasisVersionRequested, qualifier));
		}
		return availablePackageVersionToUse;
	}

	@Override
	public void deleteResourcePackageVersion(OverrideConfigEntity overrideConfigEntity) {
		if (overrideConfigEntity != null && overrideConfigEntity.getPackageVersion() != null
				&& overrideConfigEntity.getLaunchConfig() != null && overrideConfigEntity.getTarget() != null) {
			// Update OverrideConfig table
			this.deleteResourceInDbOverrideConfig(overrideConfigEntity);

			// Update PackageVersion table
			this.deleteResourceInDbPackageVersion(overrideConfigEntity);

			// S3
			this.deleteResourcePackageVersionInS3(overrideConfigEntity).whenComplete((result, throwable) -> {
				if (throwable == null) {
					// Refresh available package versions
					this.refreshAvailablePackageVersion();
				}
				else {
					throw new TechnicalException(
							"Issue when deleting resources files in S3:%s".formatted(throwable.getMessage()));
				}
			});

		}
	}

	@Override
	public boolean doesVersionNumberAlreadyExists(String version) {
		return version != null && !this.packageVersionRepository
			.findByVersionNumber(version.contains(StringUtil.HYPHEN) ? version.split(StringUtil.HYPHEN)[0] : version)
			.isEmpty();
	}

	@Override
	public List<PackageVersionEntity> retrievePackageVersionByVersionNumber(String version) {
		List<PackageVersionEntity> packageVersionEntities = new ArrayList<>();
		if (version != null) {
			packageVersionEntities = this.packageVersionRepository.findByVersionNumber(
					version.contains(StringUtil.HYPHEN) ? version.split(StringUtil.HYPHEN)[0] : version);
		}
		return packageVersionEntities;
	}

	@Override
	public boolean isImportCoherent(InputStream fileData) {
		boolean isImportCoherent;

		// Retrieve the version to import
		String versionToImport = checkWeasisNativeVersionToUpload(fileData);

		// Case version is already installed on the server
		isImportCoherent = isImportCoherentVersionNotAlreadyInstalledOnServer(versionToImport);

		// Incoherent version compatibility file
		isImportCoherent = isImportCoherent
				&& isImportCoherentVersionCompatibilityFileCoherent(fileData, versionToImport);

		return isImportCoherent;
	}

	/**
	 * If the compatibility file has to be imported, check if the version compatibility is
	 * coherent
	 * @param fileData File to check
	 * @param versionToImport Version currently being uploaded
	 * @return true if the version compatibility is coherent
	 */
	private boolean isImportCoherentVersionCompatibilityFileCoherent(InputStream fileData, String versionToImport) {
		boolean isImportCoherentVersionCompatibilityFileCoherent;

		// Retrieve the version compatibility file in the zip import
		List<MinimalReleaseVersion> minimalReleaseVersionsFromImport = retrieveMinimalVersionsFromImport(fileData);

		// Check that no version declared in the compatibility file is greater than the
		// version currently being uploaded
		isImportCoherentVersionCompatibilityFileCoherent = isImportCoherentVersionCompatibilityNoVersionGreaterThanVersionToImport(
				versionToImport, minimalReleaseVersionsFromImport);

		// if (shouldReplaceMappingMinimalVersion(mappingMinimalVersionFilePath)){
		if (shouldReplaceMappingMinimalVersion(null, minimalReleaseVersionsFromImport)) {

			// Check that if there is a version with 4 digits, the base version with 3
			// digits is declared
			assert minimalReleaseVersionsFromImport != null;
			isImportCoherentVersionCompatibilityFileCoherent = isImportCoherentVersionCompatibilityFileCoherent
					&& isImportCoherentVersionCompatibilityBaseVersion(minimalReleaseVersionsFromImport);

			// Case it is a replacement of the file
			if (this.doesMappingMinimalVersionFileExists()) {
				// Check that all the previous versions declared are presents in the new
				// compatibility file
				isImportCoherentVersionCompatibilityFileCoherent = isImportCoherentVersionCompatibilityFileCoherent
						&& isImportCoherentVersionCompatibilityPreviousVersionsExist(minimalReleaseVersionsFromImport);
			}
		}

		return isImportCoherentVersionCompatibilityFileCoherent;
	}

	/**
	 * Check that no version declared in the version compatibility file to import is
	 * greater than the version currently being uploaded
	 * @param versionToImport Version currently being uploaded
	 * @param minimalReleaseVersionsFromImport MinimalReleaseVersions from the import
	 * @return true if no version declared in the compatibility file is greater than the
	 * version being uploaded
	 */
	private boolean isImportCoherentVersionCompatibilityNoVersionGreaterThanVersionToImport(String versionToImport,
			List<MinimalReleaseVersion> minimalReleaseVersionsFromImport) {
		if (StringUtils.isBlank(versionToImport) || minimalReleaseVersionsFromImport == null) {
			return true;
		}

		// Remove the qualifier (ex: -MGR) from the version to import in order to only
		// compare the numeric part of the version
		String versionToImportWithoutQualifier = versionToImport.contains(StringUtil.HYPHEN)
				? versionToImport.split(StringUtil.HYPHEN)[0] : versionToImport;
		ComparableVersion versionToImportComparable = new ComparableVersion(versionToImportWithoutQualifier);

		return minimalReleaseVersionsFromImport.stream()
			.filter(Objects::nonNull)
			.map(MinimalReleaseVersion::getReleaseVersion)
			.filter(Objects::nonNull)
			// A release version with 4 digits (hotfix) is considered part of its 3
			// digits base version family for the comparison
			.map(releaseVersion -> VersionUtil.countDigitsGroups(releaseVersion) == 4
					? VersionUtil.extract3GroupsDigitsOf4GroupsDigitsVersion(releaseVersion) : releaseVersion)
			.noneMatch(
					releaseVersion -> new ComparableVersion(releaseVersion).compareTo(versionToImportComparable) > 0);
	}

	/**
	 * Check that all the previous versions declared are presents in the new compatibility
	 * file
	 * @param minimalReleaseVersionsFromImport MinimalReleaseVersions from import
	 * @return true if all the previous versions declared are presents in the new
	 * compatibility file
	 */
	private boolean isImportCoherentVersionCompatibilityPreviousVersionsExist(
			List<MinimalReleaseVersion> minimalReleaseVersionsFromImport) {
		// Existing version compatibility file from S3
		List<MinimalReleaseVersion> existingMinimalReleaseVersions = this
			.retrieveS3MinimalReleaseVersions(this.viewerHubResourcesPackagesWeasisMappingMinimalVersionPath);

		// Check that all the previous versions declared are presents in the new
		// compatibility file
		return new HashSet<>(minimalReleaseVersionsFromImport).containsAll(existingMinimalReleaseVersions);
	}

	/**
	 * Retrieve the version compatibility file from the file to import
	 * @param fileData File to import
	 * @return List<MinimalReleaseVersion> found
	 */
	private List<MinimalReleaseVersion> retrieveMinimalVersionsFromImport(InputStream fileData) {

		// Reset the input stream in order to start again the browsing of the zip file
		// to import
		resetInputStream(fileData);

		try (ZipInputStream zis = new ZipInputStream(fileData)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory() && Objects.equals(ze.getName(), PropertiesFileName.VERSION_COMPATIBILITY_PATH)) {
					// Retrieve the file version compatibility file in the zip
					return JacksonUtil.deserializeMinimalReleaseVersionsFromInputStream(zis);
				}
			}
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when checking version of the zip file to import (using json file):%s"
				.formatted(e.getMessage()));
		}
		finally {
			// Reset the input stream in order to start again the browsing of the zip file
			// to import
			resetInputStream(fileData);
		}
		return null;
	}

	/**
	 * Check that if there is a release version with 4 digits, the base version with 3
	 * digits is declared
	 * @param minimalReleaseVersionsFromImport List<MinimalReleaseVersion> to evaluate
	 * @return true if the check is coherent
	 */
	private boolean isImportCoherentVersionCompatibilityBaseVersion(
			List<MinimalReleaseVersion> minimalReleaseVersionsFromImport) {
		return minimalReleaseVersionsFromImport.stream()
			.filter(Objects::nonNull)
			.map(MinimalReleaseVersion::getReleaseVersion)
			.filter(version -> VersionUtil.countDigitsGroups(version) == 4)
			.allMatch(version4GroupsDigits -> minimalReleaseVersionsFromImport.stream()
				.anyMatch(mr -> Objects.equals(mr.getReleaseVersion(),
						VersionUtil.extract3GroupsDigitsOf4GroupsDigitsVersion(version4GroupsDigits))));
	}

	/**
	 * Check if the version is not already installed on the server
	 * @param versionToImport Version to import
	 * @return true if the version is not already installed on the server
	 */
	private boolean isImportCoherentVersionNotAlreadyInstalledOnServer(String versionToImport) {
		boolean isImportCoherentVersionNotAlreadyInstalledOnServer = false;

		// Check if a packageVersionEntity is already present in the database
		if (StringUtils.isNotBlank(versionToImport)) {
			isImportCoherentVersionNotAlreadyInstalledOnServer = versionToImport.contains(StringUtil.HYPHEN)
					? packageVersionRepository
						.findByVersionNumberAndQualifier(versionToImport.split(StringUtil.HYPHEN)[0],
								"%s%s".formatted(StringUtil.HYPHEN, versionToImport.split(StringUtil.HYPHEN)[1]))
						.isEmpty()
					: packageVersionRepository.findByVersionNumber(versionToImport) == null;
		}

		return isImportCoherentVersionNotAlreadyInstalledOnServer;
	}

	/**
	 * When all uploads have been done: - Handle replacement of file
	 * mapping-minimal-version.json - Refresh cache and db
	 * @param completableFutures future to wait for
	 * @param outDir Output path
	 */
	private void handleReplacementOfMappingMinimalVersionAndRefresh(
			List<CompletableFuture<PutObjectResponse>> completableFutures, Path outDir) {
		CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
			.whenComplete((result, throwable) -> {
				if (throwable == null) {
					// Check if mapping-minimal-version.json should be overridden:
					// if yes replace it with the new version at the root level of
					// the package to import
					this.compareReplaceMappingMinimalVersion(outDir).whenComplete((resultMin, comparaisonThrowable) -> {
						// If no issue when processing comparison
						if (comparaisonThrowable == null) {
							// Update db and cache based on new files added in
							// package
							this.refreshAvailablePackageVersion();
							// Send event to the front in order to refresh the
							// grid
							this.applicationEventPublisher.publishEvent(new RefreshPackageGridEvent());
						}
					});
				}
				else {
					throw new TechnicalException(
							"Issue when uploading files in S3, at least one future didn't end well:%s"
								.formatted(throwable.getMessage()));
				}
			});
	}

	/**
	 * Manage zip of the resources folder when all future are terminated
	 * @param fileData InputStream
	 * @param completableFutures Futures to wait for
	 * @param outDir Output path
	 */
	private void handleZipResourceFolder(InputStream fileData,
			List<CompletableFuture<PutObjectResponse>> completableFutures, Path outDir) {
		CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
			.whenComplete((result, throwable) -> {
				if (throwable == null) {
					// Reset the stream
					resetInputStream(fileData);

					// Zip resources folder and set the zipped file at the root
					// level of the package folder
					completableFutures.add(this.zipResourcesFolderToRootPackageFolder(fileData, outDir));
				}
				else {
					throw new TechnicalException(
							"Issue when uploading files in S3, at least one future didn't end well:%s"
								.formatted(throwable.getMessage()));
				}
			});
	}

	/**
	 * Retrieve in S3 available Weasis package version
	 * @return Set of versions available
	 */
	private Set<String> retrieveS3AvailableWeasisPackageVersions() {
		return this.s3Service.retrieveS3KeysFromPrefix(this.viewerHubResourcesPackagesWeasisPackagePath)
			.stream()
			.filter(Objects::nonNull)
			.map(key -> {
				// Transform resources/packages/weasis/package/4.1.0-QUALIFIER/... en
				// 4.1.0-QUALIFIER/....
				String versionFolderKeys = key.substring(this.viewerHubResourcesPackagesWeasisPackagePath.length() + 1);
				// Transform 4.1.0-QUALIFIER/... en 4.1.0-QUALIFIER
				return versionFolderKeys.substring(0, versionFolderKeys.indexOf("/"));
			})
			.collect(Collectors.toSet());
	}

	/**
	 * Check if mapping-minimal-version.json should be overridden: if yes replace it with
	 * the new version at the root level of the package to import
	 * @param outDir Output directory
	 * @return CompletableFuture
	 */
	private CompletableFuture<CompletedCopy> compareReplaceMappingMinimalVersion(Path outDir) {
		// File to import
		String toImportFilePath = outDir.resolve(CONF_FOLDER_NAME).resolve(VERSION_COMPATIBILITY_FILE_NAME).toString();

		if (shouldReplaceMappingMinimalVersion(toImportFilePath, null)) {
			// Replace file
			return this.s3Service.copyS3ObjectFromTo(toImportFilePath,
					this.viewerHubResourcesPackagesWeasisMappingMinimalVersionPath);
		}

		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Check if the mapping minimal version should be replaced
	 * @param toImportFilePath mapping minimal version to import
	 * @return true if the mapping minimal version file should be replaced
	 */
	private boolean shouldReplaceMappingMinimalVersion(String toImportFilePath,
			List<MinimalReleaseVersion> minimalReleaseVersionsToImport) {
		boolean shouldReplaceMappingMinimalVersion = false;

		// Common method to not duplicate code
		// It is either the import path or List<MinimalReleaseVersion> but not both
		if (toImportFilePath != null && minimalReleaseVersionsToImport != null) {
			throw new TechnicalException(
					"shouldReplaceMappingMinimalVersion should be used either with the import path or List<MinimalReleaseVersion> but not both");
		}

		// Check if file mapping-minimal-version.json already exists
		if (this.doesMappingMinimalVersionFileExists()) {
			// If yes compare the existing file with the file to import
			// Read mapping minimal version from file to import and get max release
			// version
			ComparableVersion maxVersionToImport = toImportFilePath != null
					? this.retrieveMaxReleaseVersionFromS3(toImportFilePath)
					: this.retrieveMaxReleaseVersion(minimalReleaseVersionsToImport);

			// Read mapping minimal version from existing file and get max release version
			ComparableVersion maxVersionExisting = this
				.retrieveMaxReleaseVersionFromS3(this.viewerHubResourcesPackagesWeasisMappingMinimalVersionPath);

			// If max release version is the file to import: replace the previous file
			if (maxVersionToImport != null && maxVersionExisting != null
					&& maxVersionToImport.compareTo(maxVersionExisting) > 0) {
				shouldReplaceMappingMinimalVersion = true;
			}
		}
		else {
			// Copy directly the file in the resources package folder
			shouldReplaceMappingMinimalVersion = true;
		}

		return shouldReplaceMappingMinimalVersion;
	}

	/**
	 * Retrieve max release version from file path in parameter in S3
	 * @param mappingMinimalVersionFilePath Path of the file mapping minimal version
	 * @return Comparable version
	 */
	@Nullable
	private ComparableVersion retrieveMaxReleaseVersionFromS3(String mappingMinimalVersionFilePath) {
		return retrieveMaxReleaseVersion(this.retrieveS3MinimalReleaseVersions(mappingMinimalVersionFilePath));
	}

	/**
	 * Retrieve max release version from List<MinimalReleaseVersion> in parameter
	 * @param minimalReleaseVersions minimalReleaseVersions to evaluate
	 * @return Comparable version
	 */
	@Nullable
	private ComparableVersion retrieveMaxReleaseVersion(List<MinimalReleaseVersion> minimalReleaseVersions) {
		return minimalReleaseVersions.stream()
			.map(MinimalReleaseVersion::getReleaseVersion)
			.map(ComparableVersion::new)
			.max(Comparator.naturalOrder())
			.orElse(null);
	}

	/**
	 * Zip resources folder from input zip file and set the zipped file at the root of the
	 * package to import
	 */
	private CompletableFuture<PutObjectResponse> zipResourcesFolderToRootPackageFolder(InputStream zipFileInputStream,
			Path outDir) {
		// Path key where the zip generated will be uploaded in S3
		Path zipFileOutPath = outDir.resolve(RESOURCES_ZIP_FILE_NAME);
		// Object to upload in S3
		byte[] zipFile;

		// Build the zip output stream
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				// Process the input zip file and write in the zip output stream
				processInputZipFileAndWriteInResourcesZipOutputStream(zipFileInputStream, outDir, zos);
			}
			// Retrieve the file generated
			zipFile = baos.toByteArray();
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when zipping resources folder:%s".formatted(e.getMessage()));
		}
		// Upload the zip created in S3
		if (zipFile.length != 0) {
			return this.s3Service.uploadObjectInS3(new ByteArrayInputStream(zipFile), zipFileOutPath.toString());
		}
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Process the input zip file and write in resources zip output stream
	 * @param zipFileInputStream Input stream
	 * @param outDir Output path
	 * @param zos Zip output stream
	 * @throws IOException issue when processing zip file
	 */
	private static void processInputZipFileAndWriteInResourcesZipOutputStream(InputStream zipFileInputStream,
			Path outDir, ZipOutputStream zos) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(zipFileInputStream)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (ze.getName().contains(BIN_DIST_WEASIS_RESOURCES_PATH)
						&& !Objects.equals(ze.getName(), BIN_DIST_WEASIS_RESOURCES_PATH)) {
					// Remove bin-dist/weasis/resources/ from destination path
					Path filePath = outDir.resolve(ze.getName().substring(BIN_DIST_WEASIS_RESOURCES_PATH.length()));
					if (!ze.isDirectory()) {
						// Copy file from original zip to new zipped resources file
						copyZipInputToZipOutput(outDir, zos, zis, filePath);
					}
				}
			}
		}
	}

	/**
	 * Copy the zip entry to zip output stream
	 * @param outDir Output path
	 * @param zos ZipOutputStream
	 * @param zis ZipInputStream
	 * @param filePath Path of the input zip entry
	 */
	private static void copyZipInputToZipOutput(Path outDir, ZipOutputStream zos, ZipInputStream zis, Path filePath) {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			// Extract zip file
			zis.transferTo(byteArrayOutputStream);
			try (InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
				// Copy to zip output stream
				zos.putNextEntry(new ZipEntry(
						PathUrlUtil.pathWithS3Separator(Paths.get(PathUrlUtil.pathWithS3Separator(outDir.toString()))
							.relativize(filePath)
							.toString())));
				zos.write(inputStream.readAllBytes());
				zos.closeEntry();
			}
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when copying the zip entry to zip output stream:%s".formatted(e.getMessage()));
		}
	}

	@Nullable
	private String checkWeasisNativeVersionToUploadJsonFile(InputStream fileData) {
		try (ZipInputStream zis = new ZipInputStream(fileData)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory()
						&& Objects.equals(ze.getName(), PropertiesFileName.BIN_DIST_WEASIS_CONF_BASE_JSON_FILE_PATH)) {
					// Retrieve the file base.json in the zip
					OverrideConfigEntity overrideConfigEntity = JacksonUtil.deserializeJsonOverrideConfigEntity(zis);

					// Retrieve version of weasis (property weasis.version) and check if
					// the format is correct
					return retrieveAndCheckWeasisNativeVersion(overrideConfigEntity);
				}
			}
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when checking version of the zip file to import (using json file):%s"
				.formatted(e.getMessage()));
		}
		return null;
	}

	@Nullable
	private static String checkWeasisNativeVersionToUploadPropertiesFile(InputStream fileData) {
		try (ZipInputStream zis = new ZipInputStream(fileData)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory() && Objects.equals(ze.getName(),
						PropertiesFileName.BIN_DIST_WEASIS_CONF_CONFIG_PROPERTIES_FILE_PATH)) {
					// Retrieve the file config.properties in the zip
					OverrideConfigEntity overrideConfigEntity = JacksonUtil
						.deserializePropertiesOverrideConfigEntity(zis);

					// Retrieve version of weasis (property weasis.version) and check if
					// the format is correct
					return retrieveAndCheckWeasisNativeVersion(overrideConfigEntity);
				}
			}
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when checking version of the zip file to import (using properties file):%s"
						.formatted(e.getMessage()));
		}
		return null;
	}

	@Nullable
	private static String retrieveAndCheckWeasisNativeVersion(OverrideConfigEntity overrideConfigEntity) {
		WeasisPropertyEntity weasisPropertyEntity = overrideConfigEntity.getWeasisPropertyEntities()
			.stream()
			.filter(p -> Objects.equals(p.getCode(), "weasis.version"))
			.findFirst()
			.orElse(null);
		if (weasisPropertyEntity != null) {
			return weasisPropertyEntity.getValue() != null && !weasisPropertyEntity.getValue().isBlank()
					&& weasisPropertyEntity.getValue().matches(VERIFY_WEASIS_NATIVE_VERSION_REGEX)
							? weasisPropertyEntity.getValue() : null;
		}
		return null;
	}

	/**
	 * Check if we should use json or properties files parsing for the import depending on
	 * the presence of the base.json in the zip file
	 * @param fileData InputStream to evaluate
	 * @return true if base.json is present in the zip stream
	 */
	private boolean shouldUseJsonParsing(InputStream fileData) {
		try (ZipInputStream zis = new ZipInputStream(fileData)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory()
						&& Objects.equals(ze.getName(), PropertiesFileName.BIN_DIST_WEASIS_CONF_BASE_JSON_FILE_PATH)) {
					return true;
				}
			}
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when checking if json files should be used to import properties:%s"
				.formatted(e.getMessage()));
		}
		return false;
	}

	/**
	 * Reset the input stream in order to start again the browsing of the stream
	 * @param inputStream To reset
	 */
	private static void resetInputStream(InputStream inputStream) {
		try {
			inputStream.reset();
		}
		catch (IOException e) {
			throw new TechnicalException("Issue during reset of the inputStream:%s".formatted(e.getMessage()));
		}
	}

	/**
	 * Create an outputStream in order to duplicate InputStream
	 * @param inputStream InputStream
	 * @return ByteArrayOutputStream
	 */
	@NotNull
	private static ByteArrayOutputStream copyInputStreamToOutputStream(InputStream inputStream) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			inputStream.transferTo(baos);
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when copying InputStream:" + e.getMessage());
		}
		return baos;
	}

	/**
	 * Upload package files in S3
	 * @param fileData Files
	 * @param outDir Output directory
	 * @return List of CompletableFuture generated
	 */
	private List<CompletableFuture<PutObjectResponse>> uploadVersionInS3(InputStream fileData, Path outDir) {
		List<CompletableFuture<PutObjectResponse>> futures = new ArrayList<>();
		try (ZipInputStream zis = new ZipInputStream(fileData)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (ze.getName().contains(BIN_DIST_WEASIS_PATH)
						&& !Objects.equals(ze.getName(), BIN_DIST_WEASIS_PATH)) {
					// Remove bin-dist/weasis/ from destination path
					Path filePath = outDir.resolve(ze.getName().substring(BIN_DIST_WEASIS_PATH.length()));
					if (!ze.isDirectory()) {
						// Copy file from zip to s3
						CompletableFuture<PutObjectResponse> completableFuture = this.extractZippedFileToS3(zis,
								filePath);
						if (completableFuture != null) {
							// add in list to wait all the futures to be processed
							futures.add(completableFuture);
						}
					}
				}
			}
			return futures;
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when uploading package version:%s".formatted(e.getMessage()));
		}
	}

	/**
	 * Copy file from zip to S3
	 * @param zis ZipInputStream
	 * @param path Path
	 * @return CompletableFuture
	 */
	private CompletableFuture<PutObjectResponse> extractZippedFileToS3(ZipInputStream zis, Path path) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			// Extract zip file
			zis.transferTo(baos);

			// Upload object in S3
			return this.s3Service.uploadObjectInS3(new ByteArrayInputStream(baos.toByteArray()), path.toString());
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when extracting zip file:%s".formatted(e.getMessage()));
		}
	}

	/**
	 * Check if json file containing the mapping of minimal versions is present.
	 * @return true if the file is present
	 */
	private boolean doesMappingMinimalVersionFileExists() {
		return this.s3Service.doesS3KeyExists(this.viewerHubResourcesPackagesWeasisMappingMinimalVersionPath);
	}

	@Override
	public void deleteMappingMinimalVersionFile() {
		this.s3Service.deleteS3Objects(this.viewerHubResourcesPackagesWeasisMappingMinimalVersionPath);
	}

	/**
	 * Delete package version in db only if launch config default and target default
	 * @param overrideConfigEntity OverrideConfig to evaluate
	 */
	private void deleteResourceInDbPackageVersion(OverrideConfigEntity overrideConfigEntity) {
		if (Objects.equals(overrideConfigEntity.getLaunchConfig().getName(), LaunchConfigType.DEFAULT.getCode())
				&& Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			this.packageVersionRepository.delete(overrideConfigEntity.getPackageVersion());
		}
	}

	/**
	 * Delete OverrideConfig entities: <br/>
	 * - if launch config default and target default: delete all override config entities
	 * corresponding to the package version of the override config entity selected <br/>
	 * - if target default : delete all override config entities corresponding to the
	 * package version and the launch config of the override config entity selected <br/>
	 * - otherwise delete the override config selected
	 * @param overrideConfigEntity to evaluate
	 */
	private void deleteResourceInDbOverrideConfig(OverrideConfigEntity overrideConfigEntity) {
		// Case delete all the OverrideConfig corresponding to this package version (ex:
		// 4.1.0-SNAPSHOT) if launch config default and group is default
		if (Objects.equals(overrideConfigEntity.getLaunchConfig().getName(), LaunchConfigType.DEFAULT.getCode())
				&& Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			this.overrideConfigService
				.deleteAllOverrideConfigEntitiesByPackageVersion(overrideConfigEntity.getPackageVersion());
		}
		else if (Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			// Case delete all the OverrideConfigs corresponding to this package version
			// and to the launch config (ex 3d..) of this overrideConfig for all targets
			this.overrideConfigService.deleteAllOverrideConfigEntitiesByPackageVersionAndLaunchConfig(
					overrideConfigEntity.getPackageVersion(), overrideConfigEntity.getLaunchConfig());
		}
		else {
			// Delete only the OverrideConfig selected
			this.overrideConfigService.deleteOverrideConfigEntity(overrideConfigEntity);
		}
	}

	/**
	 * Delete in the S3 the folder or config properties file
	 * @param overrideConfigEntity to evaluate
	 * @return CompletableFuture
	 */
	private CompletableFuture<DeleteObjectsResponse> deleteResourcePackageVersionInS3(
			OverrideConfigEntity overrideConfigEntity) {
		PackageVersionEntity packageVersion = overrideConfigEntity.getPackageVersion();

		// Case delete the entire package version folder (all its builds and its current
		// pointer) if launch config default and group is default
		if (Objects.equals(overrideConfigEntity.getLaunchConfig().getName(), LaunchConfigType.DEFAULT.getCode())
				&& Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			return this.s3Service.deleteS3Objects("%s/%s".formatted(this.viewerHubResourcesPackagesWeasisPackagePath,
					retrieveVersionFolderName(packageVersion)));
		}
		// Case delete only the config version selected (properties file) only if the
		// group is default: the configuration files of a version live in the folder of
		// the build currently published
		else if (Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			boolean shouldUseJsonParsing = this.shouldUseJsonParsing(packageVersion);
			return this.s3Service.deleteS3Objects("%s/%s%s%s".formatted(
					this.retrieveConfigFolderKey(retrieveVersionFolderName(packageVersion),
							packageVersion.getBuildId()),
					shouldUseJsonParsing ? StringUtil.EMPTY_STRING : PropertiesFileName.EXT_PATTERN_NAME,
					overrideConfigEntity.getLaunchConfig().getName(), shouldUseJsonParsing
							? PropertiesFileName.EXTENSION_JSON_FILE : PropertiesFileName.EXTENSION_PROPERTIES_FILE));
		}
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Retrieve the name of the S3 folder of a package version (ex: 4.9.0-QUALIFIER)
	 * @param packageVersion PackageVersionEntity to evaluate
	 * @return name of the folder of the version
	 */
	private static String retrieveVersionFolderName(PackageVersionEntity packageVersion) {
		return "%s%s".formatted(packageVersion.getVersionNumber(),
				packageVersion.getQualifier() == null ? StringUtil.EMPTY_STRING : packageVersion.getQualifier());
	}

	/**
	 * Retrieve the S3 key of the folder containing the configuration files of a package
	 * version. Config files live inside the immutable build-stamped sub-directory
	 * (&lt;version&gt;/&lt;buildId&gt;/conf), except for legacy versions (null build id)
	 * whose files are still at the top level (&lt;version&gt;/conf).
	 * @param version Name of the folder of the version (ex: 4.9.0-QUALIFIER)
	 * @param buildId Build id currently published for this version, null for a legacy
	 * version
	 * @return S3 key of the conf folder of the version
	 */
	private String retrieveConfigFolderKey(String version, String buildId) {
		Path versionDir = buildId == null || buildId.isBlank()
				? Paths.get(this.viewerHubResourcesPackagesWeasisPackagePath, version)
				: Paths.get(this.viewerHubResourcesPackagesWeasisPackagePath, version, buildId);
		// Note: PATH_CONF_FOLDER starts with a separator, so it has to be joined with
		// Paths.get (Path.resolve would consider it as an absolute path and return only
		// "/conf")
		return PathUrlUtil
			.pathWithS3Separator(Paths.get(versionDir.toString(), PropertiesFileName.PATH_CONF_FOLDER).toString());
	}

	/**
	 * Check if a package version is missing in DB and add it if necessary
	 * @param availableWeasisPackageVersions versions to evaluate
	 * @param minimalReleaseVersions minimal release versions
	 */
	private void refreshPackageVersionInDb(Set<String> availableWeasisPackageVersions,
			List<MinimalReleaseVersion> minimalReleaseVersions) {
		// Retrieve all package versions in db
		List<PackageVersionEntity> existingVersionsInDb = this.packageVersionRepository.findAll();

		List<PackageVersionEntity> entitiesToSave = new ArrayList<>();
		for (String version : availableWeasisPackageVersions) {
			if (version == null) {
				continue;
			}
			// Split version folder name into version number / qualifier (qualifier keeps
			// its
			// leading hyphen, ex: 4.9.0-QUALIFIER -> 4.9.0 + "-QUALIFIER")
			String versionNumber = version.contains(StringUtil.HYPHEN)
					? version.substring(0, version.indexOf(StringUtil.HYPHEN)) : version;
			String qualifier = version.contains(StringUtil.HYPHEN)
					? version.substring(version.indexOf(StringUtil.HYPHEN)) : null;

			// Active build id for this version (null for a legacy version without
			// pointer)
			String buildId = this.readCurrentBuildPointer(version);

			PackageVersionEntity existing = existingVersionsInDb.stream()
				.filter(e -> Objects.equals(e.getVersionNumber(), versionNumber)
						&& Objects.equals(e.getQualifier(), qualifier))
				.findFirst()
				.orElse(null);

			if (existing == null) {
				// New version: create the catalog row
				PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
				packageVersionEntity.setVersionNumber(versionNumber);
				packageVersionEntity.setQualifier(qualifier);
				packageVersionEntity
					.setI18nVersion(this.retrieveI18nFromVersionNumber(versionNumber, minimalReleaseVersions));
				packageVersionEntity.setDescription("Version %s".formatted(version));
				packageVersionEntity.setBuildId(buildId);
				entitiesToSave.add(packageVersionEntity);
			}
			else if (buildId != null && !Objects.equals(existing.getBuildId(), buildId)) {
				// Existing version re-uploaded with a new build: update the pinned build
				// id
				existing.setBuildId(buildId);
				entitiesToSave.add(existing);
			}
		}

		// Save the created/updated versions
		if (!entitiesToSave.isEmpty()) {
			this.packageVersionRepository.saveAll(entitiesToSave);
		}
	}

	/**
	 * Retrieve i18n version from version number in the list of minimal release version
	 * @param versionNumber version number to evaluate
	 * @param minimalReleaseVersions list of minimal release versions
	 * @return the i18n version found for the version number requested
	 */
	private String retrieveI18nFromVersionNumber(String versionNumber,
			List<MinimalReleaseVersion> minimalReleaseVersions) {
		String i18nVersionFound = null;
		if (versionNumber != null) {
			MinimalReleaseVersion minimalReleaseVersionFound = minimalReleaseVersions.stream()
				.filter(Objects::nonNull)
				.filter(m -> Objects.equals(m.getReleaseVersion(), versionNumber))
				.findFirst()
				.orElse(null);
			i18nVersionFound = minimalReleaseVersionFound != null ? minimalReleaseVersionFound.getI18nVersion() : null;
		}
		return i18nVersionFound;
	}

	/**
	 * Retrieve directory names of the available versions of Weasis installed in
	 * Viewer-Hub
	 * @param streamPaths Path of the directories
	 * @return Names of the directories
	 */
	@NotNull
	Set<String> retrieveAvailableWeasisPackageVersions(Stream<Path> streamPaths) {
		return streamPaths.filter(Files::isDirectory)
			.map(Path::getFileName)
			.map(Path::toString)
			.collect(Collectors.toSet());
	}

	/**
	 * Load S3 configurations properties in db if not already present or if they have been
	 * generated from a previous build of the version (case of a version re-uploaded: the
	 * id of the package version does not change, only its build id does)
	 */
	private void loadS3ConfigurationPropertiesInDb(Set<String> availableWeasisPackageVersions) {
		// Retrieve default launch_config and target
		LaunchConfigEntity defaultLaunchConfig = this.launchConfigRepository
			.findOptionalByNameIgnoreCase(LaunchConfigType.DEFAULT.getCode())
			.orElse(null);
		TargetEntity defaultTarget = this.targetService.retrieveTargetByName(TargetType.DEFAULT.getCode());
		// Loop on folder paths
		availableWeasisPackageVersions.forEach(availableVersion -> {
			Set<OverrideConfigEntity> overrideConfigEntities = new HashSet<>();
			PackageVersionEntity packageVersionEntity = this.retrievePackageVersionEntity(availableVersion);

			// The catalog has just been refreshed: without the package version in db the
			// key of the override configs to persist cannot be built
			if (packageVersionEntity == null) {
				LOG.warn("No package version found in db for version {}: configuration properties are not loaded",
						availableVersion);
				return;
			}

			// Flag to know if we should use json parsing or properties parsing of
			// configuration files
			boolean useJsonParsing = this.shouldUseJsonParsing(packageVersionEntity);

			// Needed to be effectively final
			final OverrideConfigEntity[] defaultOverrideConfig = { null };
			// Config folder key for this version
			String buildId = packageVersionEntity.getBuildId();
			String configFolderKey = this.retrieveConfigFolderKey(availableVersion, buildId);

			// Default properties file key for this version
			String defaultConfigPropertiesFileKey = PathUrlUtil.pathWithS3Separator(
					Paths
						.get(configFolderKey,
								useJsonParsing ? PropertiesFileName.BASE_JSON_FILENAME
										: PropertiesFileName.CONFIG_PROPERTIES_FILENAME)
						.toString());

			// Check config folder exist and default config exists
			if (this.s3Service.doesS3KeyExists(configFolderKey)
					&& this.s3Service.doesS3KeyExists(defaultConfigPropertiesFileKey)) {
				// Find the default config file for the version to evaluate, extract
				// properties and build overrideConfigEntity to persist
				this.determineDefaultConfigurationFromS3ToPersist(defaultConfigPropertiesFileKey, packageVersionEntity,
						defaultLaunchConfig, defaultTarget, defaultOverrideConfig, overrideConfigEntities,
						useJsonParsing, buildId);

				// Browse content of folder in order to find other configs (with default
				// found above)
				this.s3Service.retrieveS3KeysFromPrefix(configFolderKey)
					.forEach(key -> this.determineNotDefaultConfigurationFromS3ToPersist(key, packageVersionEntity,
							defaultTarget, defaultOverrideConfig, overrideConfigEntities, useJsonParsing, buildId));
			}

			// Modify the properties to take into account the environment properties
			this.adaptPropertiesByEnvironment(overrideConfigEntities);

			// Save configuration built in db
			this.overrideConfigService.saveAll(overrideConfigEntities);
		});
	}

	/**
	 * Modify the properties to take into account the environment properties
	 * @param overrideConfigEntities Properties to evaluate
	 */
	private void adaptPropertiesByEnvironment(Set<OverrideConfigEntity> overrideConfigEntities) {
		if (this.environmentOverrideProperties.getOverride() != null) {
			overrideConfigEntities.forEach(p -> {
				if (this.environmentOverrideProperties.getOverride().containsKey(p.getLaunchConfig().getName())) {
					this.environmentOverrideProperties.getOverride()
						.get(p.getLaunchConfig().getName())
						.keySet()
						.forEach(epc -> {
							if (p.getWeasisPropertyEntities()
								.stream()
								.anyMatch(wpe -> Objects.equals(wpe.getCode(), epc))) {
								p.getWeasisPropertyEntities()
									.stream()
									.filter(wpe -> Objects.equals(wpe.getCode(), epc))
									.findFirst()
									.ifPresent(weasisPropertyEntityToModify -> {
										String envOverrideValue = this.environmentOverrideProperties.getOverride()
											.get(p.getLaunchConfig().getName())
											.get(epc);
										// Environment override: value + default value
										weasisPropertyEntityToModify.setValue(envOverrideValue);
										weasisPropertyEntityToModify.setDefaultValue(envOverrideValue);
									});
							}
						});
				}
			});
		}
	}

	/**
	 * Manage the default configuration properties file (config.properties if version <
	 * 4.2.0 or base.json if version >= 4.2.0). Extract the properties and transform them
	 * in OverrideConfigEntity
	 * @param key Path/key of the default configuration properties file
	 * @param packageVersionEntity PackageVersionEntity to evaluate
	 * @param defaultLaunchConfig LaunchConfigEntity to evaluate
	 * @param defaultTarget Default target
	 * @param defaultOverrideConfig OverrideConfigEntity which corresponds to the default
	 * @param overrideConfigEntities List of OverrideConfigEntity to persist
	 * @param useJsonParsing Flag to know if json parsing of properties file should be
	 * used
	 * @param buildId Build currently published for the package version: the properties
	 * are extracted again when the configuration in db comes from another build
	 */
	private void determineDefaultConfigurationFromS3ToPersist(String key, PackageVersionEntity packageVersionEntity,
			LaunchConfigEntity defaultLaunchConfig, TargetEntity defaultTarget,
			OverrideConfigEntity[] defaultOverrideConfig, Set<OverrideConfigEntity> overrideConfigEntities,
			boolean useJsonParsing, String buildId) {
		try {
			if (!this.overrideConfigService.existOverrideConfigWithVersionConfigTargetAndBuildId(packageVersionEntity,
					defaultLaunchConfig, defaultTarget, buildId)) {
				// Read the properties file and fill the entity to save
				// Keep the default config in order to fill the other config (3d,
				// dicomizer, etc..) with default values
				defaultOverrideConfig[0] = this.extractS3PropertiesAndBuildConfigToPersist(key, defaultLaunchConfig,
						packageVersionEntity, defaultTarget, null, useJsonParsing, buildId);
				overrideConfigEntities.add(defaultOverrideConfig[0]);
			}
			else if (packageVersionEntity != null && defaultLaunchConfig != null && defaultTarget != null) {
				// Default configuration already up-to-date with the current build: reuse
				// the one in db to fill the other configs (3d, dicomizer, etc..) which
				// would still have to be extracted
				defaultOverrideConfig[0] = this.overrideConfigService.retrieveProperties(packageVersionEntity.getId(),
						defaultLaunchConfig.getId(), defaultTarget.getId());
			}
		}
		catch (Exception e) {
			throw new TechnicalException(
					"Issue when loading configuration properties in db: %s".formatted(e.getMessage()));
		}
	}

	/**
	 * Manage others configuration properties files (ex: ext-dicomizer.properties,
	 * ext-3d.properties for version < 4.2.0 or ext-dicomizer.json for version >= 4.2.0).
	 * Extract the properties and transform them in OverrideConfigEntity
	 * @param key Path/key of the file to evaluate in order to know the launch_config
	 * associated
	 * @param packageVersionEntity PackageVersionEntity to evaluate
	 * @param defaultTarget Default target
	 * @param defaultOverrideConfig OverrideConfigEntity which corresponds to the default
	 * @param overrideConfigEntities List of OverrideConfigEntity to persist
	 * @param useJsonParsing Flag to know if json parsing of properties file should be
	 * used
	 * @param buildId Build currently published for the package version: the properties
	 * are extracted again when the configuration in db comes from another build
	 */
	private void determineNotDefaultConfigurationFromS3ToPersist(String key, PackageVersionEntity packageVersionEntity,
			TargetEntity defaultTarget, OverrideConfigEntity[] defaultOverrideConfig,
			Set<OverrideConfigEntity> overrideConfigEntities, boolean useJsonParsing, String buildId) {
		String fileName = Paths.get(key).getFileName().toString();
		try {
			// Retrieve the launch config based on the file name
			LaunchConfigEntity launchConfigFound = this.retrieveLaunchConfigAssociatedToFileName(useJsonParsing,
					fileName);

			if (launchConfigFound != null
					&& !this.overrideConfigService.existOverrideConfigWithVersionConfigTargetAndBuildId(
							packageVersionEntity, launchConfigFound, defaultTarget, buildId)) {
				// Read the properties file and fill the entity to save
				overrideConfigEntities.add(this.extractS3PropertiesAndBuildConfigToPersist(key, launchConfigFound,
						packageVersionEntity, defaultTarget, defaultOverrideConfig[0], useJsonParsing, buildId));
			}
		}
		catch (Exception e) {
			throw new TechnicalException(
					"Issue when loading configuration properties in db: %s".formatted(e.getMessage()));
		}
	}

	/**
	 * Retrieve the launch config by parsing the name of the file.
	 * @param useJsonParsing If config files are in json
	 * @param fileName File name to evaluate
	 * @return Launch config found
	 */
	@Nullable
	private LaunchConfigEntity retrieveLaunchConfigAssociatedToFileName(boolean useJsonParsing, String fileName) {
		LaunchConfigEntity launchConfigFound = null;
		String launchConfigName = null;

		// Retrieve the launch config name based on the file name
		// Case Json files
		if (useJsonParsing && fileName.contains(PropertiesFileName.EXTENSION_JSON_FILE)) {
			launchConfigName = fileName.substring(0, fileName.indexOf(PropertiesFileName.EXTENSION_JSON_FILE));
		}
		// Case properties file
		else if (!useJsonParsing && fileName.contains(PropertiesFileName.EXT_PATTERN_NAME)
				&& fileName.contains(PropertiesFileName.EXTENSION_PROPERTIES_FILE)
				&& !Objects.equals(fileName, EXT_CONFIG_PROPERTIES_FILENAME)) {
			launchConfigName = fileName.substring(PropertiesFileName.EXT_PATTERN_NAME.length(),
					fileName.indexOf(PropertiesFileName.EXTENSION_PROPERTIES_FILE));
		}

		// Retrieve the launch config associated to the parsing of the file name
		if (launchConfigName != null) {
			launchConfigFound = this.launchConfigRepository.findOptionalByNameIgnoreCase(launchConfigName).orElse(null);
		}
		return launchConfigFound;
	}

	/**
	 * Extract properties from file path and build OverrideConfigEntity to persist
	 * @param key S3 key to retrieve
	 * @param launchConfig LaunchConfigEntity to evaluate
	 * @param packageVersion PackageVersionEntity to evaluate
	 * @param target TargetEntity to evaluate
	 * @param defaultOverrideConfig OverrideConfigEntity which corresponds to the default
	 * @param useJsonParsing Flag to know if json parsing of properties file should be
	 * used
	 * @param buildId Build the properties have been extracted from
	 * @return OverrideConfigEntity to persist
	 */
	private OverrideConfigEntity extractS3PropertiesAndBuildConfigToPersist(String key, LaunchConfigEntity launchConfig,
			PackageVersionEntity packageVersion, TargetEntity target, OverrideConfigEntity defaultOverrideConfig,
			boolean useJsonParsing, String buildId) {
		OverrideConfigEntity overrideConfigEntity;

		// Read properties from file
		try (InputStream inputStream = this.s3Service.retrieveS3Object(key)) {
			overrideConfigEntity = useJsonParsing ? JacksonUtil.deserializeJsonOverrideConfigEntity(inputStream)
					: JacksonUtil.deserializePropertiesOverrideConfigEntity(inputStream);

			// For version with properties config files, still use the enum to fill
			// missing attributes.
			// To remove when not supporting anymore properties config files
			if (!useJsonParsing) {
				this.fillMissingPropertiesViaEnum(overrideConfigEntity);
			}
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when reading s3 config properties:%s".formatted(e.getMessage()));
		}

		// For null values, copy values from default override config
		overrideConfigEntity.replaceNullOrNotExistingPropertiesByDefault(defaultOverrideConfig);

		// Create key for new entity to save OverrideConfig
		overrideConfigEntity.setOverrideConfigEntityPK(OverrideConfigEntityPK.builder()
			.packageVersionId(packageVersion.getId())
			.launchConfigId(launchConfig.getId())
			.targetId(target.getId())
			.build());
		overrideConfigEntity.setPackageVersion(packageVersion);
		overrideConfigEntity.setLaunchConfig(launchConfig);
		overrideConfigEntity.setTarget(target);
		// Keep track of the build the properties have been extracted from, so that a
		// version re-uploaded with a new build id is detected and loaded again
		overrideConfigEntity.setBuildId(buildId);

		return overrideConfigEntity;
	}

	/**
	 * For older version of Weasis (< 4.2.0), fill missing information via enum
	 * @param overrideConfigEntity Entity to evaluate
	 */
	private void fillMissingPropertiesViaEnum(OverrideConfigEntity overrideConfigEntity) {
		// Category
		overrideConfigEntity.getWeasisPropertyEntities()
			.stream()
			.filter(weasisPropertyEntity -> Objects.nonNull(weasisPropertyEntity)
					&& Objects.nonNull(weasisPropertyEntity.getCode()))
			.forEach(weasisPropertyEntity -> weasisPropertyEntity
				.setCategory(WeasisProperties.fromCode(weasisPropertyEntity.getCode()) != null
						? WeasisProperties.fromCode(weasisPropertyEntity.getCode()).getWeasisPropertyCategory()
						: null));
	}

	/**
	 * Read mapping minimal version from S3
	 * @param key Key to retrieve
	 * @return mapping minimal version from S3
	 */
	List<MinimalReleaseVersion> retrieveS3MinimalReleaseVersions(String key) {
		try (InputStream responseInputStream = this.s3Service.retrieveS3Object(key)) {
			ObjectMapper objectMapper = JsonMapper.builder()
				.configureForJackson2()
				.propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
				.build();

			// Retrieve the minimal release versions
			List<MinimalReleaseVersion> minimalReleaseVersions = objectMapper.readValue(responseInputStream,
					new TypeReference<>() {
					});

			// Clean versions without qualifier for release and minimal versions
			minimalReleaseVersions.forEach(MinimalReleaseVersion::cleaningQualifierForReleaseAndMinimalVersion);

			return minimalReleaseVersions;
		}
		catch (IOException | JacksonException e) {
			throw new TechnicalException("Issue when trying to retrieve minimal release versions from file %s: %s"
				.formatted(key, e.getMessage()));
		}
	}

	/**
	 * Retrieve the mapping between the different versions requested and the available
	 * Weasis package installed in Viewer-Hub depending on minimal versions of releases
	 * @param availableWeasisPackageVersions List of available Weasis package versions
	 * @param minimalReleaseVersions Mapping between release and its minimal version
	 * @return mapping between the different versions requested and the available Weasis
	 * package installed in Viewer-Hub depending on minimal versions of releases
	 */
	Map<String, String> determineAvailablePackageVersionMapping(Set<String> availableWeasisPackageVersions,
			List<MinimalReleaseVersion> minimalReleaseVersions) {

		// Retrieve the different qualifiers
		Set<String> qualifiers = this.retrieveDistinctQualifiers(availableWeasisPackageVersions);

		// Retrieve a map by qualifiers containing a reverse order list of available
		// Weasis package installed in Viewer-Hub
		Map<String, Set<ComparableVersion>> reverseOrderedComparableVersionAvailableMapByQualifier = retrieveReverseOrderedComparableVersionAvailableMapByQualifier(
				availableWeasisPackageVersions, qualifiers);

		// Determine the mapping between the different versions released and the available
		// Weasis packages installed in Viewer-Hub depending on minimal versions of
		// releases
		return retrieveAvailablePackageVersionMapping(minimalReleaseVersions, qualifiers,
				reverseOrderedComparableVersionAvailableMapByQualifier);
	}

	/**
	 * Determine the mapping between the different versions released and the available
	 * packages installed in Viewer-Hub depending on minimal versions of releases
	 * @param minimalReleaseVersions Mapping between release and its minimal version
	 * @param qualifiers The different qualifiers of Weasis packages installed in
	 * Viewer-Hub
	 * @param reverseOrderedComparableVersionAvailableMapByQualifier Map by qualifiers
	 * containing a reverse order list of available package installed in Viewer-Hub
	 * @return the mapping between the different versions released and the available
	 * packages installed in Viewer-Hub depending on minimal versions of releases
	 */
	private static Map<String, String> retrieveAvailablePackageVersionMapping(
			List<MinimalReleaseVersion> minimalReleaseVersions, Set<String> qualifiers,
			Map<String, Set<ComparableVersion>> reverseOrderedComparableVersionAvailableMapByQualifier) {
		Map<String, String> availablePackageVersionMapping = new HashMap<>();
		qualifiers.forEach(qualifier -> minimalReleaseVersions
			.forEach(minimalReleaseVersionToEvaluate -> evaluateAvailablePackageVersionMapping(
					reverseOrderedComparableVersionAvailableMapByQualifier, availablePackageVersionMapping, qualifier,
					minimalReleaseVersionToEvaluate, minimalReleaseVersions)));
		return availablePackageVersionMapping;
	}

	/**
	 * Evaluate available package version mapping depending on qualifier and minimal
	 * release version
	 * @param reverseOrderedComparableVersionAvailableMapByQualifier Map by qualifiers
	 * containing a reverse order list of available Weasis package installed in Viewer-Hub
	 * @param availablePackageVersionMapping Available package version map to fill
	 * @param qualifier Qualifier
	 * @param minimalReleaseVersionToEvaluate Minimal release version to evaluate
	 * @param allMinimalReleaseVersions All minimal release versions
	 */
	private static void evaluateAvailablePackageVersionMapping(
			Map<String, Set<ComparableVersion>> reverseOrderedComparableVersionAvailableMapByQualifier,
			Map<String, String> availablePackageVersionMapping, String qualifier,
			MinimalReleaseVersion minimalReleaseVersionToEvaluate,
			List<MinimalReleaseVersion> allMinimalReleaseVersions) {
		// Retrieve the release version
		String releaseVersion = minimalReleaseVersionToEvaluate.getReleaseVersion();

		// Retrieve the major release number of the release version in order to filter the
		// available package by major release number
		String majorReleaseNumber = releaseVersion.substring(0, releaseVersion.indexOf(StringUtil.DOT));

		// Retrieve the minimal version as a ComparableVersion
		ComparableVersion comparableMinimalVersion = new ComparableVersion(
				minimalReleaseVersionToEvaluate.getMinimalVersion());

		// Retrieve the reverse ordered versions for this specific qualifier filtered by
		// major release number
		LinkedHashSet<ComparableVersion> availableVersionsToCompare = reverseOrderedComparableVersionAvailableMapByQualifier
			.get(qualifier)
			.stream()
			.filter(Objects::nonNull)
			.filter(v -> v.toString().contains(StringUtil.DOT))
			.filter(av -> Objects.equals(majorReleaseNumber,
					av.toString().substring(0, av.toString().indexOf(StringUtil.DOT))))
			.collect(Collectors.toCollection(LinkedHashSet::new));

		// Find the latest version installed in Viewer-Hub above the minimal version
		// of the release
		ComparableVersion comparableVersion = availableVersionsToCompare.stream()
			.filter(Objects::nonNull)
			// Keep available versions above the one requested
			.filter(av -> av.compareTo(comparableMinimalVersion) >= 0)
			// Keep available versions where the minimum version is below the one
			// requested
			.filter(av -> filterOnMinimumVersion(minimalReleaseVersionToEvaluate, allMinimalReleaseVersions, av))
			.findFirst()
			.orElse(null);

		// Fill the map with release version + qualifier installed => version installed
		// found for this specific qualifier
		if (comparableVersion != null) {
			availablePackageVersionMapping.put(releaseVersion + qualifier,
					Objects.equals(qualifier, PackageUtil.NO_QUALIFIER) ? comparableVersion.toString()
							: comparableVersion + qualifier);
		}
		else {
			availablePackageVersionMapping.put(releaseVersion + qualifier, null);
		}
	}

	/**
	 * Filter based on minimum version
	 * @param versionRequested Version requested
	 * @param allMinimalReleaseVersions All minimal release versions
	 * @param versionToCheck Version to check
	 * @return true if the version to check has its minimum version below the one
	 * requested
	 */
	private static boolean filterOnMinimumVersion(MinimalReleaseVersion versionRequested,
			List<MinimalReleaseVersion> allMinimalReleaseVersions, ComparableVersion versionToCheck) {
		// Retrieve MinimalReleaseVersion corresponding to the version to check
		MinimalReleaseVersion versionToFilter = allMinimalReleaseVersions.stream()
			.filter(a -> Objects.equals(versionToCheck.toString(), a.getReleaseVersion()))
			.findFirst()
			.orElse(null);
		if (versionToFilter != null) {
			// Verify the version to check has its minimum version below the one requested
			return new ComparableVersion(versionToFilter.getMinimalVersion())
				.compareTo(new ComparableVersion(versionRequested.getReleaseVersion())) <= 0;
		}
		return false;
	}

	/**
	 * Retrieve a map by qualifiers containing a reverse order list of available package
	 * installed in Viewer-Hub
	 * @param availableWeasisPackageVersions List of available weasis package versions
	 * installed in Viewer-Hub
	 * @param qualifiers The different qualifiers of packages installed in Viewer-Hub
	 * @return map by qualifiers containing a reverse order list of available package
	 * installed in Viewer-Hub
	 */
	private static Map<String, Set<ComparableVersion>> retrieveReverseOrderedComparableVersionAvailableMapByQualifier(
			Set<String> availableWeasisPackageVersions, Set<String> qualifiers) {
		Map<String, Set<ComparableVersion>> reverseOrderedComparableVersionMapByQualifier = new HashMap<>();
		qualifiers.forEach(qualifier -> {
			Set<ComparableVersion> comparableVersionsAvailableForSpecificQualifierReverseOrder = Objects
				.equals(PackageUtil.NO_QUALIFIER, qualifier)
						// Qualifiers without Hyphen
						? retrieveReverseOrderAvailablePackageVersionWithoutHyphen(availableWeasisPackageVersions)
						// Qualifiers with Hyphen
						: retrieveReverseOrderAvailablePackageVersionWithHyphen(availableWeasisPackageVersions,
								qualifier);

			reverseOrderedComparableVersionMapByQualifier.put(qualifier,
					comparableVersionsAvailableForSpecificQualifierReverseOrder);
		});
		return reverseOrderedComparableVersionMapByQualifier;
	}

	/**
	 * Retrieve reverse order available package versions with hyphen containing the
	 * qualifier in parameter
	 * @param availableWeasisPackageVersions List of available weasis * package versions
	 * installed in Viewer-Hub
	 * @param qualifier Qualifier to evaluate
	 * @return reverse order available package versions with hyphen containing the
	 * qualifier in parameter
	 */
	private static LinkedHashSet<ComparableVersion> retrieveReverseOrderAvailablePackageVersionWithHyphen(
			Set<String> availableWeasisPackageVersions, String qualifier) {
		return availableWeasisPackageVersions.stream()
			.filter(v -> v.contains(qualifier))
			.map(va -> va.substring(0, va.indexOf(qualifier)))
			.map(ComparableVersion::new)
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Reverse order available package versions without hyphen
	 * @param availableWeasisPackageVersions List of available weasis * * package versions
	 * installed in Viewer-Hub
	 * @return reverse order available package versions without hyphen
	 */
	private static LinkedHashSet<ComparableVersion> retrieveReverseOrderAvailablePackageVersionWithoutHyphen(
			Set<String> availableWeasisPackageVersions) {
		return availableWeasisPackageVersions.stream()
			.filter(v -> !v.contains(StringUtil.HYPHEN))
			.map(ComparableVersion::new)
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Retrieve the different qualifiers
	 * @param availableWeasisPackageVersions List of available weasis package versions
	 * installed in Viewer-Hub
	 * @return the different qualifiers from installed package in Viewer-Hub
	 */
	private Set<String> retrieveDistinctQualifiers(Set<String> availableWeasisPackageVersions) {
		// Qualifiers with Hyphen
		Set<String> qualifiers = availableWeasisPackageVersions.stream()
			.filter(av -> av.contains(StringUtil.HYPHEN))
			.map(v -> v.substring(v.indexOf(StringUtil.HYPHEN)))
			.collect(Collectors.toSet());

		// No qualifiers
		if (this
			.doesExistAvailablePackageWithoutQualifierAndDefaultQualifierNotFilled(availableWeasisPackageVersions)) {
			qualifiers.add(PackageUtil.NO_QUALIFIER);
		}

		return qualifiers;
	}

	/**
	 * Check if in the available package installed in Viewer-Hub, some package are without
	 * qualifier like xx.xx.xx and default package version is null or blank
	 * @param availableWeasisPackageVersions List of available Weasis * package versions
	 * installed in Viewer-Hub
	 * @return true if such kind of package exists and default package version is null or
	 * blank
	 */
	private boolean doesExistAvailablePackageWithoutQualifierAndDefaultQualifierNotFilled(
			Set<String> availableWeasisPackageVersions) {
		return !availableWeasisPackageVersions.stream()
			.filter(av -> !av.contains(StringUtil.HYPHEN))
			.collect(Collectors.toSet())
			.isEmpty()
				&& (this.defaultPackageVersionQualifier == null || this.defaultPackageVersionQualifier.isBlank());
	}

	/**
	 * Refresh the cache with the built map
	 * @param availablePackageVersionMapping Map used to refresh the cache
	 */
	private void refreshPackageVersionCache(Map<String, String> availablePackageVersionMapping) {
		// Remove all the values from package version cache
		this.cacheService.removeAllPackageVersion();

		availablePackageVersionMapping.keySet().forEach(key -> {
			String value = availablePackageVersionMapping.get(key);
			if (key != null && value != null) {
				// Retrieve the entity corresponding to the value in the DB and set the
				// entity in the cache
				this.cacheService.putPackageVersion(key, this.retrievePackageVersionEntity(value));
			}
		});
	}

	/**
	 * Retrieve the entity corresponding to the version in the DB
	 * @param version Version to evaluate
	 * @return entity found
	 */
	private PackageVersionEntity retrievePackageVersionEntity(String version) {
		// Retrieve the entity corresponding to the version in the DB
		String qualifier = version.contains(StringUtil.HYPHEN) ? version.substring(version.indexOf(StringUtil.HYPHEN))
				: null;
		String versionNumber = version.contains(StringUtil.HYPHEN)
				? version.substring(0, version.indexOf(StringUtil.HYPHEN)) : version;
		return this.packageVersionRepository.findByVersionNumberAndQualifier(versionNumber, qualifier).orElse(null);
	}

	/**
	 * Write the &lt;version&gt;/current pointer object with the given build id (the
	 * atomic publish marker for a package version).
	 * @param version Version folder name
	 * @param buildId Build id to publish
	 * @return CompletableFuture of the pointer upload
	 */
	private CompletableFuture<PutObjectResponse> writeCurrentBuildPointer(String version, String buildId) {
		String pointerKey = "%s/%s/%s".formatted(this.viewerHubResourcesPackagesWeasisPackagePath, version,
				PackageUtil.CURRENT_BUILD_POINTER_FILE);
		return this.s3Service.uploadObjectInS3(new ByteArrayInputStream(buildId.getBytes(StandardCharsets.UTF_8)),
				pointerKey);
	}

	/**
	 * Read the active build id from the &lt;version&gt;/current pointer object.
	 * @param version Version folder name
	 * @return the active build id, or null when no pointer exists (legacy version)
	 */
	private String readCurrentBuildPointer(String version) {
		String pointerKey = "%s/%s/%s".formatted(this.viewerHubResourcesPackagesWeasisPackagePath, version,
				PackageUtil.CURRENT_BUILD_POINTER_FILE);
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
			throw new TechnicalException("Issue when reading package build pointer:%s".formatted(e.getMessage()));
		}
	}

}
