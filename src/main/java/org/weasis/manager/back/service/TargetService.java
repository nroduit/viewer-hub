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
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;

import java.util.List;
import java.util.Set;

public interface TargetService {

	/**
	 * Retrieve all the targets
	 * @return all targets
	 */
	List<TargetEntity> retrieveTargets();

	/**
	 * Returns the list of targets by the type in parameter
	 * @param targetType TargetType
	 * @return the list of targets by the type in parameter
	 */
	List<TargetEntity> retrieveTargetsByType(TargetType targetType);

	/**
	 * Returns the targets by the name and type in parameter
	 * @param targetName Target Name
	 * @param targetType TargetType
	 * @return target by the name and type in parameter
	 */
	TargetEntity retrieveTargetByNameAndType(String targetName, TargetType targetType);

	/**
	 * Returns the targets by the name in parameter
	 * @param targetName Target Name
	 * @return target by the name in parameter
	 */
	TargetEntity retrieveTargetByName(String targetName);

	/**
	 * Retrieve targets by their ids
	 * @param targetIds Target ids
	 * @return Targets found
	 */
	List<TargetEntity> retrieveTargetsByIds(List<Long> targetIds);

	/**
	 * Save the targets provided and return the created targets
	 * @param targets Targets to create
	 * @return Created targets
	 */
	List<TargetEntity> createTargets(List<TargetEntity> targets);

	/**
	 * Check if the target exist by the name and type provided in parameter
	 * @param targetName Name of the target
	 * @param targetType Target type
	 * @return true if the target with the name and type in parameter exists
	 */
	boolean targetExistsByNameAndType(String targetName, TargetType targetType);

	/**
	 * Check if the target exist by the name provided in parameter
	 * @param targetName Name of the target
	 * @return true if the target with the name in parameter exists
	 */
	boolean targetExistsByName(String targetName);

	/**
	 * Check if the list of target names in parameter contains a group
	 * @param targets List of target names
	 * @return true if there is at least one group in the list of target names
	 */
	boolean containsAGroup(List<TargetEntity> targets);

	/**
	 * Check potential errors in the input of the request
	 * @param groupName Group name
	 * @param targets Targets to associate
	 * @param targetType Target type
	 * @return if errors throw Parameter exception
	 */
	void checkParametersAssociation(String groupName, List<TargetEntity> targets, TargetType targetType,
			String messageType);

	/**
	 * Check inputs for delete target
	 * @param targetName Name of the target
	 * @return if errors throw Parameter exception
	 */
	void checkParametersDeleteTarget(String targetName);

	/**
	 * Delete target
	 * @param target Target to delete
	 */
	void deleteTarget(TargetEntity target);

	/**
	 * Retrieve page target entities depending on the filters
	 * @param hasFilterInputBelongToMemberOf Input in the filter of the column
	 * BelongToMember has been set. Start the search filter on this column only if input
	 * has more than 2 characters
	 * @param idsFilterBelongToMemberOf Ids of the targets to look for if the filter
	 * BelongToMemberOf has an input
	 * @param targetName target name
	 * @param targetType target type
	 * @param pageable Pageable
	 * @return Page target entities found
	 */
	Page<TargetEntity> retrieveTargets(boolean hasFilterInputBelongToMemberOf, Set<Long> idsFilterBelongToMemberOf,
			String targetName, TargetType targetType, Pageable pageable);

	/**
	 * Count number target entities matching depending on the filters
	 * @param hasFilterInputBelongToMemberOf Input in the filter of the column
	 * BelongToMember has been set. Start the search filter on this column only if input
	 * has more than 2 characters
	 * @param idsFilterBelongToMemberOf Ids of the targets to look for if the filter
	 * BelongToMemberOf has an input
	 * @param targetName target name
	 * @param targetType target type
	 * @return number target entities matching depending on the filters
	 */
	int countTargetEntities(boolean hasFilterInputBelongToMemberOf, Set<Long> idsFilterBelongToMemberOf,
			String targetName, TargetType targetType);

	/**
	 * Retrieve targets by name in parameter
	 * @param name Name to look for
	 * @return Targets found
	 */
	List<TargetEntity> retrieveTargetsContainingName(String name);

	/**
	 * Count targets by name in parameter
	 * @param name Name to look for
	 * @return number targets found
	 */
	int countTargetsContainingName(String name);

}
