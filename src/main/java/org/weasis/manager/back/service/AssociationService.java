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

package org.weasis.manager.back.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.model.AssociationModel;
import org.weasis.manager.back.model.AssociationModelFilter;

import java.util.List;

public interface AssociationService {

	/**
	 * Retrieve the association models depending on the filters
	 * @param associationModelFilter Filter
	 * @param pageable Pageable
	 * @return association models found
	 */
	Page<AssociationModel> retrieveAssociationModels(AssociationModelFilter associationModelFilter, Pageable pageable);

	/**
	 * Input in the filter of the column BelongToMember has been set. Start the search
	 * filter on this column only if input has more than 2 characters
	 * @param associationModelFilter Filter
	 * @return true if the search filter should be activated
	 */
	boolean hasFilterInputBelongToMemberOf(AssociationModelFilter associationModelFilter);

	/**
	 * Count the number of association models corresponding to the filters
	 * @param associationModelFilter Filter
	 * @return count number
	 */
	int countAssociationModels(AssociationModelFilter associationModelFilter);

	/**
	 * Retrieve groups for which the target in parameter belongs to
	 * @param targetEntity Target to evaluate
	 * @return List of groups for which the target belong to
	 */
	List<TargetEntity> retrieveGroupsBelongsTo(TargetEntity targetEntity);

	/**
	 * Update values in backend for BelongToMemberOf column
	 * @param target Target of the row selected
	 * @param modifiedBelongToMemberOf Modified list of BelongToMemberOf in UI before call
	 * of backend
	 */
	void updateBelongToMemberOf(TargetEntity target, List<TargetEntity> modifiedBelongToMemberOf);

	/**
	 * Retrieve all targets
	 * @return all targets
	 */
	List<TargetEntity> retrieveTargets();

	/**
	 * Limit to 100 matches in order to not slow down the application
	 * @param name Name to evaluate
	 * @return true if not too much
	 */
	boolean hasNotTooMuchTargetsContainingNameBelongToMemberOfFilter(String name);

	/**
	 * Create a target entity if name not already existing
	 * @param targetToCreate Target to create
	 * @return true if it has been created
	 */
	boolean createTarget(TargetEntity targetToCreate);

	/**
	 * Retrieve the launch models corresponding to the target id in parameter
	 * @param targetEntity Target
	 * @return launch models found
	 */
	List<LaunchEntity> retrieveLaunches(TargetEntity targetEntity);

}
