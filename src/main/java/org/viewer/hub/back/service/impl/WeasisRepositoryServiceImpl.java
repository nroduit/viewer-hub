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

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.LaunchConfigType;
import org.viewer.hub.back.enums.NexusApiQueryParamType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.model.asset.SearchAssetsResponseModel;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.service.PackageService;
import org.viewer.hub.back.service.TargetService;
import org.viewer.hub.back.service.WeasisRepositoryService;
import org.viewer.hub.back.util.PageUtil;
import org.viewer.hub.front.views.bundle.repository.component.WeasisAssetFilter;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service managing the Weasis Nexus Repository
 */
@Service
@Slf4j
public class WeasisRepositoryServiceImpl implements WeasisRepositoryService {

	// Name of the Weasis repository
	private final String weasisRepositoryName;

	// Path to access the different versions of Weasis in the repository
	private final String weasisRepositoryGroup;

	// WebClient used to look for assets in the nexus repository
	private final WebClient webClientSearchAssetsRepository;

	// WebClient used to download assets in the nexus repository
	private final WebClient webClientDownloadAssetsRepository;

	// Services
	private final PackageService packageService;

	private final TargetService targetService;

	// Repositories
	private final LaunchConfigRepository launchConfigRepository;

	@Autowired
	public WeasisRepositoryServiceImpl(@Value("${weasis.repository.name}") String weasisRepositoryName,
			@Value("${weasis.repository.group}") String weasisRepositoryGroup,
			WebClient webClientSearchAssetsRepository, WebClient webClientDownloadAssetsRepository,
			final PackageService packageService, final LaunchConfigRepository launchConfigRepository,
			final TargetService targetService) {
		this.weasisRepositoryName = weasisRepositoryName;
		this.weasisRepositoryGroup = weasisRepositoryGroup;
		this.webClientSearchAssetsRepository = webClientSearchAssetsRepository;
		this.webClientDownloadAssetsRepository = webClientDownloadAssetsRepository;
		this.packageService = packageService;
		this.launchConfigRepository = launchConfigRepository;
		this.targetService = targetService;
	}

	@Override
	public Page<WeasisAssetModel> retrieveWeasisAssets(WeasisAssetFilter filter, List<QuerySortOrder> sortOrders,
			Pageable pageable) {
		List<WeasisAssetModel> weasisAssetModels = retrieveWeasisAssets(filter);
		weasisAssetModels = sortWeasisAssets(weasisAssetModels, sortOrders);
		return weasisAssetModels != null && !weasisAssetModels.isEmpty()
				? PageUtil.convertToPage(weasisAssetModels, pageable) : Page.empty();
	}

	@Override
	public int countWeasisAssets(WeasisAssetFilter filter) {
		return retrieveWeasisAssets(filter).size();
	}

	@Override
	public InputStream downloadWeasisAsset(WeasisAssetModel weasisAssetModel) {
		byte[] downloadedAssetBytes = webClientDownloadAssetsRepository.get()
			.uri(uriBuilder -> uriBuilder.queryParam(NexusApiQueryParamType.REPOSITORY.getCode(), weasisRepositoryName)
				.queryParam(NexusApiQueryParamType.GROUP.getCode(), weasisRepositoryGroup)
				.queryParam(NexusApiQueryParamType.MAVEN_EXTENSION.getCode(),
						NexusApiQueryParamType.ZIP_EXTENSION.getCode())
				.queryParam(NexusApiQueryParamType.NAME.getCode(), weasisAssetModel.getArtifactId())
				.queryParam(NexusApiQueryParamType.VERSION.getCode(), weasisAssetModel.getVersion())
				.build())
			.retrieve()
			.bodyToMono(ByteArrayResource.class)
			.map(ByteArrayResource::getByteArray)
			.block();

		return downloadedAssetBytes != null ? new ByteArrayInputStream(downloadedAssetBytes) : null;
	}

	@Override
	public void removeResourcePackageVersion(WeasisAssetModel weasisAssetModel) {
		if (StringUtils.isNotBlank(weasisAssetModel.getVersion())) {
			// Retrieve default target and launchConfig
			LaunchConfigEntity defaultLaunchConfig = this.launchConfigRepository
				.findByNameIgnoreCase(LaunchConfigType.DEFAULT.getCode());
			TargetEntity defaultTarget = this.targetService.retrieveTargetByNameAndType(TargetType.DEFAULT.getCode(),
					TargetType.DEFAULT);
			// Remove the package version resources
			this.packageService.retrievePackageVersionByVersionNumber(weasisAssetModel.getVersion())
				.forEach(packageVersionEntity -> this.packageService
					.deleteResourcePackageVersion(OverrideConfigEntity.builder()
						.launchConfig(defaultLaunchConfig)
						.target(defaultTarget)
						.packageVersion(packageVersionEntity)
						.build()));
		}
	}

