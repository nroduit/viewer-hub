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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.AssociationModelFilter;
import org.weasis.manager.back.service.AssociationService;
import org.weasis.manager.back.service.GroupService;
import org.weasis.manager.back.service.LaunchPreferenceService;
import org.weasis.manager.back.service.TargetService;

import java.util.Collections;

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

}
