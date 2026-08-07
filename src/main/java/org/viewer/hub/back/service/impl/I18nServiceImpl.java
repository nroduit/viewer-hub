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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.entity.I18nEntity;
import org.viewer.hub.back.repository.I18nRepository;
import org.viewer.hub.back.repository.specification.I18nVersionSpecification;
import org.viewer.hub.back.service.I18nService;
import org.viewer.hub.back.service.S3Service;
import org.viewer.hub.back.util.PackageUtil;
import org.viewer.hub.back.util.StringUtil;
import org.viewer.hub.front.views.weasis.i18n.component.I18nFilter;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.viewer.hub.back.constant.PropertiesFileName.I18N_PATTERN_NAME;
import static org.viewer.hub.back.constant.PropertiesFileName.ZIP_EXTENSION;

@Service
@Slf4j
@RefreshScope
public class I18nServiceImpl implements I18nService {

	@Value("${viewer-hub.resources-packages.weasis.i18n.path}")
	private String viewerHubResourcesPackagesWeasisI18nPath;

	// Services
	private final I18nRepository i18nRepository;

	private final S3Service s3Service;

	/**
	 * Autowired constructor
	 */
	@Autowired
	public I18nServiceImpl(final I18nRepository i18nRepository, final S3Service s3Service) {
		this.i18nRepository = i18nRepository;
		this.s3Service = s3Service;
	}

	@Override
	// Every 24h
	@Scheduled(fixedRate = 24 * 60 * 60 * 1000)
	public void refreshAvailableI18nVersion() {
		// Retrieve list of available weasis i18n versions and check if a i18n
		// version is missing in DB: add it if necessary
		this.refreshI18nVersionsInDb(this.retrieveS3AvailableWeasisI18nPackageVersions());
	}

	@Override
	public void handleI18nVersionToUpload(InputStream fileData, String fileName) {
		try (fileData) {
			// Version folder derived from the file name (ex: weasis-i18n-dist-4.0.0-SNAPSHOT.zip ->
			// 4.0.0-SNAPSHOT)
			String version = fileName.substring(I18N_PATTERN_NAME.length(), fileName.indexOf(ZIP_EXTENSION));

			// Each upload lands in its own immutable build-stamped sub-directory
			// (<version>/<buildId>/...) so a re-uploaded (e.g. SNAPSHOT) version never overwrites
			// files a client may currently be downloading.
			String buildId = UUID.randomUUID().toString();
			Path outDir = Paths.get(this.viewerHubResourcesPackagesWeasisI18nPath).resolve(version).resolve(buildId);

			// Upload files in S3
			List<CompletableFuture<PutObjectResponse>> completableFutures = this.extractI18nFilesToUploadInS3(fileData,
					outDir);

			CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
				// Atomic publish: only once every file of the build is durably written, flip the
				// <version>/current pointer to this build id, then refresh the DB catalog. The new
				// build is therefore never visible/served until it is complete.
				.thenCompose(unused -> this.writeCurrentBuildPointer(version, buildId))
				.whenComplete((result, throwable) -> {
					if (throwable == null) {
						// Refresh the DB
						this.refreshAvailableI18nVersion();
					}
					else {
						throw new TechnicalException(
								"Issue when uploading i18n files in S3, at least one future didn't end well:%s"
									.formatted(throwable.getMessage()));
					}
				});

		}
		catch (IOException e) {
			throw new TechnicalException("Issue when uploading i18n:%s".formatted(e.getMessage()));
		}
	}