	/**
	 * Sort list of WeasisAssetModel
	 * @param weasisAssetModels List to sort
	 * @param sortOrders Used to select which sorting apply
	 * @return sorted list
	 */
	private List<WeasisAssetModel> sortWeasisAssets(List<WeasisAssetModel> weasisAssetModels,
			List<QuerySortOrder> sortOrders) {
		if (weasisAssetModels != null && !weasisAssetModels.isEmpty() && sortOrders != null && !sortOrders.isEmpty()) {
			// Currently handle only one QuerySortOrder. Multiple sort has not been
			// activated on the grid.
			QuerySortOrder querySortOrder = sortOrders.getFirst();
			String nameOfColumnToSort = querySortOrder.getSorted();
			SortDirection direction = querySortOrder.getDirection();

			if (Objects.equals("versionColumn", nameOfColumnToSort)) {
				sortWeasisAssetsByVersion(weasisAssetModels, direction);
			}
			else if (Objects.equals("fileSizeColumn", nameOfColumnToSort)) {
				sortWeasisAssetsByFileSize(weasisAssetModels, direction);
			}
			else if (Objects.equals("statusColumn", nameOfColumnToSort)) {
				sortWeasisAssetsByStatus(weasisAssetModels, direction);
			}
			else if (Objects.equals("lastModifiedColumn", nameOfColumnToSort)) {
				sortWeasisAssetsByLastModified(weasisAssetModels, direction);
			}
			else if (Objects.equals("lastDownloadedColumn", nameOfColumnToSort)) {
				sortWeasisAssetsByLastDownloaded(weasisAssetModels, direction);
			}
		}
		return weasisAssetModels;
	}

	/**
	 * Sort list of WeasisAssetModel by last download
	 * @param weasisAssetModels list to sort
	 * @param direction Ascending/Descending
	 */
	private void sortWeasisAssetsByLastDownloaded(List<WeasisAssetModel> weasisAssetModels, SortDirection direction) {
		weasisAssetModels.sort(Comparator.comparing(
				(WeasisAssetModel model) -> model.getLastDownloaded() != null
						? ZonedDateTime.parse(model.getLastDownloaded()) : null,
				Objects.equals(direction, SortDirection.ASCENDING) ? Comparator.nullsFirst(Comparator.naturalOrder())
						: Comparator.nullsLast(Comparator.reverseOrder())));
	}

	/**
	 * Sort list of WeasisAssetModel by last modified
	 * @param weasisAssetModels list to sort
	 * @param direction Ascending/Descending
	 */
	private void sortWeasisAssetsByLastModified(List<WeasisAssetModel> weasisAssetModels, SortDirection direction) {
		weasisAssetModels.sort(Comparator.comparing(
				(WeasisAssetModel model) -> model.getLastModified() != null
						? ZonedDateTime.parse(model.getLastModified()) : null,
				Objects.equals(direction, SortDirection.ASCENDING) ? Comparator.nullsFirst(Comparator.naturalOrder())
						: Comparator.nullsLast(Comparator.reverseOrder())));
	}

	/**
	 * Sort list of WeasisAssetModel by status
	 * @param weasisAssetModels list to sort
	 * @param direction Ascending/Descending
	 */
	private void sortWeasisAssetsByStatus(List<WeasisAssetModel> weasisAssetModels, SortDirection direction) {
		weasisAssetModels.sort(Objects.equals(direction, SortDirection.ASCENDING)
				? Comparator.comparing(WeasisAssetModel::isAlreadyInstalled)
				: Comparator.comparing(WeasisAssetModel::isAlreadyInstalled).reversed());
	}

	/**
	 * Sort list of WeasisAssetModel by file size
	 * @param weasisAssetModels list to sort
	 * @param direction Ascending/Descending
	 */
	private void sortWeasisAssetsByFileSize(List<WeasisAssetModel> weasisAssetModels, SortDirection direction) {
		weasisAssetModels.sort(
				Objects.equals(direction, SortDirection.ASCENDING) ? Comparator.comparing(WeasisAssetModel::getFileSize)
						: Comparator.comparing(WeasisAssetModel::getFileSize).reversed());
	}

	/**
	 * Sort list of WeasisAssetModel by version
	 * @param weasisAssetModels list to sort
	 * @param direction Ascending/Descending
	 */
	private void sortWeasisAssetsByVersion(List<WeasisAssetModel> weasisAssetModels, SortDirection direction) {
		weasisAssetModels.sort((a, b) -> {
			ComparableVersion comparableVersionA = new ComparableVersion(a.getVersion());
			ComparableVersion comparableVersionB = new ComparableVersion(b.getVersion());
			return Objects.equals(direction, SortDirection.ASCENDING) ? comparableVersionA.compareTo(comparableVersionB)
					: comparableVersionB.compareTo(comparableVersionA);
		});
	}

	/**
	 * Retrieve Weasis assets in the nexus repository and apply filters in parameter
	 * @param filters Filters to apply
	 * @return WeasisAssetModels found
	 */
	private List<WeasisAssetModel> retrieveWeasisAssets(WeasisAssetFilter filters) {
		// Call repository api to retrieve the assets
		SearchAssetsResponseModel searchAssetsResponseModel = webClientSearchAssetsRepository.get()
			.uri(uriBuilder -> uriBuilder.queryParam(NexusApiQueryParamType.REPOSITORY.getCode(), weasisRepositoryName)
				.queryParam(NexusApiQueryParamType.GROUP.getCode(), weasisRepositoryGroup)
				.queryParam(NexusApiQueryParamType.MAVEN_EXTENSION.getCode(),
						NexusApiQueryParamType.ZIP_EXTENSION.getCode())
				.build())
			.retrieve()
			.bodyToMono(SearchAssetsResponseModel.class)
			.onErrorResume(e -> Mono.empty())
			.block();

		// Transform to models
		List<WeasisAssetModel> weasisAssetModels = searchAssetsResponseModel != null
				? searchAssetsResponseModel.transformToWeasisAssetModels() : new ArrayList<>();

		// Check if already installed on the server
		weasisAssetModels.forEach(weasisAssetModel -> {
			if (packageService.doesVersionNumberAlreadyExists(weasisAssetModel.getVersion())) {
				weasisAssetModel.setAlreadyInstalled(true);
			}
		});

		// Apply filters
		return WeasisAssetFilter.applyFilters(weasisAssetModels, filters);
	}

}

// TODO handle continueToken if more than 100 assets ?
