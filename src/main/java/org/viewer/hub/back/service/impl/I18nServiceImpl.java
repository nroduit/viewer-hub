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
import org.viewer.hub.back.util.StringUtil;
import org.viewer.hub.front.views.i18n.component.I18nFilter;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
			// Create output directory where the zip file will be uploaded
			Path outDir = Paths.get(this.viewerHubResourcesPackagesWeasisI18nPath)
				.resolve(fileName.substring(I18N_PATTERN_NAME.length(), fileName.indexOf(ZIP_EXTENSION)));

			// Upload files in S3
			List<CompletableFuture<PutObjectResponse>> completableFutures = this.extractI18nFilesToUploadInS3(fileData,
					outDir);

			CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
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
	 * Check if a i18n version is missing in DB and add it if necessary
	 * @param availableWeasisI18nVersions versions to evaluate
	 */
	private void refreshI18nVersionsInDb(Set<String> availableWeasisI18nVersions) {
		// Retrieve all i18n versions in db
		List<I18nEntity> existingVersionsInDb = this.i18nRepository.findAll();

		// Format the versions in xx.xx.xx-QUALIFIER
		List<String> formattedExistingVersionsInDb = existingVersionsInDb.stream()
			.map(e -> e.getQualifier() != null ? e.getVersionNumber() + e.getQualifier() : e.getVersionNumber())
			.toList();

		// Retrieve the versions available in the weasis/i18n but not set in the db
		List<String> versionsNotExistingInDb = new ArrayList<>(availableWeasisI18nVersions.stream()
			.filter(Objects::nonNull)
			.filter(av -> !formattedExistingVersionsInDb.contains(av))
			.toList());

		// Case no hyphen
		Set<I18nEntity> versionsToAddInDb = versionsNotExistingInDb.stream()
			.filter(v -> !v.contains(StringUtil.HYPHEN))
			.map(v -> {
				I18nEntity i18nEntity = new I18nEntity();
				i18nEntity.setVersionNumber(v);
				i18nEntity.setDescription("Version %s".formatted(v));
				return i18nEntity;
			})
			.collect(Collectors.toSet());

		// Case hyphen
		versionsToAddInDb.addAll(versionsNotExistingInDb.stream().filter(v -> v.contains(StringUtil.HYPHEN)).map(v -> {
			I18nEntity i18nEntity = new I18nEntity();
			String versionNumber = v.substring(0, v.indexOf(StringUtil.HYPHEN));
			i18nEntity.setVersionNumber(versionNumber);
			i18nEntity.setQualifier(v.substring(v.indexOf(StringUtil.HYPHEN)));
			i18nEntity.setDescription("Version %s".formatted(v));
			return i18nEntity;
		}).collect(Collectors.toSet()));

		// Save in the db the missing versions
		this.i18nRepository.saveAll(versionsToAddInDb);
	}

}
