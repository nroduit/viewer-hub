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
package org.weasis.manager.back.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.weasis.manager.back.entity.OverrideConfigEntity;
import org.weasis.manager.front.views.override.component.OverrideConfigFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Override config specification: used to look for entries depending on criteria
 */
public class OverrideConfigSpecification implements Specification<OverrideConfigEntity> {

	// Like character
	private static final String LIKE = "%";

	// Criteria to look for
	private final OverrideConfigFilter overrideConfigFilter;

	/**
	 * Constructor with filter
	 * @param overrideConfigFilter Criteria to look for
	 */
	public OverrideConfigSpecification(OverrideConfigFilter overrideConfigFilter) {
		this.overrideConfigFilter = overrideConfigFilter;
	}

	@Override
	public Predicate toPredicate(Root<OverrideConfigEntity> root, CriteriaQuery<?> query,
			CriteriaBuilder criteriaBuilder) {
		// Predicates to fill
		List<Predicate> predicates = new ArrayList<>();

		// Paths
		// Package version
		Path<String> pPackageVersionVersionNumber = root.get("packageVersion").get("versionNumber");
		Path<String> pPackageVersionQualifier = root.get("packageVersion").get("qualifier");

		// Launch config
		Path<String> pLaunchConfigName = root.get("launchConfig").get("name");
		// Target
		Path<String> pTargetName = root.get("target").get("name");

		// Build criteria
		if (this.overrideConfigFilter != null) {
			// Package version
			this.buildCriteriaPackageVersion(criteriaBuilder, predicates, pPackageVersionVersionNumber,
					pPackageVersionQualifier);
			// Launch config
			this.buildCriteriaLaunchConfig(criteriaBuilder, predicates, pLaunchConfigName);
			// Target
			this.buildCriteriaTarget(criteriaBuilder, predicates, pTargetName);
		}

		return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
	}

	/**
	 * Build criteria for package version
	 * @param criteriaBuilder CriteriaBuilder
	 * @param predicates Predicates to build
	 * @param pPackageVersionVersionNumber Path of version number
	 * @param pPackageVersionQualifier Path of qualifier
	 */
	private void buildCriteriaPackageVersion(CriteriaBuilder criteriaBuilder, List<Predicate> predicates,
			Path<String> pPackageVersionVersionNumber, Path<String> pPackageVersionQualifier) {
		if (this.overrideConfigFilter.getPackageVersion() != null
				&& StringUtils.isNotBlank(this.overrideConfigFilter.getPackageVersion())) {
			predicates.add(criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.upper(pPackageVersionVersionNumber),
							LIKE + this.overrideConfigFilter.getPackageVersion().trim().toUpperCase() + LIKE),
					criteriaBuilder.like(criteriaBuilder.upper(pPackageVersionQualifier),
							LIKE + this.overrideConfigFilter.getPackageVersion().trim().toUpperCase() + LIKE)));
		}
	}

	/**
	 * Build criteria for serie uid
	 * @param criteriaBuilder CriteriaBuilder
	 * @param predicates Predicates to build
	 * @param pLaunchConfigName Path of launch config name
	 */
	private void buildCriteriaLaunchConfig(CriteriaBuilder criteriaBuilder, List<Predicate> predicates,
			Path<String> pLaunchConfigName) {
		if (this.overrideConfigFilter.getLaunchConfig() != null
				&& StringUtils.isNotBlank(this.overrideConfigFilter.getLaunchConfig())) {
			predicates.add(criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.upper(pLaunchConfigName),
					LIKE + this.overrideConfigFilter.getLaunchConfig().trim().toUpperCase() + LIKE)));
		}
	}

	/**
	 * Build criteria for sop instance uid
	 * @param criteriaBuilder CriteriaBuilder
	 * @param predicates Predicates to build
	 * @param pTargetName Path of target name
	 */
	private void buildCriteriaTarget(CriteriaBuilder criteriaBuilder, List<Predicate> predicates,
			Path<String> pTargetName) {
		if (this.overrideConfigFilter.getGroup() != null
				&& StringUtils.isNotBlank(this.overrideConfigFilter.getGroup())) {
			predicates.add(criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.upper(pTargetName),
					LIKE + this.overrideConfigFilter.getGroup().trim().toUpperCase() + LIKE)));
		}
	}

}
