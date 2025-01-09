/*
 *
 *  * Copyright (c) 2022-20xx Weasis Team and other contributors.
 *  *
 *  * This program and the accompanying materials are made available under the terms of the Eclipse
 *  * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.weasis.manager.back.controller.exception.ParameterException;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.GroupRepositoryTest;
import org.weasis.manager.back.repository.TargetRepository;
import org.weasis.manager.back.service.TargetService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TargetServiceImplTest {

	private final TargetRepository targetRepositoryMock = Mockito.mock(TargetRepository.class);

	private TargetService targetService;

	@BeforeEach
	public void setUp() {
		// TargetEntity User
		TargetEntity targetUser = new TargetEntity();
		targetUser.setName("TargetUser");
		targetUser.setType(TargetType.USER);

		// TargetEntity Host
		TargetEntity targetHost = new TargetEntity();
		targetHost.setName("TargetHost");
		targetHost.setType(TargetType.HOST);

		// TargetEntity HostGroup
		TargetEntity targetHostGroup = new TargetEntity();
		targetHostGroup.setName("TargetHostgroup");
		targetHostGroup.setType(TargetType.HOST_GROUP);

		// TargetRepository behaviour
		Mockito.when(this.targetRepositoryMock.findByType(Mockito.eq(TargetType.USER)))
			.thenReturn(Collections.singletonList(targetUser));
		Mockito.when(this.targetRepositoryMock.findByType(Mockito.eq(TargetType.HOST)))
			.thenReturn(Collections.singletonList(targetHost));
		Mockito.when(this.targetRepositoryMock.findByNameIgnoreCase(Mockito.anyString())).thenReturn(targetHostGroup);
		Mockito.when(this.targetRepositoryMock.saveAll(Mockito.anyCollection()))
			.thenReturn(List.of(GroupRepositoryTest.buildTarget(true, 1L, "Test target", TargetType.HOST)));
		Mockito.when(this.targetRepositoryMock.findAllById(Mockito.anyList()))
			.thenReturn(Collections.singletonList(targetUser));
		Mockito
			.when(this.targetRepositoryMock.findByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.eq(TargetType.USER)))
			.thenReturn(targetUser);
		Mockito
			.when(this.targetRepositoryMock.findByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST)))
			.thenReturn(targetHost);
		Mockito.when(this.targetRepositoryMock.findAll()).thenReturn(Collections.singletonList(targetHost));
		Mockito.when(this.targetRepositoryMock.existsByNameIgnoreCase(Mockito.anyString())).thenReturn(true);
		Mockito.when(this.targetRepositoryMock.findByNameContainingIgnoreCase(Mockito.anyString()))
			.thenReturn(Collections.singletonList(targetHost));
		Mockito.when(this.targetRepositoryMock.countByNameContainingIgnoreCase(Mockito.anyString())).thenReturn(1);

		// Build the mocked target service
		this.targetService = new TargetServiceImpl(this.targetRepositoryMock);
	}

	/**
	 * Test to retrieve all users in target repository by type User
	 */
	@Test
	void retrieveTargetsByTypeUserTest() {
		// Call service
		List<TargetEntity> users = this.targetService.retrieveTargetsByType(TargetType.USER);

		// Test results
		assertEquals(1, users.size());
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findByType(Mockito.any());
		assertEquals("TARGETUSER", users.get(0).getName());
	}

	/**
	 * Test to retrieve all host in target repository by type Host
	 */
	@Test
	void retrieveTargetsByTypeHostTest() {
		// Call service
		List<TargetEntity> hosts = this.targetService.retrieveTargetsByType(TargetType.HOST);

		// Test results
		assertEquals(1, hosts.size());
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findByType(Mockito.any());
		assertEquals("TARGETHOST", hosts.get(0).getName());
	}

	/**
	 * Test to retrieve all users in target repository by type User
	 */
	@Test
	void retrieveTargetByNameAndTypeUserTest() {
		// Call service
		TargetEntity user = this.targetService.retrieveTargetByNameAndType("name", TargetType.USER);

		// Test results
		assertNotNull(user);
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByNameIgnoreCaseAndType(Mockito.anyString(), Mockito.any(TargetType.class));
		assertEquals("TARGETUSER", user.getName());
	}

	/**
	 * Test to retrieve all host in target repository by type Host
	 */
	@Test
	void retrieveTargetByNameAndTypeHostTest() {
		// Call service
		TargetEntity host = this.targetService.retrieveTargetByNameAndType("name", TargetType.HOST);

		// Test results
		assertNotNull(host);
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByNameIgnoreCaseAndType(Mockito.anyString(), Mockito.any(TargetType.class));
		assertEquals("TARGETHOST", host.getName());
	}

	/**
	 * Test to retrieve target by name
	 */
	@Test
	void retrieveTargetByNameTest() {
		// Call service
		TargetEntity host = this.targetService.retrieveTargetByName("TargetHostgroup");

		// Test results
		assertNotNull(host);
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findByNameIgnoreCase(Mockito.anyString());
		assertEquals("TARGETHOSTGROUP", host.getName());
	}

	/**
	 * Test create targets
	 */
	@Test
	void createTargetsTest() {
		// Init data
		List<TargetEntity> targetsTosave = new ArrayList<>();
		TargetEntity targetEntity = GroupRepositoryTest.buildTarget(false, null, "Test target", TargetType.HOST);
		targetsTosave.add(targetEntity);

		// Call service
		List<TargetEntity> targets = this.targetService.createTargets(targetsTosave);

		// Test results
		assertEquals(1, targets.size());
		assertEquals(Long.valueOf(1), targets.get(0).getId());
		assertEquals("TEST TARGET", targets.get(0).getName());
		assertEquals(TargetType.HOST, targets.get(0).getType());
	}

	/**
	 * Test targetExistsByNameAndType
	 * <p>
	 * Expected: repository existsByNameAndType has been called
	 */
	@Test
	void targetExistsByNameAndType() {
		// Call service
		this.targetService.targetExistsByNameAndType("targetName", TargetType.HOST);

		// Test call
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.existsByNameIgnoreCaseAndType(Mockito.anyString(), Mockito.any(TargetType.class));
	}

	/**
	 * Test containsAGroup
	 * <p>
	 * Expected: detect that the list contains a group
	 */
	@Test
	void containsAGroup() {

		// TargetEntity HostGroup
		TargetEntity targetHostGroup = new TargetEntity();
		targetHostGroup.setName("TargetHostgroup");
		targetHostGroup.setType(TargetType.HOST_GROUP);

		// Call service
		boolean toTest = this.targetService.containsAGroup(Collections.singletonList(targetHostGroup));

		// Test call
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findByNameIgnoreCase(Mockito.anyString());
		assertTrue(toTest);
	}

	/**
	 * Case there is an error in the name of the group name / group name is empty / empty
	 * target list / wrong parameter in the targets Initial: Name of the target found by
	 * deserialization jackson is null
	 * <p>
	 * Expected: Parameter exception
	 */
	@Test
	void checkParametersCreateAssociationCaseWrongParameters() {
		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		// TargetEntity Host
		TargetEntity targetHost = new TargetEntity();
		targetHost.setName(null);
		targetHost.setType(TargetType.HOST);
		targets.add(targetHost);

		// Call service and Test results
		assertThrows(ParameterException.class, () -> this.targetService.checkParametersAssociation("createAssociation",
				targets, TargetType.HOST, "created"));
	}

	/**
	 * Case group name does not exist in database Name of the group does not exist in DB
	 * <p>
	 * Expected: ParameterException
	 */
	@Test
	void checkParametersCreateAssociationCaseGroupNameDoesNotExistInDb() {
		// Mock data
		Mockito
			.when(this.targetRepositoryMock.existsByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.any(TargetType.class)))
			.thenReturn(false);

		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		// TargetEntity Host
		TargetEntity targetHost = new TargetEntity();
		targetHost.setName("host");
		targetHost.setType(TargetType.HOST);
		targets.add(targetHost);

		// Call service and test results
		assertThrows(ParameterException.class, () -> this.targetService.checkParametersAssociation("createAssociation",
				targets, TargetType.HOST, "created"));
	}

	/**
	 * Case there is one of the target which is a group
	 * <p>
	 * Expected: Parameter exception
	 */
	@Test
	void checkParametersCreateAssociationCaseTargetsContainGroup() {
		// Mock data
		Mockito
			.when(this.targetRepositoryMock.existsByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.any(TargetType.class)))
			.thenReturn(true);

		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		// TargetEntity Host
		TargetEntity targetHost = new TargetEntity();
		targetHost.setName("host");
		targetHost.setType(TargetType.HOST);
		targets.add(targetHost);

		// Call service and test results
		assertThrows(ParameterException.class, () -> this.targetService.checkParametersAssociation("createAssociation",
				targets, TargetType.HOST, "created"));
	}

	/**
	 * Case there is one of the target which does not exist
	 * <p>
	 * Expected: Parameter exception
	 */
	@Test
	void checkParametersCreateAssociationCaseMemberDoesNotExist() {

		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		// TargetEntity Host
		TargetEntity targetHost = new TargetEntity();
		targetHost.setName("host");
		targetHost.setType(TargetType.HOST);
		targets.add(targetHost);

		// Mock data
		Mockito
			.when(this.targetRepositoryMock.existsByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST_GROUP)))
			.thenReturn(true);
		Mockito.when(this.targetRepositoryMock.findByNameIgnoreCase(Mockito.anyString())).thenReturn(targetHost);
		Mockito
			.when(this.targetRepositoryMock.existsByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST)))
			.thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class, () -> this.targetService.checkParametersAssociation("createAssociation",
				targets, TargetType.HOST, "created"));
	}

	/**
	 * Case no error
	 * <p>
	 * Expected: should not throw exception
	 */
	@Test
	void checkParametersCreateAssociationCaseNoError() {

		// Init data
		List<TargetEntity> targets = new ArrayList<>();
		// TargetEntity Host
		TargetEntity targetHost = new TargetEntity();
		targetHost.setName("host");
		targetHost.setType(TargetType.HOST);
		targets.add(targetHost);

		// Mock data
		Mockito
			.when(this.targetRepositoryMock.existsByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST_GROUP)))
			.thenReturn(true);
		Mockito.when(this.targetRepositoryMock.findByNameIgnoreCase(Mockito.anyString())).thenReturn(targetHost);
		Mockito
			.when(this.targetRepositoryMock.existsByNameIgnoreCaseAndType(Mockito.anyString(),
					Mockito.eq(TargetType.HOST)))
			.thenReturn(true);

		// Call service and test results
		assertDoesNotThrow(() -> this.targetService.checkParametersAssociation("createAssociation", targets,
				TargetType.HOST, "created"));
	}

	/**
	 * Test method checkParametersDeleteTarget: case wrong parameters
	 * <p>
	 * Expected: Parameter exception
	 */
	@Test
	void checkParametersDeleteTargetCaseWrongParametersTest() {
		// Call service
		assertThrows(ParameterException.class, () -> this.targetService.checkParametersDeleteTarget(""));
	}

	/**
	 * Test method checkParametersDeleteTarget: case target not existing
	 * <p>
	 * Expected: - Parameter exception
	 */
	@Test
	void checkParametersDeleteTargetCaseTargetNotExistingTest() {
		// Init data
		Instant tStart = Instant.now();

		// Mock data
		Mockito.when(this.targetRepositoryMock.existsByNameIgnoreCase(Mockito.anyString())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class, () -> this.targetService.checkParametersDeleteTarget("launchPrefered"));
	}

	/**
	 * Test method checkParametersDeleteTarget: case no error
	 * <p>
	 * Expected: - exception not thrown
	 */
	@Test
	void checkParameterDeleteTargetCaseNoErrorTest() {

		// Mock data
		Mockito.when(this.targetRepositoryMock.existsByNameIgnoreCase(Mockito.anyString())).thenReturn(true);

		// Call service and test results
		assertDoesNotThrow(() -> this.targetService.checkParametersDeleteTarget("launchPreferedName"));
	}

	/**
	 * Test method deleteTarget
	 * <p>
	 * Expected: Delete has been called
	 */
	@Test
	void deleteTargetTest() {

		// call service
		this.targetService.deleteTarget(new TargetEntity());

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).delete(Mockito.any());
	}

	/**
	 * Test method retrieveTargetsByIds
	 * <p>
	 * Expected: Mocked entities have been found
	 */
	@Test
	void retrieveTargetsByIdsTest() {
		// call service
		List<TargetEntity> targetEntities = this.targetService.retrieveTargetsByIds(Arrays.asList(1L, 2L));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findAllById(Mockito.any());
		assertEquals(1, targetEntities.size());
	}

	/**
	 * Test method retrieveTargets
	 * <p>
	 * Expected: Repository findAll has been called
	 */
	@Test
	void retrieveTargetsTest() {
		// call service
		List<TargetEntity> targetEntities = this.targetService.retrieveTargets();

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findAll();
		assertEquals(1, targetEntities.size());
	}

	/**
	 * Test method targetExistsByName
	 * <p>
	 * Expected: Repository existsByNameIgnoreCase has been called
	 */
	@Test
	void targetExistsByNameTest() {
		// call service
		boolean exists = this.targetService.targetExistsByName("Test");

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).existsByNameIgnoreCase(Mockito.anyString());
		assertTrue(exists);
	}

	/**
	 * Test method retrieveTargetsContainingName
	 * <p>
	 * Expected: Repository findByNameContainingIgnoreCase has been called
	 */
	@Test
	void retrieveTargetsContainingNameTest() {
		// call service
		List<TargetEntity> targetEntities = this.targetService.retrieveTargetsContainingName("Test");

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findByNameContainingIgnoreCase(Mockito.anyString());
		assertEquals(1, targetEntities.size());
	}

	/**
	 * Test method countTargetsContainingName
	 * <p>
	 * Expected: Repository countByNameContainingIgnoreCase has been called
	 */
	@Test
	void countTargetsContainingNameTest() {
		// call service
		int count = this.targetService.countTargetsContainingName("Test");

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.countByNameContainingIgnoreCase(Mockito.anyString());
		assertEquals(1, count);
	}

	/**
	 * Test method retrieveTargets: case no filter belongToMemberOf and Target Name,
	 * target type empty
	 * <p>
	 * Expected: Find all has been called
	 */
	@Test
	void retrieveTargetsTestCaseFindAll() {

		// call service
		this.targetService.retrieveTargets(false, new HashSet<>(), null, null, PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
	}

	/**
	 * Test method retrieveTargets: case findByNameContainingIgnoreCase
	 * <p>
	 * Expected: findByNameContainingIgnoreCase has been called
	 */
	@Test
	void retrieveTargetsTestCaseFindByNameContainingIgnoreCase() {

		// call service
		this.targetService.retrieveTargets(false, new HashSet<>(), "Test", null, PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByNameContainingIgnoreCase(Mockito.anyString(), Mockito.any(Pageable.class));
	}

	/**
	 * Test method retrieveTargets: case findByNameContainingIgnoreCaseAndType
	 * <p>
	 * Expected: findByNameContainingIgnoreCaseAndType has been called
	 */
	@Test
	void retrieveTargetsTestCaseFindByNameContainingIgnoreCaseAndType() {

		// call service
		this.targetService.retrieveTargets(false, new HashSet<>(), "Test", TargetType.USER, PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByNameContainingIgnoreCaseAndType(Mockito.anyString(), Mockito.any(TargetType.class),
					Mockito.any(Pageable.class));
	}

	/**
	 * Test method retrieveTargets: case findByIdIn
	 * <p>
	 * Expected: findByIdIn has been called
	 */
	@Test
	void retrieveTargetsTestCaseFindByIdIn() {

		// call service
		this.targetService.retrieveTargets(true, new HashSet<>(Collections.singletonList(1L)), null, null,
				PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByIdIn(Mockito.anySet(), Mockito.any(Pageable.class));
	}

	/**
	 * Test method retrieveTargets: case findByNameContainingIgnoreCaseAndIdIn
	 * <p>
	 * Expected: findByNameContainingIgnoreCaseAndIdIn has been called
	 */
	@Test
	void retrieveTargetsTestCaseFindByNameContainingIgnoreCaseAndIdIn() {

		// call service
		this.targetService.retrieveTargets(true, new HashSet<>(Collections.singletonList(1L)), "Test", null,
				PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByNameContainingIgnoreCaseAndIdIn(Mockito.anyString(), Mockito.anySet(), Mockito.any(Pageable.class));
	}

	/**
	 * Test method retrieveTargets: case findByNameContainingIgnoreCaseAndTypeAndIdIn
	 * <p>
	 * Expected: findByNameContainingIgnoreCaseAndTypeAndIdIn has been called
	 */
	@Test
	void retrieveTargetsTestCaseFindByNameContainingIgnoreCaseAndTypeAndIdIn() {

		// call service
		this.targetService.retrieveTargets(true, new HashSet<>(Collections.singletonList(1L)), "Test", TargetType.USER,
				PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.findByNameContainingIgnoreCaseAndTypeAndIdIn(Mockito.anyString(), Mockito.any(TargetType.class),
					Mockito.anySet(), Mockito.any(Pageable.class));
	}

	/**
	 * Test method countTargetEntities: case no filter belongToMemberOf and Target Name,
	 * target type empty
	 * <p>
	 * Expected: Count has been called
	 */
	@Test
	void countTargetEntitiesTestCaseCount() {

		// call service
		this.targetService.countTargetEntities(false, new HashSet<>(), null, null);

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).count();
	}

	/**
	 * Test method countTargetEntities: case countByNameContainingIgnoreCase
	 * <p>
	 * Expected: countByNameContainingIgnoreCase has been called
	 */
	@Test
	void countTargetEntitiesTestCaseCountByNameContainingIgnoreCase() {

		// call service
		this.targetService.countTargetEntities(false, new HashSet<>(), "Test", null);

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.countByNameContainingIgnoreCase(Mockito.anyString());
	}

	/**
	 * Test method countTargetEntities: case countByNameContainingIgnoreCaseAndType
	 * <p>
	 * Expected: countByNameContainingIgnoreCaseAndType has been called
	 */
	@Test
	void countTargetEntitiesTestCaseCountByNameContainingIgnoreCaseAndType() {

		// call service
		this.targetService.countTargetEntities(false, new HashSet<>(), "Test", TargetType.USER);

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.countByNameContainingIgnoreCaseAndType(Mockito.anyString(), Mockito.any(TargetType.class));
	}

	/**
	 * Test method countTargetEntities: case countByIdIn
	 * <p>
	 * Expected: countByIdIn has been called
	 */
	@Test
	void countTargetEntitiesTestCaseFindByIdIn() {

		// call service
		this.targetService.countTargetEntities(true, new HashSet<>(Collections.singletonList(1L)), null, null);

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1)).countByIdIn(Mockito.anySet());
	}

	/**
	 * Test method countTargetEntities: case countByNameContainingIgnoreCaseAndIdIn
	 * <p>
	 * Expected: countByNameContainingIgnoreCaseAndIdIn has been called
	 */
	@Test
	void countTargetEntitiesTestCaseCountByNameContainingIgnoreCaseAndIdIn() {

		// call service
		this.targetService.countTargetEntities(true, new HashSet<>(Collections.singletonList(1L)), "Test", null);

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.countByNameContainingIgnoreCaseAndIdIn(Mockito.anyString(), Mockito.anySet());
	}

	/**
	 * Test method countTargetEntities: case countByNameContainingIgnoreCaseAndTypeAndIdIn
	 * <p>
	 * Expected: countByNameContainingIgnoreCaseAndTypeAndIdIn has been called
	 */
	@Test
	void countTargetEntitiesTestCaseCountByNameContainingIgnoreCaseAndTypeAndIdIn() {

		// call service
		this.targetService.countTargetEntities(true, new HashSet<>(Collections.singletonList(1L)), "Test",
				TargetType.USER);

		// Test result
		Mockito.verify(this.targetRepositoryMock, Mockito.times(1))
			.countByNameContainingIgnoreCaseAndTypeAndIdIn(Mockito.anyString(), Mockito.any(TargetType.class),
					Mockito.anySet());
	}

}
