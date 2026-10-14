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
package org.viewer.hub.front.views.weasis.bundle.override;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.front.views.weasis.bundle.override.component.OverrideConfigFilter;

import java.io.Serial;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Hierarchical data provider: the root nodes are the package version numbers without
 * qualifier (ex: 4.7.2.0), their children are the override configs of this version.
 */
@Component
@Setter
@Getter
public class OverrideDataProvider
		extends AbstractBackEndHierarchicalDataProvider<OverrideConfigEntity, OverrideConfigFilter> {

	@Serial
	private static final long serialVersionUID = -8992746522768895949L;

	private static final Comparator<OverrideConfigEntity> CONFIG_COMPARATOR = Comparator
		.comparing((OverrideConfigEntity o) -> o.getPackageVersion().getVersionNumber(),
				Comparator.nullsFirst(Comparator.reverseOrder()))
		.thenComparing(o -> o.getPackageVersion().getQualifier(), Comparator.nullsFirst(Comparator.naturalOrder()))
		.thenComparing(o -> o.getLaunchConfig().getName(), Comparator.nullsFirst(Comparator.naturalOrder()))
		.thenComparing(o -> o.getTarget().getName(), Comparator.nullsFirst(Comparator.naturalOrder()));

	// Services
	private final transient OverrideLogic overrideLogic;

	@SuppressWarnings("java:S3749") // Filter state set by the grid, not a Spring
									// dependency
	private transient OverrideConfigFilter filter = new OverrideConfigFilter();

	@Autowired
	public OverrideDataProvider(OverrideLogic overrideLogic) {
		super();
		this.overrideLogic = overrideLogic;
	}

	/**
	 * A version node is not a persisted override config: it has no launch config
	 * @param item Item to evaluate
	 * @return true if the item is a version node
	 */
	public static boolean isVersionNode(OverrideConfigEntity item) {
		return item.getLaunchConfig() == null;
	}

	private static OverrideConfigEntity createVersionNode(String versionNumber) {
		PackageVersionEntity packageVersion = new PackageVersionEntity();
		packageVersion.setVersionNumber(versionNumber);
		return OverrideConfigEntity.builder().packageVersion(packageVersion).build();
	}

	private List<OverrideConfigEntity> retrieveSortedConfigs() {
		OverrideConfigFilter currentFilter = this.filter == null ? new OverrideConfigFilter() : this.filter;
		return this.overrideLogic.retrieveOverrideConfigs(currentFilter, PageRequest.of(0, Integer.MAX_VALUE))
			.getContent()
			.stream()
			.sorted(CONFIG_COMPARATOR)
			.toList();
	}

	private List<OverrideConfigEntity> retrieveNodes(OverrideConfigEntity parent) {
		List<OverrideConfigEntity> configs = this.retrieveSortedConfigs();
		if (parent == null) {
			LinkedHashSet<String> versionNumbers = new LinkedHashSet<>();
			configs.forEach(o -> versionNumbers.add(o.getPackageVersion().getVersionNumber()));
			return versionNumbers.stream().map(OverrideDataProvider::createVersionNode).toList();
		}
		String versionNumber = parent.getPackageVersion().getVersionNumber();
		return configs.stream()
			.filter(o -> Objects.equals(o.getPackageVersion().getVersionNumber(), versionNumber))
			.toList();
	}

	@Override
	protected Stream<OverrideConfigEntity> fetchChildrenFromBackEnd(
			HierarchicalQuery<OverrideConfigEntity, OverrideConfigFilter> query) {
		return this.retrieveNodes(query.getParent()).stream().skip(query.getOffset()).limit(query.getLimit());
	}

	@Override
	public int getChildCount(HierarchicalQuery<OverrideConfigEntity, OverrideConfigFilter> query) {
		return this.retrieveNodes(query.getParent()).size();
	}

	@Override
	public boolean hasChildren(OverrideConfigEntity item) {
		return isVersionNode(item);
	}

	@Override
	public Object getId(OverrideConfigEntity item) {
		return isVersionNode(item) ? ("version:" + item.getPackageVersion().getVersionNumber()) : item;
	}

}
