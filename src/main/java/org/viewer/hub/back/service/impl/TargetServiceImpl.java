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

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.back.service.TargetService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service managing the targets
 */
@Service
@Transactional
@Slf4j
public class TargetServiceImpl implements TargetService {

	// Repositories
	private final TargetRepository targetRepository;

	@Autowired
	public TargetServiceImpl(TargetRepository targetRepository) {
		this.targetRepository = targetRepository;
	}

	@Override
	public List<TargetEntity> retrieveTargets() {
		return this.targetRepository.findAll();
	}

	@Override
	public List<TargetEntity> retrieveTargetsByType(TargetType targetType) {
		LOG.debug("retrieveTargetsByType");
		// Retrieve the different targets from DB
		return this.targetRepository.findByType(targetType);
	}

	@Override
	public TargetEntity retrieveTargetByNameAndType(String targetName, TargetType targetType) {
		LOG.debug("retrieveTargetByNameAndType");
		// Retrieve the DB
		return this.targetRepository.findByNameIgnoreCaseAndType(targetName, targetType);
	}

	@Override
	public TargetEntity retrieveTargetByName(String targetName) {
		LOG.debug("retrieveTargetByName");
		// Retrieve the target
		return this.targetRepository.findByNameIgnoreCase(targetName);
	}

	@Override
	public List<TargetEntity> retrieveTargetsByIds(List<Long> targetIds) {
		LOG.debug("retrieveTargetsByIds");
		// Retrieve the target
		return this.targetRepository.findAllById(targetIds);
	}

	@Override
	public List<TargetEntity> createTargets(@Valid List<TargetEntity> targets) {
		LOG.debug("createTargets");
		// Save the targets provided
		return this.targetRepository.saveAll(targets);
	}

	@Override
	public boolean targetExistsByNameAndType(String targetName, TargetType targetType) {
		return this.targetRepository.existsByNameIgnoreCaseAndType(targetName, targetType);
	}

	@Override
	public boolean targetExistsByName(String targetName) {
		return this.targetRepository.existsByNameIgnoreCase(targetName);
	}

	@Override
	public boolean containsAGroup(@Valid List<TargetEntity> targets) {
		return targets.stream()
			.anyMatch(t -> Objects.equals(TargetType.HOST_GROUP,
					this.targetRepository.findByNameIgnoreCase(t.getName()).getType())
					|| Objects.equals(TargetType.USER_GROUP,
							this.targetRepository.findByNameIgnoreCase(t.getName()).getType()));
	}

	@Override
	public void checkParametersAssociation(String groupName, @Valid List<TargetEntity> targets, TargetType targetType,
			String messageType) {

		// Determine the target type of the group
		TargetType groupType = Objects.equals(TargetType.HOST, targetType) ? TargetType.HOST_GROUP
				: TargetType.USER_GROUP;

		if (groupName == null || groupName.trim().isEmpty() || targets.isEmpty()
				|| targets.stream().anyMatch(t -> t.getName() == null)) {
			// case there is an error in the name of the group name / group name is empty
			// / empty target list / wrong parameter in the targets
			throw new ParameterException("Associations not %s: wrong parameters".formatted(messageType));
		}
		else if (!this.targetExistsByNameAndType(groupName, groupType)) {
			// case group name does not exist in database
			throw new ParameterException("Associations not %s: group does not exist".formatted(messageType));
		}
		else if (targets.stream().anyMatch(t -> !this.targetExistsByNameAndType(t.getName(), targetType))) {
			// case there is one of the target which does not exist
			throw new ParameterException(
					"Associations not %s: one of the member does not exist or does not have the correct target type:%s"
						.formatted(messageType, targetType.getCode()));
		}
		else if (this.containsAGroup(targets)) {
			// case there is one of the target which is a group
			throw new ParameterException("Associations not %s: one of the member is a group".formatted(messageType));
		}
	}

	@Override
	public void checkParametersDeleteTarget(String targetName) {

		if (targetName == null || targetName.trim().isEmpty()) {
			// case there is an error in the name of the target: empty or not filled
			throw new ParameterException("Delete not done: wrong parameters");
		}
		else if (!this.targetRepository.existsByNameIgnoreCase(targetName)) {
			// case target not existing
			throw new ParameterException("Delete not done: target not existing");
		}
	}

