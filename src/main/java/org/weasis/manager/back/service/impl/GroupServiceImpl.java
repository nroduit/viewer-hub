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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weasis.manager.back.entity.GroupEntity;
import org.weasis.manager.back.entity.GroupEntityPK;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.repository.GroupRepository;
import org.weasis.manager.back.service.GroupService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service managing the groups
 */
@Service
@Transactional
@Slf4j
public class GroupServiceImpl implements GroupService {

	// Repositories
	private final GroupRepository groupRepository;

	@Autowired
	public GroupServiceImpl(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@Override
	public List<GroupEntity> retrieveGroupsByMember(@Valid TargetEntity member) {
		LOG.debug("retrieveGroupsByMember");
		// Retrieve the different groups from DB
		return this.groupRepository.findByGroupEntityPKMemberId(Objects.nonNull(member) ? member.getId() : null);
	}

	@Override
	public List<GroupEntity> retrieveGroupsByGroup(@Valid TargetEntity group) {
		LOG.debug("retrieveGroupsByGroup");
		// Retrieve the different groups from DB
		return this.groupRepository.findByGroupEntityPKGroupId(Objects.nonNull(group) ? group.getId() : null);
	}

	@Override
	public List<GroupEntity> createGroupAssociation(@Valid TargetEntity groupEntity,
			@Valid List<TargetEntity> targetEntities) {
		LOG.debug("createGroupAssociation");

		// Create the group association
		List<GroupEntity> groups = targetEntities.stream()
			.map(t -> this.createGroup(groupEntity, t))
			.collect(Collectors.toList());

		// Save in database
		return this.groupRepository.saveAll(groups);
	}

	@Override
	public GroupEntity createGroup(@Valid TargetEntity group, @Valid TargetEntity target) {
		GroupEntity groupEntity = new GroupEntity();
		GroupEntityPK groupEntityPK = new GroupEntityPK();
		groupEntityPK.setGroupId(group.getId());
		groupEntityPK.setMemberId(target.getId());
		groupEntity.setGroupEntityPK(groupEntityPK);
		return groupEntity;
	}

	@Override
	public void deleteGroupAssociation(@Valid TargetEntity group) {
		LOG.debug("deleteGroupAssociation");

		// Find associations to delete
		List<GroupEntity> groupAssociationsToDelete = this.groupRepository.findByGroupEntityPKGroupId(group.getId());

		// Delete associations
		this.groupRepository.deleteAll(groupAssociationsToDelete);
	}

	@Override
	public void deleteMemberAssociation(@Valid TargetEntity member) {
		LOG.debug("deleteMemberAssociation");

		// Find associations to delete
		List<GroupEntity> groupAssociationsToDelete = this.groupRepository.findByGroupEntityPKMemberId(member.getId());

		// Delete associations
		this.groupRepository.deleteAll(groupAssociationsToDelete);
	}

	@Override
	public void deleteMembers(@Valid TargetEntity group, @Valid List<TargetEntity> members) {
		LOG.debug("deleteMembers");

		// Create the group association
		List<GroupEntity> groupEntities = members.stream()
			.map(t -> this.createGroup(group, t))
			.collect(Collectors.toList());

		// Delete in database
		this.groupRepository.deleteAll(groupEntities);
	}

	@Override
	public void deleteGroups(@Valid TargetEntity member, @Valid List<TargetEntity> groups) {
		LOG.debug("deleteGroups");

		// Create the group association
		List<GroupEntity> groupEntities = groups.stream()
			.map(g -> this.createGroup(g, member))
			.collect(Collectors.toList());

		// Delete in database
		this.groupRepository.deleteAll(groupEntities);
	}

}
