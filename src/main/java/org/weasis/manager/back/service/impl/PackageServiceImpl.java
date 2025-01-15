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

package org.weasis.manager.back.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.config.properties.EnvironmentOverrideProperties;
import org.weasis.manager.back.constant.PropertiesFileName;
import org.weasis.manager.back.controller.exception.TechnicalException;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.OverrideConfigEntity;
import org.weasis.manager.back.entity.OverrideConfigEntityPK;
import org.weasis.manager.back.entity.PackageVersionEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.entity.WeasisPropertyEntity;
import org.weasis.manager.back.enums.LaunchConfigType;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.enums.WeasisProperties;
import org.weasis.manager.back.model.version.MinimalReleaseVersion;
import org.weasis.manager.back.repository.LaunchConfigRepository;
import org.weasis.manager.back.repository.PackageVersionRepository;
import org.weasis.manager.back.service.CacheService;
import org.weasis.manager.back.service.OverrideConfigService;
import org.weasis.manager.back.service.PackageService;
import org.weasis.manager.back.service.S3Service;
import org.weasis.manager.back.service.TargetService;
import org.weasis.manager.back.util.JacksonUtil;
import org.weasis.manager.back.util.PackageUtil;
import org.weasis.manager.back.util.StringUtil;
import org.weasis.manager.front.views.override.component.RefreshPackageGridEvent;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.weasis.manager.back.constant.PropertiesFileName.BIN_DIST_WEASIS_PATH;
import static org.weasis.manager.back.constant.PropertiesFileName.BIN_DIST_WEASIS_RESOURCES_PATH;
import static org.weasis.manager.back.constant.PropertiesFileName.CONF_FOLDER_NAME;
import static org.weasis.manager.back.constant.PropertiesFileName.EXT_CONFIG_PROPERTIES_FILENAME;
import static org.weasis.manager.back.constant.PropertiesFileName.RESOURCES_ZIP_FILE_NAME;
import static org.weasis.manager.back.constant.PropertiesFileName.VERSION_COMPATIBILITY_FILE_NAME;

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

	@Value("${weasis-manager.resources-packages.weasis.package.path}")
	private String weasisManagerResourcesPackagesWeasisPackagePath;

	@Value("${weasis-manager.resources-packages.weasis.mapping-minimal-version.path}")
	private String weasisManagerResourcesPackagesWeasisMappingMinimalVersionPath;

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

			// Retrieve list of available weasis-manager package versions
			Set<String> availableWeasisManagerPackageVersions = this.retrieveS3AvailableWeasisManagerPackageVersions();

			// Read mapping minimal version from existing releases
			List<MinimalReleaseVersion> minimalReleaseVersions = this
				.retrieveS3MinimalReleaseVersions(this.weasisManagerResourcesPackagesWeasisMappingMinimalVersionPath);

			// Check if a package version is missing in DB and add it if necessary
			this.refreshPackageVersionInDb(availableWeasisManagerPackageVersions, minimalReleaseVersions);

			// Refresh cache
			this.refreshPackageVersionCache(this.determineAvailablePackageVersionMapping(
					availableWeasisManagerPackageVersions, minimalReleaseVersions));

			// Load configurations properties in db if not already present
			this.loadS3ConfigurationPropertiesInDb(availableWeasisManagerPackageVersions);
		}
	}

	@Override
	public void handlePackageVersionToUpload(InputStream fileData, String versionToUpload) {
		try (fileData) {
			if (versionToUpload != null && !versionToUpload.isBlank()) {
				// Determine the output directory key where the zip file will be uploaded
				Path outDir = Paths.get(this.weasisManagerResourcesPackagesWeasisPackagePath).resolve(versionToUpload);

				// Upload version package in S3,zip resource folder, check if the
				// mapping-minimal-version.json file should be updated with a more
				// recent version and refresh cache and db

				// Upload version package in S3
				List<CompletableFuture<PutObjectResponse>> completableFutures = this.uploadVersionInS3(fileData,
						outDir);

				// Manage zip of the resources folder when all future are terminated
				this.handleZipResourceFolder(fileData, completableFutures, outDir);

				// When all uploads have been done
				this.handleReplacementOfMappingMinimalVersionAndRefresh(completableFutures, outDir);
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
	 * Retrieve in S3 available weasis manager package version
	 * @return Set of versions available
	 */
	private Set<String> retrieveS3AvailableWeasisManagerPackageVersions() {
		return this.s3Service.retrieveS3KeysFromPrefix(this.weasisManagerResourcesPackagesWeasisPackagePath)
			.stream()
			.filter(Objects::nonNull)
			.map(key -> {
				// Transform resources/packages/weasis/package/4.1.0-QUALIFIER/... en
				// 4.1.0-QUALIFIER/....
				String versionFolderKeys = key.substring(this.weasisManagerResourcesPackagesWeasisPackagePath.length());
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

		// Check if file mapping-minimal-version.json already exists
		if (this.doesMappingMinimalVersionFileExists()) {
			// If yes compare the existing file with the file to import
			// Read mapping minimal version from file to import and get max release
			// version
			ComparableVersion maxVersionToImport = this.retrieveMaxReleaseVersion(toImportFilePath);
			// Read mapping minimal version from existing file and get max release version
			ComparableVersion maxVersionExisting = this
				.retrieveMaxReleaseVersion(this.weasisManagerResourcesPackagesWeasisMappingMinimalVersionPath);

			// If max release version is the file to import: replace the previous file
			if (maxVersionToImport != null && maxVersionExisting != null
					&& maxVersionToImport.compareTo(maxVersionExisting) > 0) {
				// Replace file
				return this.s3Service.copyS3ObjectFromTo(toImportFilePath,
						this.weasisManagerResourcesPackagesWeasisMappingMinimalVersionPath);
			}
		}
		else {
			// Copy directly the file in the resources package folder
			return this.s3Service.copyS3ObjectFromTo(toImportFilePath,
					this.weasisManagerResourcesPackagesWeasisMappingMinimalVersionPath);
		}
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Retrieve max release version from file path in parameter
	 * @param mappingMinimalVersionFilePath Path of the file mapping minimal version
	 * @return Comparable version
	 */
	@Nullable
	private ComparableVersion retrieveMaxReleaseVersion(String mappingMinimalVersionFilePath) {
		return this.retrieveS3MinimalReleaseVersions(mappingMinimalVersionFilePath)
			.stream()
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
				zos.putNextEntry(new ZipEntry(StringUtil.pathWithS3Separator(
						Paths.get(StringUtil.pathWithS3Separator(outDir.toString())).relativize(filePath).toString())));
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
			throw new TechnicalException(
					"Issue when checking version of the weasis-native zip file to import (using json file):%s"
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
					"Issue when checking version of the weasis-native zip file to import (using properties file):%s"
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
		return this.s3Service.doesS3KeyExists(this.weasisManagerResourcesPackagesWeasisMappingMinimalVersionPath);
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
		// Case delete the entire package version folder if launch config default and
		// group is default
		if (Objects.equals(overrideConfigEntity.getLaunchConfig().getName(), LaunchConfigType.DEFAULT.getCode())
				&& Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			return this.s3Service
				.deleteS3Objects("%s/%s%s".formatted(this.weasisManagerResourcesPackagesWeasisPackagePath,
						overrideConfigEntity.getPackageVersion().getVersionNumber(),
						overrideConfigEntity.getPackageVersion().getQualifier() == null ? StringUtil.EMPTY_STRING
								: overrideConfigEntity.getPackageVersion().getQualifier()));
		}
		// Case delete only the config version selected (properties file) only if the
		// group is default
		else if (Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
			return this.s3Service.deleteS3Objects("%s/%s%s%s/%s%s%s".formatted(
					this.weasisManagerResourcesPackagesWeasisPackagePath,
					overrideConfigEntity.getPackageVersion().getVersionNumber(),
					overrideConfigEntity.getPackageVersion().getQualifier() == null ? StringUtil.EMPTY_STRING
							: overrideConfigEntity.getPackageVersion().getQualifier(),
					PropertiesFileName.PATH_CONF_FOLDER, PropertiesFileName.EXT_PATTERN_NAME,
					overrideConfigEntity.getLaunchConfig().getName(), PropertiesFileName.EXTENSION_PROPERTIES_FILE));
		}
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Check if a package version is missing in DB and add it if necessary
	 * @param availableWeasisManagerPackageVersions versions to evaluate
	 * @param minimalReleaseVersions minimal release versions
	 */
	private void refreshPackageVersionInDb(Set<String> availableWeasisManagerPackageVersions,
			List<MinimalReleaseVersion> minimalReleaseVersions) {
		// Retrieve all package versions in db
		List<PackageVersionEntity> existingVersionsInDb = this.packageVersionRepository.findAll();

		// Format the versions in xx.xx.xx-QUALIFIER
		List<String> formattedExistingVersionsInDb = existingVersionsInDb.stream()
			.map(e -> e.getQualifier() != null ? e.getVersionNumber() + e.getQualifier() : e.getVersionNumber())
			.toList();

		// Retrieve the versions available in the weasis/package but not set in the db
		List<String> versionsNotExistingInDb = new ArrayList<>(availableWeasisManagerPackageVersions.stream()
			.filter(Objects::nonNull)
			.filter(av -> !formattedExistingVersionsInDb.contains(av))
			.toList());

		// Case no hyphen
		Set<PackageVersionEntity> versionsToAddInDb = versionsNotExistingInDb.stream()
			.filter(v -> !v.contains(StringUtil.HYPHEN))
			.map(v -> {
				PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
				packageVersionEntity.setVersionNumber(v);
				packageVersionEntity.setI18nVersion(this.retrieveI18nFromVersionNumber(v, minimalReleaseVersions));
				packageVersionEntity.setDescription("Version %s".formatted(v));
				return packageVersionEntity;
			})
			.collect(Collectors.toSet());

		// Case hyphen
		versionsToAddInDb.addAll(versionsNotExistingInDb.stream().filter(v -> v.contains(StringUtil.HYPHEN)).map(v -> {
			PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
			String versionNumber = v.substring(0, v.indexOf(StringUtil.HYPHEN));
			packageVersionEntity.setVersionNumber(versionNumber);
			packageVersionEntity.setQualifier(v.substring(v.indexOf(StringUtil.HYPHEN)));
			packageVersionEntity
				.setI18nVersion(this.retrieveI18nFromVersionNumber(versionNumber, minimalReleaseVersions));
			packageVersionEntity.setDescription("Version %s".formatted(v));
			return packageVersionEntity;
		}).collect(Collectors.toSet()));

		// Save in the db the missing versions
		this.packageVersionRepository.saveAll(versionsToAddInDb);
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
	 * weasis-manager
	 * @param streamPaths Path of the directories
	 * @return Names of the directories
	 */
	@NotNull
	Set<String> retrieveAvailableWeasisManagerPackageVersions(Stream<Path> streamPaths) {
		return streamPaths.filter(Files::isDirectory)
			.map(Path::getFileName)
			.map(Path::toString)
			.collect(Collectors.toSet());
	}

	/**
	 * Load S3 configurations properties in db if not already present
	 */
	private void loadS3ConfigurationPropertiesInDb(Set<String> availableWeasisManagerPackageVersions) {
		// Retrieve default launch_config and target
		LaunchConfigEntity defaultLaunchConfig = this.launchConfigRepository
			.findByNameIgnoreCase(LaunchConfigType.DEFAULT.getCode());
		TargetEntity defaultTarget = this.targetService.retrieveTargetByName(TargetType.DEFAULT.getCode());
		// Loop on folder paths
		availableWeasisManagerPackageVersions.forEach(availableVersion -> {
			Set<OverrideConfigEntity> overrideConfigEntities = new HashSet<>();
			PackageVersionEntity packageVersionEntity = this.retrievePackageVersionEntity(availableVersion);

			// Flag to know if we should use json parsing or properties parsing of
			// configuration files
			boolean useJsonParsing = this.shouldUseJsonParsing(packageVersionEntity);

			// Needed to be effectively final
			final OverrideConfigEntity[] defaultOverrideConfig = { null };
			// Config folder key for this version
			String configFolderKey = StringUtil.pathWithS3Separator(
					Paths
						.get(this.weasisManagerResourcesPackagesWeasisPackagePath, availableVersion,
								PropertiesFileName.PATH_CONF_FOLDER)
						.toString());

			// Default properties file key for this version
			String defaultConfigPropertiesFileKey = StringUtil.pathWithS3Separator(
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
						useJsonParsing);

				// Browse content of folder in order to find other configs (with default
				// found above)
				this.s3Service.retrieveS3KeysFromPrefix(configFolderKey)
					.forEach(key -> this.determineNotDefaultConfigurationFromS3ToPersist(key, packageVersionEntity,
							defaultTarget, defaultOverrideConfig, overrideConfigEntities, useJsonParsing));
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
		if (this.environmentOverrideProperties.getOverride() != null){
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
	 */
	private void determineDefaultConfigurationFromS3ToPersist(String key, PackageVersionEntity packageVersionEntity,
			LaunchConfigEntity defaultLaunchConfig, TargetEntity defaultTarget,
			OverrideConfigEntity[] defaultOverrideConfig, Set<OverrideConfigEntity> overrideConfigEntities,
			boolean useJsonParsing) {
		try {
			if (!this.overrideConfigService.existOverrideConfigWithVersionConfigTarget(packageVersionEntity,
					defaultLaunchConfig, defaultTarget)) {
				// Read the properties file and fill the entity to save
				// Keep the default config in order to fill the other config (3d,
				// dicomizer, etc..) with default values
				defaultOverrideConfig[0] = this.extractS3PropertiesAndBuildConfigToPersist(key, defaultLaunchConfig,
						packageVersionEntity, defaultTarget, null, useJsonParsing);
				overrideConfigEntities.add(defaultOverrideConfig[0]);
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
	 */
	private void determineNotDefaultConfigurationFromS3ToPersist(String key, PackageVersionEntity packageVersionEntity,
			TargetEntity defaultTarget, OverrideConfigEntity[] defaultOverrideConfig,
			Set<OverrideConfigEntity> overrideConfigEntities, boolean useJsonParsing) {
		String fileName = Paths.get(key).getFileName().toString();
		try {
			// Retrieve the launch config based on the file name
			LaunchConfigEntity launchConfigFound = this.retrieveLaunchConfigAssociatedToFileName(useJsonParsing,
					fileName);

			if (launchConfigFound != null && !this.overrideConfigService
				.existOverrideConfigWithVersionConfigTarget(packageVersionEntity, launchConfigFound, defaultTarget)) {
				// Read the properties file and fill the entity to save
				overrideConfigEntities.add(this.extractS3PropertiesAndBuildConfigToPersist(key, launchConfigFound,
						packageVersionEntity, defaultTarget, defaultOverrideConfig[0], useJsonParsing));
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
			launchConfigFound = this.launchConfigRepository.findByNameIgnoreCase(launchConfigName);
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
	 * @return OverrideConfigEntity to persist
	 */
	private OverrideConfigEntity extractS3PropertiesAndBuildConfigToPersist(String key, LaunchConfigEntity launchConfig,
			PackageVersionEntity packageVersion, TargetEntity target, OverrideConfigEntity defaultOverrideConfig,
			boolean useJsonParsing) {
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
			ObjectMapper objectMapper = new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

			// Retrieve the minimal release versions
			List<MinimalReleaseVersion> minimalReleaseVersions = objectMapper.readValue(responseInputStream,
					new TypeReference<>() {
					});

			// Clean versions without qualifier for release and minimal versions
			minimalReleaseVersions.forEach(MinimalReleaseVersion::cleaningQualifierForReleaseAndMinimalVersion);

			return minimalReleaseVersions;
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when trying to retrieve minimal release versions from file %s: %s"
				.formatted(key, e.getMessage()));
		}
	}

	/**
	 * Retrieve the mapping between the different versions requested and the available
	 * package installed in weasis-manager depending on minimal versions of releases
	 * @param availableWeasisManagerPackageVersions List of available weasis-manager
	 * package versions
	 * @param minimalReleaseVersions Mapping between release and its minimal version
	 * @return mapping between the different versions requested and the available package
	 * installed in weasis-manager depending on minimal versions of releases
	 */
	Map<String, String> determineAvailablePackageVersionMapping(Set<String> availableWeasisManagerPackageVersions,
			List<MinimalReleaseVersion> minimalReleaseVersions) {

		// Retrieve the different qualifiers
		Set<String> qualifiers = this.retrieveDistinctQualifiers(availableWeasisManagerPackageVersions);

		// Retrieve a map by qualifiers containing a reverse order list of available
		// package installed in weasis-manager
		Map<String, Set<ComparableVersion>> reverseOrderedComparableVersionAvailableMapByQualifier = retrieveReverseOrderedComparableVersionAvailableMapByQualifier(
				availableWeasisManagerPackageVersions, qualifiers);

		// Determine the mapping between the different versions released and the available
		// packages installed in weasis-manager depending on minimal versions of releases
		return retrieveAvailablePackageVersionMapping(minimalReleaseVersions, qualifiers,
				reverseOrderedComparableVersionAvailableMapByQualifier);
	}

	/**
	 * Determine the mapping between the different versions released and the available
	 * packages installed in weasis-manager depending on minimal versions of releases
	 * @param minimalReleaseVersions Mapping between release and its minimal version
	 * @param qualifiers The different qualifiers of packages installed in weasis-manager
	 * @param reverseOrderedComparableVersionAvailableMapByQualifier Map by qualifiers
	 * containing a reverse order list of available package installed in weasis-manager
	 * @return the mapping between the different versions released and the available
	 * packages installed in weasis-manager depending on minimal versions of releases
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
	 * containing a reverse order list of available package installed in weasis-manager
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

		// Find the latest version installed in weasis-manager above the minimal version
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
	 * installed in weasis-manager
	 * @param availableWeasisManagerPackageVersions List of available weasis-manager
	 * package versions installed in weasis-manager
	 * @param qualifiers The different qualifiers of packages installed in weasis-manager
	 * @return map by qualifiers containing a reverse order list of available package
	 * installed in weasis-manager
	 */
	private static Map<String, Set<ComparableVersion>> retrieveReverseOrderedComparableVersionAvailableMapByQualifier(
			Set<String> availableWeasisManagerPackageVersions, Set<String> qualifiers) {
		Map<String, Set<ComparableVersion>> reverseOrderedComparableVersionMapByQualifier = new HashMap<>();
		qualifiers.forEach(qualifier -> {
			Set<ComparableVersion> comparableVersionsAvailableForSpecificQualifierReverseOrder = Objects
				.equals(PackageUtil.NO_QUALIFIER, qualifier)
						// Qualifiers without Hyphen
						? retrieveReverseOrderAvailablePackageVersionWithoutHyphen(
								availableWeasisManagerPackageVersions)
						// Qualifiers with Hyphen
						: retrieveReverseOrderAvailablePackageVersionWithHyphen(availableWeasisManagerPackageVersions,
								qualifier);

			reverseOrderedComparableVersionMapByQualifier.put(qualifier,
					comparableVersionsAvailableForSpecificQualifierReverseOrder);
		});
		return reverseOrderedComparableVersionMapByQualifier;
	}

	/**
	 * Retrieve reverse order available package versions with hyphen containing the
	 * qualifier in parameter
	 * @param availableWeasisManagerPackageVersions List of available weasis-manager *
	 * package versions installed in weasis-manager
	 * @param qualifier Qualifier to evaluate
	 * @return reverse order available package versions with hyphen containing the
	 * qualifier in parameter
	 */
	private static LinkedHashSet<ComparableVersion> retrieveReverseOrderAvailablePackageVersionWithHyphen(
			Set<String> availableWeasisManagerPackageVersions, String qualifier) {
		return availableWeasisManagerPackageVersions.stream()
			.filter(v -> v.contains(qualifier))
			.map(va -> va.substring(0, va.indexOf(qualifier)))
			.map(ComparableVersion::new)
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Reverse order available package versions without hyphen
	 * @param availableWeasisManagerPackageVersions List of available weasis-manager * *
	 * package versions installed in weasis-manager
	 * @return reverse order available package versions without hyphen
	 */
	private static LinkedHashSet<ComparableVersion> retrieveReverseOrderAvailablePackageVersionWithoutHyphen(
			Set<String> availableWeasisManagerPackageVersions) {
		return availableWeasisManagerPackageVersions.stream()
			.filter(v -> !v.contains(StringUtil.HYPHEN))
			.map(ComparableVersion::new)
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Retrieve the different qualifiers
	 * @param availableWeasisManagerPackageVersions List of available weasis-manager
	 * package versions installed in weasis-manager
	 * @return the different qualifiers from installed package in weasis-manager
	 */
	private Set<String> retrieveDistinctQualifiers(Set<String> availableWeasisManagerPackageVersions) {
		// Qualifiers with Hyphen
		Set<String> qualifiers = availableWeasisManagerPackageVersions.stream()
			.filter(av -> av.contains(StringUtil.HYPHEN))
			.map(v -> v.substring(v.indexOf(StringUtil.HYPHEN)))
			.collect(Collectors.toSet());

		// No qualifiers
		if (this.doesExistAvailablePackageWithoutQualifierAndDefaultQualifierNotFilled(
				availableWeasisManagerPackageVersions)) {
			qualifiers.add(PackageUtil.NO_QUALIFIER);
		}

		return qualifiers;
	}

	/**
	 * Check if in the available package installed in weasis-manager, some package are
	 * without qualifier like xx.xx.xx and default package version is null or blank
	 * @param availableWeasisManagerPackageVersions List of available weasis-manager *
	 * package versions installed in weasis-manager
	 * @return true if such kind of package exists and default package version is null or
	 * blank
	 */
	private boolean doesExistAvailablePackageWithoutQualifierAndDefaultQualifierNotFilled(
			Set<String> availableWeasisManagerPackageVersions) {
		return !availableWeasisManagerPackageVersions.stream()
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
		return this.packageVersionRepository.findByVersionNumberAndQualifier(versionNumber, qualifier);
	}

}