	@Override
	public void deleteTarget(@Valid TargetEntity target) {
		// Delete target
		this.targetRepository.delete(target);
	}

	@Override
	public Page<TargetEntity> retrieveTargets(boolean hasFilterInputBelongToMemberOf,
			Set<Long> idsFilterBelongToMemberOf, String targetName, TargetType targetType, Pageable pageable) {
		Page<TargetEntity> targetsFound;

		// Check which filters have inputs
		boolean checkFiltersTargetNameTypeEmpty = (targetName == null || targetName.trim().isEmpty())
				&& targetType == null;
		boolean checkFiltersTargetNameNotEmptyAndTypeEmpty = targetName != null && !targetName.trim().isEmpty()
				&& targetType == null;

		// Case no input in the filter BelongToMemberOf
		if (idsFilterBelongToMemberOf.isEmpty() && !hasFilterInputBelongToMemberOf) {
			if (checkFiltersTargetNameTypeEmpty) {
				targetsFound = this.targetRepository.findAll(pageable);
			}
			else if (checkFiltersTargetNameNotEmptyAndTypeEmpty) {
				targetsFound = this.targetRepository.findByNameContainingIgnoreCase(targetName, pageable);
			}
			else {
				targetsFound = this.targetRepository.findByNameContainingIgnoreCaseAndType(targetName, targetType,
						pageable);
			}
		}
		else {
			// Case inputs in the filter BelongToMemberOf
			if (checkFiltersTargetNameTypeEmpty) {
				targetsFound = this.targetRepository.findByIdIn(idsFilterBelongToMemberOf, pageable);
			}
			else if (checkFiltersTargetNameNotEmptyAndTypeEmpty) {
				targetsFound = this.targetRepository.findByNameContainingIgnoreCaseAndIdIn(targetName,
						idsFilterBelongToMemberOf, pageable);
			}
			else {
				targetsFound = this.targetRepository.findByNameContainingIgnoreCaseAndTypeAndIdIn(targetName,
						targetType, idsFilterBelongToMemberOf, pageable);
			}
		}

		return targetsFound;
	}

	@Override
	public int countTargetEntities(boolean hasFilterInputBelongToMemberOf, Set<Long> idsFilterBelongToMemberOf,
			String targetName, TargetType targetType) {
		int count;

		// Check which filters have inputs
		boolean checkFiltersTargetNameTypeEmpty = (targetName == null || targetName.trim().isEmpty())
				&& targetType == null;
		boolean checkFiltersTargetNameNotEmptyAndTypeEmpty = targetName != null && !targetName.trim().isEmpty()
				&& targetType == null;

		// Case no input in the filter BelongToMemberOf
		if (idsFilterBelongToMemberOf.isEmpty() && !hasFilterInputBelongToMemberOf) {
			if (checkFiltersTargetNameTypeEmpty) {
				count = (int) this.targetRepository.count();
			}
			else if (checkFiltersTargetNameNotEmptyAndTypeEmpty) {
				count = this.targetRepository.countByNameContainingIgnoreCase(targetName);
			}
			else {
				count = this.targetRepository.countByNameContainingIgnoreCaseAndType(targetName, targetType);
			}
		}
		else {
			// Case inputs in the filter BelongToMemberOf
			if (checkFiltersTargetNameTypeEmpty) {
				count = this.targetRepository.countByIdIn(idsFilterBelongToMemberOf);
			}
			else if (checkFiltersTargetNameNotEmptyAndTypeEmpty) {
				count = this.targetRepository.countByNameContainingIgnoreCaseAndIdIn(targetName,
						idsFilterBelongToMemberOf);
			}
			else {
				count = this.targetRepository.countByNameContainingIgnoreCaseAndTypeAndIdIn(targetName, targetType,
						idsFilterBelongToMemberOf);
			}
		}
		return count;
	}

	@Override
	public List<TargetEntity> retrieveTargetsContainingName(String name) {
		return this.targetRepository.findByNameContainingIgnoreCase(name);
	}

	@Override
	public int countTargetsContainingName(String name) {
		return this.targetRepository.countByNameContainingIgnoreCase(name);
	}

}