	@Override
	public String retrieveI18nBuildId(String i18nVersion) {
		if (i18nVersion == null || i18nVersion.isBlank()) {
			return null;
		}
		String versionNumber = i18nVersion.contains(StringUtil.HYPHEN)
				? i18nVersion.substring(0, i18nVersion.indexOf(StringUtil.HYPHEN)) : i18nVersion;
		String qualifier = i18nVersion.contains(StringUtil.HYPHEN)
				? i18nVersion.substring(i18nVersion.indexOf(StringUtil.HYPHEN)) : null;
		return this.i18nRepository.findAll()
			.stream()
			.filter(e -> Objects.equals(e.getVersionNumber(), versionNumber)
					&& Objects.equals(e.getQualifier(), qualifier))
			.map(I18nEntity::getBuildId)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	/**
	 * Write the &lt;version&gt;/current pointer object with the given build id (the atomic publish
	 * marker for an i18n version).
	 * @param version Version folder name
	 * @param buildId Build id to publish
	 * @return CompletableFuture of the pointer upload
	 */
	private CompletableFuture<PutObjectResponse> writeCurrentBuildPointer(String version, String buildId) {
		String pointerKey = "%s/%s/%s".formatted(this.viewerHubResourcesPackagesWeasisI18nPath, version,
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
		String pointerKey = "%s/%s/%s".formatted(this.viewerHubResourcesPackagesWeasisI18nPath, version,
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
			throw new TechnicalException("Issue when reading i18n build pointer:%s".formatted(e.getMessage()));
		}
	}

	@Override
	public Page<I18nEntity> retrieveI18nVersionsPageable(I18nFilter filter, Pageable pageable) {
		Page<I18nEntity> i18nVersionsFound;
		if (!filter.hasFilter()) {
			// No filter
			i18nVersionsFound = this.i18nRepository.findAll(pageable);
		}
		else {
			// Create the specification and query the i18n table
			Specification<I18nEntity> i18nSpecification = new I18nVersionSpecification(filter);
			i18nVersionsFound = this.i18nRepository.findAll(i18nSpecification, pageable);
		}
		return i18nVersionsFound;
	}

	@Override
	public int countI18nVersions(I18nFilter filter) {
		int countI18nVersions;

		if (!filter.hasFilter()) {
			// No filter
			countI18nVersions = (int) this.i18nRepository.count();
		}
		else {
			// Create the specification and query the i18n table
			Specification<I18nEntity> i18nSpecification = new I18nVersionSpecification(filter);
			countI18nVersions = (int) this.i18nRepository.count(i18nSpecification);
		}
		return countI18nVersions;
	}

	@Override
	public void deleteResourceI18nVersion(I18nEntity i18nEntity) {
		if (i18nEntity != null) {
			// Update I18n table
			this.deleteResourceI18nInDb(i18nEntity);

			// S3
			this.deleteResourceI18nVersionInS3(i18nEntity).whenComplete((result, throwable) -> {
				if (throwable == null) {
					// Refresh available i18n versions
					this.refreshAvailableI18nVersion();
				}
				else {
					throw new TechnicalException(
							"Issue when deleting i18n resources files in S3:%s".formatted(throwable.getMessage()));
				}
			});

		}
	}

	/**
	 * Retrieve in S3 available Weasis i18n versions
	 * @return Set of versions available
	 */
	private Set<String> retrieveS3AvailableWeasisI18nPackageVersions() {
		return this.s3Service.retrieveS3KeysFromPrefix(this.viewerHubResourcesPackagesWeasisI18nPath)
			.stream()
			.filter(Objects::nonNull)
			.map(key -> {
				// Transform resources/packages/weasis/i18n/4.0.0-QUALIFIER/... en
				// 4.0.0-QUALIFIER/....
				String versionFolderKeys = key.substring(this.viewerHubResourcesPackagesWeasisI18nPath.length() + 1);
				// Transform 4.1.0-QUALIFIER/... en 4.1.0-QUALIFIER
				return versionFolderKeys.substring(0, versionFolderKeys.indexOf("/"));
			})
			.collect(Collectors.toSet());
	}

	/**
	 * Upload i18n files in folders
	 * @param fileData Files
	 * @param outDir Output directory
	 */
	private List<CompletableFuture<PutObjectResponse>> extractI18nFilesToUploadInS3(InputStream fileData, Path outDir) {
		List<CompletableFuture<PutObjectResponse>> futures = new ArrayList<>();
		try (ZipInputStream zis = new ZipInputStream(fileData)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				Path filePath = outDir.resolve(ze.getName());
				if (!ze.isDirectory()) {
					// Copy file from zip to S3
					CompletableFuture<PutObjectResponse> completableFuture = this.extractZippedFileToS3(zis, filePath);
					if (completableFuture != null) {
						// add in list to wait all the futures to be processed
						futures.add(completableFuture);
					}
				}
			}
			return futures;
		}
		catch (IOException e) {
			throw new TechnicalException("Issue when uploading i18n version:%s".formatted(e.getMessage()));
		}
	}

	/**
	 * Copy file from zip to S3
	 * @param zis ZipInputStream
	 * @param path Path
	 * @return
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
	 * Delete i18n version in db
	 * @param i18nEntity I18nEntity to evaluate
	 */
	private void deleteResourceI18nInDb(I18nEntity i18nEntity) {
		this.i18nRepository.delete(i18nEntity);
	}

	/**
	 * Delete in the S3 the folder corresponding to the i18n version to delete
	 * @param i18nEntity to evaluate
	 * @return CompletableFuture
	 */
	private CompletableFuture<DeleteObjectsResponse> deleteResourceI18nVersionInS3(I18nEntity i18nEntity) {
		// Delete the entire i18n version folder
		return this.s3Service.deleteS3Objects(
				"%s/%s%s".formatted(this.viewerHubResourcesPackagesWeasisI18nPath, i18nEntity.getVersionNumber(),
						i18nEntity.getQualifier() == null ? StringUtil.EMPTY_STRING : i18nEntity.getQualifier()));
	}

	/**
	 * Synchronise the i18n table with the versions available in S3: add missing versions and, for
	 * every version, (re)set its build_id from the &lt;version&gt;/current pointer so a re-uploaded
	 * (e.g. SNAPSHOT) version and a catalog rebuilt from S3 both converge on the active build.
	 * @param availableWeasisI18nVersions versions to evaluate
	 */
	private void refreshI18nVersionsInDb(Set<String> availableWeasisI18nVersions) {
		// Retrieve all i18n versions in db
		List<I18nEntity> existingVersionsInDb = this.i18nRepository.findAll();

		List<I18nEntity> entitiesToSave = new ArrayList<>();
		for (String version : availableWeasisI18nVersions) {
			if (version == null) {
				continue;
			}
			// Split version folder name into version number / qualifier (qualifier keeps its
			// leading hyphen, ex: 4.0.0-SNAPSHOT -> 4.0.0 + "-SNAPSHOT")
			String versionNumber = version.contains(StringUtil.HYPHEN)
					? version.substring(0, version.indexOf(StringUtil.HYPHEN)) : version;
			String qualifier = version.contains(StringUtil.HYPHEN)
					? version.substring(version.indexOf(StringUtil.HYPHEN)) : null;

			// Active build id for this version (null for a legacy version without pointer)
			String buildId = this.readCurrentBuildPointer(version);

			I18nEntity existing = existingVersionsInDb.stream()
				.filter(e -> Objects.equals(e.getVersionNumber(), versionNumber)
						&& Objects.equals(e.getQualifier(), qualifier))
				.findFirst()
				.orElse(null);

			if (existing == null) {
				// New version: create the catalog row
				I18nEntity i18nEntity = new I18nEntity();
				i18nEntity.setVersionNumber(versionNumber);
				i18nEntity.setQualifier(qualifier);
				i18nEntity.setDescription("Version %s".formatted(version));
				i18nEntity.setBuildId(buildId);
				entitiesToSave.add(i18nEntity);
			}
			else if (buildId != null && !Objects.equals(existing.getBuildId(), buildId)) {
				// Existing version re-uploaded with a new build: update the pinned build id
				existing.setBuildId(buildId);
				entitiesToSave.add(existing);
			}
		}

		// Save the created/updated versions
		if (!entitiesToSave.isEmpty()) {
			this.i18nRepository.saveAll(entitiesToSave);
		}
	}

}
