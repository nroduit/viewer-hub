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

package org.weasis.manager.back.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for the entity Target.
 */
public interface TargetRepository extends JpaRepository<TargetEntity, Long> {

	/**
	 * Find target entities matching the type in parameter
	 * @param type target type filter
	 * @return list of target entities matching the type in parameter
	 */
	List<TargetEntity> findByType(TargetType type);

	/**
	 * Find target entity by name
	 * @param targetName target Name
	 * @return target entity found
	 */
	TargetEntity findByNameIgnoreCase(String targetName);

	/**
	 * Find target entity by name ignore case: return an optional
	 * @param targetName target Name
	 * @return optional Target entity found
	 */
	Optional<TargetEntity> findOptionalByNameIgnoreCase(String targetName);

	/**
	 * Find target entity by name
	 * @param targetName target Name
	 * @return target entity found
	 */
	TargetEntity findByNameIgnoreCaseAndType(String targetName, TargetType targetType);

	/**
	 * Find Target by names
	 * @param targetNames target names to look for
	 * @return list of Target found
	 */
	List<TargetEntity> findByNameIn(List<String> targetNames);

	/**
	 * Find target entities by name containing parameter ignoring case
	 * @param targetName target Name
	 * @param pageable pageable
	 * @return target entities found
	 */
	Page<TargetEntity> findByNameContainingIgnoreCase(String targetName, Pageable pageable);

	/**
	 * Find target entities by name containing parameter ignoring case
	 * @param targetName target Name
	 * @return target entities found
	 */
	List<TargetEntity> findByNameContainingIgnoreCase(String targetName);

	/**
	 * Find target entities by name containing parameter ignoring case and target type
	 * @param targetName target Name
	 * @param targetType target Type
	 * @param pageable pageable
	 * @return target entities found
	 */
	Page<TargetEntity> findByNameContainingIgnoreCaseAndType(String targetName, TargetType targetType,
			Pageable pageable);

	/**
	 * Find targets depending on ids in parameter
	 * @param idsFilterBelongToMemberOf ids to look for
	 * @param pageable pageable
	 * @return target entities found
	 */
	Page<TargetEntity> findByIdIn(Set<Long> idsFilterBelongToMemberOf, Pageable pageable);

	/**
	 * Find target entities by name containing parameter ignoring case and on ids in
	 * parameter
	 * @param targetName target Name
	 * @param idsFilterBelongToMemberOf ids to look for
	 * @param pageable pageable
	 * @return target entities found
	 */
	Page<TargetEntity> findByNameContainingIgnoreCaseAndIdIn(String targetName, Set<Long> idsFilterBelongToMemberOf,
			Pageable pageable);

	/**
	 * Find target entities by name containing parameter ignoring case and target type and
	 * on ids in parameter
	 * @param targetName target Name
	 * @param targetType target Type
	 * @param idsFilterBelongToMemberOf ids to look for
	 * @param pageable pageable
	 * @return target entities found
	 */
	Page<TargetEntity> findByNameContainingIgnoreCaseAndTypeAndIdIn(String targetName, TargetType targetType,
			Set<Long> idsFilterBelongToMemberOf, Pageable pageable);

	/**
	 * Check if the target corresponding to the name in parameter exists
	 * @param targetName target name to look for
	 * @return true if the target with the given target name exists
	 */
	boolean existsByNameIgnoreCase(String targetName);

	/**
	 * Check if the target corresponding to the name and type in parameter exists
	 * @param targetName target name to look for
	 * @param targetType type to look for
	 * @return true if the target with the given target name and target type exists
	 */
	boolean existsByNameIgnoreCaseAndType(String targetName, TargetType targetType);

	/**
	 * Count the number of target entities by name containing parameter ignoring case
	 * @param targetName target Name
	 * @return number of target entities found
	 */
	int countByNameContainingIgnoreCase(String targetName);

	/**
	 * Count the number of target entities by name containing parameter ignoring case and
	 * target type
	 * @param targetName target Name
	 * @param targetType target Type
	 * @return target entities found
	 */
	int countByNameContainingIgnoreCaseAndType(String targetName, TargetType targetType);

	/**
	 * Count the number of target entities by ids in parameter
	 * @param idsFilterBelongToMemberOf ids to look for
	 * @return target entities found
	 */
	int countByIdIn(Set<Long> idsFilterBelongToMemberOf);

	/**
	 * Count the number of target entities by ids in parameter and by name containing
	 * parameter ignoring case
	 * @param targetName target Name
	 * @param idsFilterBelongToMemberOf ids to look for
	 * @return target entities found
	 */
	int countByNameContainingIgnoreCaseAndIdIn(String targetName, Set<Long> idsFilterBelongToMemberOf);

	/**
	 * Count the number of target entities by ids in parameter, by name containing
	 * parameter ignoring case and target type
	 * @param targetName target Name
	 * @param targetType target Type
	 * @param idsFilterBelongToMemberOf ids to look for
	 * @return target entities found
	 */
	int countByNameContainingIgnoreCaseAndTypeAndIdIn(String targetName, TargetType targetType,
			Set<Long> idsFilterBelongToMemberOf);

}
