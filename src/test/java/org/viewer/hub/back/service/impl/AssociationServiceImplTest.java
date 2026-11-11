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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.model.AssociationModelFilter;
import org.viewer.hub.back.service.AssociationService;
import org.viewer.hub.back.service.GroupService;
import org.viewer.hub.back.service.LaunchPreferenceService;
import org.viewer.hub.back.service.TargetService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AssociationServiceImplTest {

	private final TargetService targetServiceMock = Mockito.mock(TargetService.class);

	private final GroupService groupServiceMock = Mockito.mock(GroupService.class);

	private final LaunchPreferenceService launchPreferenceServiceMock = Mockito.mock(LaunchPreferenceService.class);

	private AssociationService associationService;

	@BeforeEach
	public void setUp() {

		// TargetService behaviour
		TargetEntity targetEntity = new TargetEntity();
		Page<TargetEntity> targetEntitiesPage = new PageImpl<>(Collections.singletonList(targetEntity),
				PageRequest.of(0, 3), 3);
		Mockito
			.when(this.targetServiceMock.retrieveTargets(Mockito.anyBoolean(), Mockito.anySet(), Mockito.anyString(),
					Mockito.any(TargetType.class), Mockito.any(Pageable.class)))
			.thenReturn(targetEntitiesPage);
		Mockito.when(this.targetServiceMock.retrieveTargetsContainingName(Mockito.anyString()))
			.thenReturn(Collections.singletonList(targetEntity));

		// Build the mocked target service
		this.associationService = new AssociationServiceImpl(this.targetServiceMock, this.groupServiceMock,
				this.launchPreferenceServiceMock);
	}

	@Test
	void retrieveAssociationModelsTest() {
		// Init data
		AssociationModelFilter associationModelFilter = new AssociationModelFilter();
		associationModelFilter.setTargetName("Target Name");
		associationModelFilter.setTargetType(TargetType.USER);
		associationModelFilter.setBelongToMemberOf("Test");

		// Call service
		this.associationService.retrieveAssociationModels(associationModelFilter, PageRequest.of(0, 3));

		// Test result
		Mockito.verify(this.targetServiceMock, Mockito.times(1))
			.retrieveTargets(Mockito.anyBoolean(), Mockito.anySet(), Mockito.anyString(), Mockito.any(TargetType.class),
					Mockito.any(Pageable.class));
	}

	@Test
	void countAssociationModelsTest() {
		// Init data
		AssociationModelFilter associationModelFilter = new AssociationModelFilter();
		associationModelFilter.setTargetName("Target Name");
		associationModelFilter.setTargetType(TargetType.USER);
		associationModelFilter.setBelongToMemberOf("Test");

		// Call service
		this.associationService.countAssociationModels(associationModelFilter);

		// Test result
		Mockito.verify(this.targetServiceMock, Mockito.times(1))
			.countTargetEntities(Mockito.anyBoolean(), Mockito.anySet(), Mockito.anyString(),
					Mockito.any(TargetType.class));
	}

	@Test
	void retrieveGroupsBelongsToTest() {
		// Init data
		TargetEntity targetEntity = new TargetEntity();

		// Call service
		this.associationService.retrieveGroupsBelongsTo(targetEntity);

		// Test result
		Mockito.verify(this.targetServiceMock, Mockito.times(1)).retrieveTargetsByIds(Mockito.anyList());
		Mockito.verify(this.groupServiceMock, Mockito.times(1)).retrieveGroupsByMember(Mockito.any(TargetEntity.class));
	}

	@Test
	void retrieveTargetsTest() {
		// Init data
		TargetEntity targetEntity = new TargetEntity();

		// Call service
		this.associationService.retrieveTargets();

		// Test result
		Mockito.verify(this.targetServiceMock, Mockito.times(1)).retrieveTargets();
	}

	@Test
	void retrieveLaunchesTest() {
		// Init data
		TargetEntity targetEntity = new TargetEntity();

		// Call service
		this.associationService.retrieveLaunches(targetEntity);

		// Test result
		Mockito.verify(this.launchPreferenceServiceMock, Mockito.times(1))
			.retrieveLaunchesById(Mockito.any(TargetEntity.class));
		Mockito.verify(this.launchPreferenceServiceMock, Mockito.times(1)).retrieveLaunchConfigsById(Mockito.anyList());
		Mockito.verify(this.launchPreferenceServiceMock, Mockito.times(1))
			.retrieveLaunchPreferedById(Mockito.anyList());
		Mockito.verify(this.launchPreferenceServiceMock, Mockito.times(1))
			.fillAssociatedEntitiesLaunches(Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList());
	}

	@Test
	void createTargetTest() {
		// Init data
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("Test");

		// Call service
		this.associationService.createTarget(targetEntity);

		// Test result
		Mockito.verify(this.targetServiceMock, Mockito.times(1)).targetExistsByName(Mockito.anyString());
		Mockito.verify(this.targetServiceMock, Mockito.times(1)).createTargets(Mockito.anyList());
	}

	@Test
	void setGroupActive_with_userGroup_should_deactivateAndPersist() {
		TargetEntity group = this.buildTarget(10L, "UserGroup", TargetType.USER_GROUP, true);
		TargetEntity persisted = this.buildTarget(10L, "UserGroup", TargetType.USER_GROUP, true);
		Mockito.when(this.targetServiceMock.retrieveTargetsByIds(Collections.singletonList(10L)))
			.thenReturn(Collections.singletonList(persisted));
		Mockito.when(this.targetServiceMock.createTargets(Mockito.anyList()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		TargetEntity result = this.associationService.setGroupActive(group, false);

		assertFalse(result.isActive());
		assertSame(persisted, result);
		Mockito.verify(this.targetServiceMock).createTargets(Collections.singletonList(persisted));
	}

	@Test
	void setGroupActive_with_hostGroup_should_reactivateAndPersist() {
		TargetEntity group = this.buildTarget(11L, "HostGroup", TargetType.HOST_GROUP, false);
		TargetEntity persisted = this.buildTarget(11L, "HostGroup", TargetType.HOST_GROUP, false);
		Mockito.when(this.targetServiceMock.retrieveTargetsByIds(Collections.singletonList(11L)))
			.thenReturn(Collections.singletonList(persisted));
		Mockito.when(this.targetServiceMock.createTargets(Mockito.anyList()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		TargetEntity result = this.associationService.setGroupActive(group, true);

		assertTrue(result.isActive());
		Mockito.verify(this.targetServiceMock).createTargets(Collections.singletonList(persisted));
	}

	@Test
	void setGroupActive_should_applyStateOnReloadedEntityNotOnStaleCopy() {
		TargetEntity stale = this.buildTarget(12L, "StaleName", TargetType.USER_GROUP, true);
		TargetEntity persisted = this.buildTarget(12L, "CurrentName", TargetType.USER_GROUP, true);
		Mockito.when(this.targetServiceMock.retrieveTargetsByIds(Collections.singletonList(12L)))
			.thenReturn(Collections.singletonList(persisted));
		Mockito.when(this.targetServiceMock.createTargets(Mockito.anyList()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		TargetEntity result = this.associationService.setGroupActive(stale, false);

		// TargetEntity normalizes names to upper case
		assertEquals("CURRENTNAME", result.getName());
		assertSame(persisted, result);
		assertFalse(result.isActive());
		assertTrue(stale.isActive());
	}

	@Test
	void setGroupActive_with_nonGroupTarget_should_throwAndNotPersist() {
		for (TargetType type : new TargetType[] { TargetType.USER, TargetType.HOST }) {
			TargetEntity target = this.buildTarget(13L, "Target", type, true);

			assertThrows(IllegalArgumentException.class, () -> this.associationService.setGroupActive(target, false));
		}

		Mockito.verify(this.targetServiceMock, Mockito.never()).retrieveTargetsByIds(Mockito.anyList());
		Mockito.verify(this.targetServiceMock, Mockito.never()).createTargets(Mockito.anyList());
	}

	@Test
	void setGroupActive_with_unknownGroup_should_throwAndNotPersist() {
		TargetEntity group = this.buildTarget(14L, "Missing", TargetType.USER_GROUP, true);
		Mockito.when(this.targetServiceMock.retrieveTargetsByIds(Collections.singletonList(14L)))
			.thenReturn(Collections.emptyList());

		assertThrows(IllegalArgumentException.class, () -> this.associationService.setGroupActive(group, false));

		Mockito.verify(this.targetServiceMock, Mockito.never()).createTargets(Mockito.anyList());
	}

	private TargetEntity buildTarget(Long id, String name, TargetType type, boolean active) {
		TargetEntity target = new TargetEntity();
		target.setId(id);
		target.setName(name);
		target.setType(type);
		target.setActive(active);
		return target;
	}

}
