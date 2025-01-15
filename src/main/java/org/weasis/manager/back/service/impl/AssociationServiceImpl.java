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

package org.weasis.manager.back.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.LaunchPreferredEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.AssociationModel;
import org.weasis.manager.back.model.AssociationModelFilter;
import org.weasis.manager.back.service.AssociationService;
import org.weasis.manager.back.service.GroupService;
import org.weasis.manager.back.service.LaunchPreferenceService;
import org.weasis.manager.back.service.TargetService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Association service
 */
@Service
@Slf4j
public class AssociationServiceImpl implements AssociationService {

	// Services
	private final TargetService targetService;

	private final GroupService groupService;

	private final LaunchPreferenceService launchService;

	/**
	 * Constructor
	 * @param targetService target service
	 * @param groupService group service
	 * @param launchService launch service
	 */
	@Autowired
	public AssociationServiceImpl(final TargetService targetService, final GroupService groupService,
			final LaunchPreferenceService launchService) {
		this.targetService = targetService;
		this.groupService = groupService;
		this.launchService = launchService;
	}

	@Override
	public Page<AssociationModel> retrieveAssociationModels(AssociationModelFilter associationModelFilter,
			Pageable pageable) {
		return this.targetService
			.retrieveTargets(this.hasFilterInputBelongToMemberOf(associationModelFilter),
					this.retrieveIdsFilterBelongToMemberOf(associationModelFilter),
					associationModelFilter.getTargetName(), associationModelFilter.getTargetType(), pageable)
			.map(targetEntity -> {
				AssociationModel associationModel = new AssociationModel();
				associationModel.setTarget(targetEntity);
				associationModel.setBelongToMemberOf(this.retrieveBelongToMemberOf(targetEntity));
				return associationModel;
			});
	}

	@Override
	public boolean hasFilterInputBelongToMemberOf(AssociationModelFilter associationModelFilter) {
		return associationModelFilter.getBelongToMemberOf() != null
				&& !associationModelFilter.getBelongToMemberOf().trim().isEmpty()
				&& associationModelFilter.getBelongToMemberOf().trim().length() > 2;
	}

	@Override
	public int countAssociationModels(AssociationModelFilter associationModelFilter) {
		return this.targetService.countTargetEntities(this.hasFilterInputBelongToMemberOf(associationModelFilter),
				this.retrieveIdsFilterBelongToMemberOf(associationModelFilter), associationModelFilter.getTargetName(),
				associationModelFilter.getTargetType());
	}

	@Override
	public List<TargetEntity> retrieveGroupsBelongsTo(@Valid TargetEntity targetEntity) {
		return this.targetService.retrieveTargetsByIds(this.groupService.retrieveGroupsByMember(targetEntity)
			.stream()
			.map(g -> g.getGroupEntityPK().getGroupId())
			.collect(Collectors.toList()));
	}

	@Override
	public void updateBelongToMemberOf(@Valid TargetEntity target, @Valid List<TargetEntity> modifiedBelongToMemberOf) {
		List<TargetEntity> originalBelongToMemberOf = this.retrieveBelongToMemberOf(target);

		if (originalBelongToMemberOf.size() > modifiedBelongToMemberOf.size()) {
			// case remove target: find the target to delete
			TargetEntity targetToRemoveAssociation = originalBelongToMemberOf.stream()
				.filter(t -> !modifiedBelongToMemberOf.contains(t))
				.findFirst()
				.orElse(null);
			if (targetToRemoveAssociation != null) {
				if (Objects.equals(targetToRemoveAssociation.getType(), TargetType.HOST)
						|| Objects.equals(targetToRemoveAssociation.getType(), TargetType.USER)) {
					this.groupService.deleteMembers(target, Collections.singletonList(targetToRemoveAssociation));
				}
				else {
					this.groupService.deleteGroups(target, Collections.singletonList(targetToRemoveAssociation));
				}
			}
		}
		else {
			// case add target: find the target to add
			TargetEntity targetToAddAssociation = modifiedBelongToMemberOf.stream()
				.filter(t -> !originalBelongToMemberOf.contains(t))
				.findFirst()
				.orElse(null);
			if (targetToAddAssociation != null) {
				if (Objects.equals(targetToAddAssociation.getType(), TargetType.HOST)
						|| Objects.equals(targetToAddAssociation.getType(), TargetType.USER)) {
					this.groupService.createGroupAssociation(target, Collections.singletonList(targetToAddAssociation));
				}
				else {
					this.groupService.createGroupAssociation(targetToAddAssociation, Collections.singletonList(target));
				}
			}
		}
	}

