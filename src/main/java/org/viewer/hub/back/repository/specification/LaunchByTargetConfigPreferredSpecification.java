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
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.LaunchPreferredEntity;
import org.viewer.hub.back.entity.TargetEntity;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Specification for the launch repository
 */
public class LaunchByTargetConfigPreferredSpecification implements Specification<LaunchEntity> {

	@Serial
	private static final long serialVersionUID = 5041615390952842560L;

	private static final String LAUNCH_ENTITY_PK = "launchEntityPK";

	@Valid
	private final List<TargetEntity> targets;

	@Valid
	private final List<LaunchConfigEntity> configs;

	@Valid
	private final List<LaunchPreferredEntity> preferred;

	/**
	 * Constructor with parameters
	 * @param targets Target Entities
	 * @param configs Config Entities
	 * @param preferred Preferred Entities
	 */
	public LaunchByTargetConfigPreferredSpecification(List<TargetEntity> targets, List<LaunchConfigEntity> configs,
			List<LaunchPreferredEntity> preferred) {
		this.targets = targets;
		this.configs = configs;
		this.preferred = preferred;
	}

	@Override
	public Predicate toPredicate(Root<LaunchEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		// Predicates to fill
		List<Predicate> predicates = new ArrayList<>();

		// Paths
		Path<Long> pTargetId = root.get(LAUNCH_ENTITY_PK).get("targetId");
		Path<Long> pConfigId = root.get(LAUNCH_ENTITY_PK).get("launchConfigId");
		Path<Long> pPreferredId = root.get(LAUNCH_ENTITY_PK).get("launchPreferredId");

		// Target
		if (this.targets != null && !this.targets.isEmpty()) {
			predicates.add(pTargetId.in(this.targets.stream()
				.filter(Objects::nonNull)
				.map(TargetEntity::getId)
				.collect(Collectors.toList())));
		}

		// Config
		if (this.configs != null && !this.configs.isEmpty()) {
			predicates.add(pConfigId.in(this.configs.stream()
				.filter(Objects::nonNull)
				.map(LaunchConfigEntity::getId)
				.collect(Collectors.toList())));
		}

		// Preferred
		if (this.preferred != null && !this.preferred.isEmpty()) {
			predicates.add(pPreferredId.in(this.preferred.stream()
				.filter(Objects::nonNull)
				.map(LaunchPreferredEntity::getId)
				.collect(Collectors.toList())));
		}

		return cb.and(predicates.toArray(new Predicate[] {}));
	}

}
