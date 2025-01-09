package org.weasis.manager.back.entity;

import org.junit.jupiter.api.Test;
import org.weasis.manager.back.entity.comparator.LaunchByTargetOrderComparator;
import org.weasis.manager.back.enums.PreferredType;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.LaunchRepositoryTest;

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