	@Override
	public List<TargetEntity> retrieveTargets() {
		return this.targetService.retrieveTargets();
	}

	@Override
	public boolean hasNotTooMuchTargetsContainingNameBelongToMemberOfFilter(String name) {
		return this.targetService.countTargetsContainingName(name) < 100;
	}

	@Override
	public boolean createTarget(@Valid TargetEntity targetToCreate) {
		return !this.targetService.targetExistsByName(targetToCreate.getName())
				&& !this.targetService.createTargets(Collections.singletonList(targetToCreate)).isEmpty();
	}

	@Override
	public List<LaunchEntity> retrieveLaunches(@Valid TargetEntity targetEntity) {
		// Retrieve launches/launch config/launch prefered corresponding to the target
		List<LaunchEntity> launches = this.launchService.retrieveLaunchesById(targetEntity);
		List<LaunchConfigEntity> launchConfigEntities = this.launchService.retrieveLaunchConfigsById(
				launches.stream().map(l -> l.getLaunchEntityPK().getLaunchConfigId()).collect(Collectors.toList()));
		List<LaunchPreferredEntity> launchPreferedEntities = this.launchService.retrieveLaunchPreferedById(
				launches.stream().map(l -> l.getLaunchEntityPK().getLaunchPreferredId()).collect(Collectors.toList()));

		// Fill the associated entities
		this.launchService.fillAssociatedEntitiesLaunches(Collections.singletonList(targetEntity),
				launchPreferedEntities, launchConfigEntities, launches);

		return launches;
	}

	/**
	 * Retrieve the ids of the targets to look for if the filter BelongToMemberOf has an
	 * input
	 * @param associationModelFilter Filter
	 * @return Ids found
	 */
	private Set<Long> retrieveIdsFilterBelongToMemberOf(AssociationModelFilter associationModelFilter) {
		Set<Long> idsFilterBelongToMemberOf = new HashSet<>();

		// Case input in the filter of the column BelongToMemberOf
		if (this.hasFilterInputBelongToMemberOf(associationModelFilter)
				// Limit to 100 matches in order to not slow down the application
				&& this.hasNotTooMuchTargetsContainingNameBelongToMemberOfFilter(
						associationModelFilter.getBelongToMemberOf())) {

			// Find targets containing the input in the filter of the column
			// BelongToMemberOf
			List<TargetEntity> targetsContainingNameBelongToMemberOf = this.targetService
				.retrieveTargetsContainingName(associationModelFilter.getBelongToMemberOf());

			// Retrieve the ids of targets corresponding to the input
			if (targetsContainingNameBelongToMemberOf != null) {
				// Find groups
				targetsContainingNameBelongToMemberOf.forEach(targetEntity -> {
					idsFilterBelongToMemberOf.addAll(this.groupService.retrieveGroupsByGroup(targetEntity)
						.stream()
						.map(g -> g.getGroupEntityPK().getMemberId())
						.collect(Collectors.toSet()));
					idsFilterBelongToMemberOf.addAll(this.groupService.retrieveGroupsByMember(targetEntity)
						.stream()
						.map(g -> g.getGroupEntityPK().getGroupId())
						.collect(Collectors.toSet()));
				});
			}
		}
		return idsFilterBelongToMemberOf;
	}

	/**
	 * Case user or host: retrieve groups for which the target belongs to Case group
	 * (usergroup or hostgroup): retrieve members of the target group
	 * @param targetEntity Target to evaluate
	 * @return List of targets found
	 */
	private List<TargetEntity> retrieveBelongToMemberOf(TargetEntity targetEntity) {
		return Objects.equals(targetEntity.getType(), TargetType.HOST)
				|| Objects.equals(targetEntity.getType(), TargetType.USER) ? this.retrieveGroupsBelongsTo(targetEntity)
						: this.retrieveTargetsMemberOf(targetEntity);
	}

	/**
	 * Retrieve targets member of the group in parameter
	 * @param targetEntity Group to evaluate
	 * @return List of targets member of the group
	 */
	private List<TargetEntity> retrieveTargetsMemberOf(TargetEntity targetEntity) {
		return this.targetService.retrieveTargetsByIds(this.groupService.retrieveGroupsByGroup(targetEntity)
			.stream()
			.map(g -> g.getGroupEntityPK().getMemberId())
			.collect(Collectors.toList()));
	}

}
