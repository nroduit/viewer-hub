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

package org.weasis.manager.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weasis.manager.back.entity.GroupEntity;
import org.weasis.manager.back.entity.GroupEntityPK;

import java.util.List;

/**
 * Repository for the entity Group.
 */
public interface GroupRepository extends JpaRepository<GroupEntity, GroupEntityPK> {

	/**
	 * Retrieve the groups whose the target member belongs to
	 * @param memberId id of the member target
	 * @return list of GroupEntity whose the target member belongs to
	 */
	List<GroupEntity> findByGroupEntityPKMemberId(Long memberId);

	/**
	 * Retrieve the groups association with the group id in parameter
	 * @param groupId id of the group target
	 * @return list of GroupEntity with the group id in parameter
	 */
	List<GroupEntity> findByGroupEntityPKGroupId(Long groupId);

}
