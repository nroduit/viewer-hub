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
import org.springframework.data.jpa.domain.Specification;
import org.weasis.manager.back.entity.PreferenceEntity;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for the preference repository
 */
public class PreferenceByUserProfileModuleSpecification implements Specification<PreferenceEntity> {

	@Serial
	private static final long serialVersionUID = -4108425249170166771L;

	private final Long targetId;

	private final Long profileId;

	private final List<Long> moduleIds;

	/**
	 * Constructor with parameters
	 * @param targetId Target id
	 * @param profileId Profile Id
	 * @param moduleIds Module Ids
	 */
	public PreferenceByUserProfileModuleSpecification(Long targetId, Long profileId, List<Long> moduleIds) {
		this.targetId = targetId;
		this.profileId = profileId;
		this.moduleIds = moduleIds;
	}

	@Override
	public Predicate toPredicate(Root<PreferenceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		// Predicates to fill
		List<Predicate> predicates = new ArrayList<>();

		// Paths
		Path<Long> pProfileId = root.get("profile").get("id");
		Path<Long> pModuleId = root.get("module").get("id");
		Path<Long> pTargetId = root.get("target").get("id");

		// Target id
		if (this.targetId != null) {
			predicates.add(cb.equal(pTargetId, this.targetId));
		}

		// Profile Id
		if (this.profileId != null) {
			predicates.add(cb.equal(pProfileId, this.profileId));
		}

		// Module ids
		if (this.moduleIds != null && !this.moduleIds.isEmpty()) {
			predicates.add(pModuleId.in(this.moduleIds));
		}

		return cb.and(predicates.toArray(new Predicate[] {}));
	}

}
