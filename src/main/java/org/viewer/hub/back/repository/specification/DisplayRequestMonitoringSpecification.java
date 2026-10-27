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

package org.viewer.hub.back.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Display request specification: used to look for monitoring entries depending on
 * criteria
 */
public class DisplayRequestMonitoringSpecification implements Specification<DisplayRequestMonitoringEntity> {

	// Criteria to look for
	private final DisplayRequestMonitoringFilter filter;

	/**
	 * Constructor with filter
	 * @param filter Criteria to look for (can be null)
	 */
	public DisplayRequestMonitoringSpecification(DisplayRequestMonitoringFilter filter) {
		this.filter = filter;
	}

	@Override
	public Predicate toPredicate(Root<DisplayRequestMonitoringEntity> root, CriteriaQuery<?> query,
			CriteriaBuilder cb) {
		List<Predicate> predicates = new ArrayList<>();
		if (this.filter != null) {
			if (this.filter.getFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), this.filter.getFrom()));
			}
			if (this.filter.getTo() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), this.filter.getTo()));
			}
			if (this.filter.getViewers() != null && !this.filter.getViewers().isEmpty()) {
				predicates.add(root.get("viewer").in(this.filter.getViewers()));
			}
			if (StringUtils.isNotBlank(this.filter.getUser())) {
				predicates.add(cb.or(contains(cb, root.get("jwtUser"), this.filter.getUser()),
						contains(cb, root.get("requestUser"), this.filter.getUser())));
			}
			addContains(predicates, cb, root.get("patientId"), this.filter.getPatientId());
			addContains(predicates, cb, root.get("studyUid"), this.filter.getStudyUid());
			addContains(predicates, cb, root.get("seriesUid"), this.filter.getSeriesUid());
			addContains(predicates, cb, root.get("objectUid"), this.filter.getObjectUid());
			addContains(predicates, cb, root.get("accessionNumber"), this.filter.getAccessionNumber());
			addContains(predicates, cb, root.get("archive"), this.filter.getArchive());
			addContains(predicates, cb, root.get("host"), this.filter.getHost());
			addContains(predicates, cb, root.get("client"), this.filter.getClient());
			if (StringUtils.isNotBlank(this.filter.getQueryLevel())) {
				predicates.add(cb.equal(root.get("queryLevel"), this.filter.getQueryLevel()));
			}
			if (this.filter.getSuccess() != null) {
				predicates.add(cb.equal(root.get("success"), this.filter.getSuccess()));
			}
		}
		return cb.and(predicates.toArray(new Predicate[0]));
	}

	private static void addContains(List<Predicate> predicates, CriteriaBuilder cb, Expression<String> path,
			String value) {
		if (StringUtils.isNotBlank(value)) {
			predicates.add(contains(cb, path, value));
		}
	}

	private static Predicate contains(CriteriaBuilder cb, Expression<String> path, String value) {
		String escaped = value.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return cb.like(cb.lower(path), "%" + escaped + "%", '\\');
	}

}
