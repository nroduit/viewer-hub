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
package org.viewer.hub.back.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.viewer.hub.back.entity.I18nEntity;
import org.viewer.hub.front.views.i18n.component.I18nFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * I18n versions specification: used to look for entries depending on criteria
 */
public class I18nVersionSpecification implements Specification<I18nEntity> {

	// Like character
	private static final String LIKE = "%";

	// Criteria to look for
	private final I18nFilter i18nFilter;

	/**
	 * Constructor with filter
	 * @param i18nFilter Criteria to look for
	 */
	public I18nVersionSpecification(I18nFilter i18nFilter) {
		this.i18nFilter = i18nFilter;
	}

	@Override
	public Predicate toPredicate(Root<I18nEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		// Predicates to fill
		List<Predicate> predicates = new ArrayList<>();

		// Paths
		// Package version
		Path<String> pVersionNumber = root.get("versionNumber");
		Path<String> pQualifier = root.get("qualifier");

		// Build criteria
		if (this.i18nFilter != null) {
			// I18n version
			this.buildCriteriaI18nVersion(criteriaBuilder, predicates, pVersionNumber, pQualifier);
		}

		return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
	}

	/**
	 * Build criteria for i18n version
	 * @param criteriaBuilder CriteriaBuilder
	 * @param predicates Predicates to build
	 * @param pVersionNumber Path of version number
	 * @param pQualifier Path of qualifier
	 */
	private void buildCriteriaI18nVersion(CriteriaBuilder criteriaBuilder, List<Predicate> predicates,
			Path<String> pVersionNumber, Path<String> pQualifier) {
		if (this.i18nFilter.getI18nVersion() != null && StringUtils.isNotBlank(this.i18nFilter.getI18nVersion())) {
			predicates.add(criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.upper(pVersionNumber),
							LIKE + this.i18nFilter.getI18nVersion().trim().toUpperCase() + LIKE),
					criteriaBuilder.like(criteriaBuilder.upper(pQualifier),
							LIKE + this.i18nFilter.getI18nVersion().trim().toUpperCase() + LIKE)));
		}
	}

}
