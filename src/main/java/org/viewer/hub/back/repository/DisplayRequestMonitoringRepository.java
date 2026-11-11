/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;

/**
 * Repository for the entity DisplayRequestMonitoring.
 */
public interface DisplayRequestMonitoringRepository extends JpaRepository<DisplayRequestMonitoringEntity, Long>,
		JpaSpecificationExecutor<DisplayRequestMonitoringEntity> {

	/**
	 * Requests having a non-empty client
	 * @param empty empty string, excluded from the results
	 * @return matching requests
	 */
	List<DisplayRequestMonitoringEntity> findByClientIsNotNullAndClientNot(String empty);

	/**
	 * Distinct clients of the recorded requests, sorted alphabetically
	 * @return client names
	 */
	default List<String> findDistinctClients() {
		return this.findByClientIsNotNullAndClientNot("")
			.stream()
			.map(DisplayRequestMonitoringEntity::getClient)
			.distinct()
			.sorted()
			.toList();
	}

	/**
	 * Delete the requests recorded before the given date (retention)
	 * @param limit requests older than this date are deleted
	 * @return number of requests deleted
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM DisplayRequestMonitoringEntity e WHERE e.createdAt < :limit")
	int deleteOlderThan(@Param("limit") LocalDateTime limit);

}
