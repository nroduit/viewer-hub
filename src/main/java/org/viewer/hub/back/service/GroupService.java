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

package org.viewer.hub.back.service;

import org.viewer.hub.back.entity.GroupEntity;
import org.viewer.hub.back.entity.TargetEntity;

import java.util.List;

public interface GroupService {

	/**
	 * Returns the list of group entities for a member in parameter
	 * @param member Target member
	 * @return the list of group entities for a group in parameter
	 */
	List<GroupEntity> retrieveGroupsByMember(TargetEntity member);

	/**
	 * Returns the list of group entities for a group in parameter
	 * @param group Group to look for
	 * @return the list of group entities for a group in parameter
	 */
	List<GroupEntity> retrieveGroupsByGroup(TargetEntity group);

	/**
	 * Associate a list of targets to a group
	 * @param groupEntity Group entity
	 * @param targetEntities Targets entities
	 * @return List of groups created
	 */
	List<GroupEntity> createGroupAssociation(TargetEntity groupEntity, List<TargetEntity> targetEntities);

	/**
	 * Create a group entity
	 * @param group Target group
	 * @param target Target member
	 * @return Group created
	 */
	GroupEntity createGroup(TargetEntity group, TargetEntity target);

	/**
	 * Delete all the group association where the target group in parameter is the group
	 * of the association
	 * @param group Group to look for
	 */
	void deleteGroupAssociation(TargetEntity group);

	/**
	 * Delete all the group association where the target member in parameter is the member
	 * of the association
	 * @param member Member to look for
	 */
	void deleteMemberAssociation(TargetEntity member);

	/**
	 * Delete members of the group in parameters
	 * @param group Group
	 * @param members Members to delete
	 */
	void deleteMembers(TargetEntity group, List<TargetEntity> members);

	/**
	 * Delete groups where the member in parameter belongs to
	 * @param member Member
	 * @param groups Groups to delete
	 */
	void deleteGroups(TargetEntity member, List<TargetEntity> groups);

}
