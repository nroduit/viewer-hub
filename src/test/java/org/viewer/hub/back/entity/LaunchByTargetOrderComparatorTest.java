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

package org.viewer.hub.back.entity;

import org.junit.jupiter.api.Test;
import org.viewer.hub.back.entity.comparator.LaunchByTargetOrderComparator;
import org.viewer.hub.back.enums.PreferredType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.LaunchRepositoryTest;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for LaunchByTargetOrderComparator
 */

class LaunchByTargetOrderComparatorTest {

	/**
	 * Test comparator used to order launch entities by targets types
	 * <p>
	 * Initial data: List<LaunchEntity>: 1: Launch => Target Type: USER => Order 3 2:
	 * Launch => Target Type: HOST => Order 1 3: Launch => Target Type: HOST GROUP =>
	 * Order 2
	 * <p>
	 * Expected:
	 * <p>
	 * Reorder the list of launch: Result: List<LaunchEntity>: 1: Launch => Target Type:
	 * HOST => Order 1 2: Launch => Target Type: HOST GROUP => Order 2 3: Launch => Target
	 * Type: USER => Order 3
	 * <p>
	 * Collections.sort(launches, new LaunchByTargetOrderComparator());
	 */
	@Test
	public void shouldOrderLaunchByTargetType() {
		// Init data
		List<LaunchEntity> launches = new LinkedList<>();

		// Create Launches
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");
		LaunchEntity launchHost = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.HOST, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");
		LaunchEntity launchHostGroup = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.HOST_GROUP,
				1L, "LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");

		// Add in the list
		launches.add(launchUser);
		launches.add(launchHost);
		launches.add(launchHostGroup);

		// Test initial order
		assertEquals(TargetType.USER, launches.get(0).getAssociatedTarget().getType());
		assertEquals(TargetType.HOST, launches.get(1).getAssociatedTarget().getType());
		assertEquals(TargetType.HOST_GROUP, launches.get(2).getAssociatedTarget().getType());

		// Call sorting
		launches.sort(new LaunchByTargetOrderComparator());

		// Test after sorting order
		assertEquals(TargetType.HOST, launches.get(0).getAssociatedTarget().getType());
		assertEquals(TargetType.HOST_GROUP, launches.get(1).getAssociatedTarget().getType());
		assertEquals(TargetType.USER, launches.get(2).getAssociatedTarget().getType());
	}

}
