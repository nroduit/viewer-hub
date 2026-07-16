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

package org.viewer.hub.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.repository.ViewerSelectionRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
class ViewerSelectionServiceImplTest {

	@Mock
	private ViewerSelectionRepository viewerSelectionRepository;

	@InjectMocks
	private ViewerSelectionServiceImpl viewerSelectionService;

	@Captor
	private ArgumentCaptor<List<ViewerSelectionEntity>> entitiesCaptor;

	private ViewerSelectionEntity defaultRule;

	private ViewerSelectionEntity weasisRule;

	private ViewerSelectionEntity ohifRule;

	private ViewerSelectionEntity slicerRule;

	@BeforeEach
	void setUp() {
		// Default rule
		defaultRule = new ViewerSelectionEntity();
		defaultRule.setId(1L);
		defaultRule.setPriority(0);
		defaultRule.setArchive("DEFAULT");
		defaultRule.setViewer(ViewerType.WEASIS);

		// Weasis rule
		weasisRule = new ViewerSelectionEntity();
		weasisRule.setId(2L);
		weasisRule.setPriority(1);
		weasisRule.setArchive("dcm4chee");
		weasisRule.setViewer(ViewerType.WEASIS);
		List<ModalityType> weasisModalities = new ArrayList<>();
		weasisModalities.add(ModalityType.CT);
		weasisModalities.add(ModalityType.MR);
		weasisRule.setModalities(weasisModalities);

		// OHIF rule
		ohifRule = new ViewerSelectionEntity();
		ohifRule.setId(3L);
		ohifRule.setPriority(2);
		ohifRule.setArchive("dcm4chee");
		ohifRule.setViewer(ViewerType.OHIF);
		List<ModalityType> ohifModalities = new ArrayList<>();
		ohifModalities.add(ModalityType.US);
		ohifRule.setModalities(ohifModalities);

		// Slicer rule
		slicerRule = new ViewerSelectionEntity();
		slicerRule.setId(4L);
		slicerRule.setPriority(3);
		slicerRule.setArchive("orthanc");
		slicerRule.setViewer(ViewerType.SLICER);

		when(viewerSelectionRepository.findByArchive("DEFAULT")).thenReturn(List.of(defaultRule));
	}

	// ========== Test retrieveViewerSelection with Pageable ==========

	@Test
	void when_retrieveViewerSelection_with_pageable_should_returnPageOfEntities() {
		// Given
		Pageable pageable = PageRequest.of(0, 10);
		List<ViewerSelectionEntity> entities = Arrays.asList(defaultRule, weasisRule, ohifRule);
		Page<ViewerSelectionEntity> page = new PageImpl<>(entities, pageable, entities.size());

		when(viewerSelectionRepository.findAll(pageable)).thenReturn(page);

		// When
		Page<ViewerSelectionEntity> result = viewerSelectionService.retrieveViewerSelection(pageable);

		// Then
		assertNotNull(result);
		assertEquals(3, result.getContent().size());
		assertEquals(3, result.getTotalElements());
		verify(viewerSelectionRepository).findAll(pageable);
	}

	// ========== Test checkDuplicate ==========

	@Test
	void when_checkDuplicate_with_existingDuplicate_should_returnTrue() {
		// Given
		String archive = "dcm4chee";
		ViewerType viewer = ViewerType.WEASIS;
		List<ModalityType> modalities = Arrays.asList(ModalityType.CT, ModalityType.MR);
		Long excludeId = 999L;

		when(viewerSelectionRepository.findByArchiveAndViewer(archive, viewer)).thenReturn(List.of(weasisRule));

		// When
		boolean result = viewerSelectionService.checkDuplicate(archive, viewer, modalities, excludeId);

		// Then
		assertTrue(result);
	}

	@Test
	void when_checkDuplicate_with_sameIdExcluded_should_returnFalse() {
		// Given
		String archive = "dcm4chee";
		ViewerType viewer = ViewerType.WEASIS;
		List<ModalityType> modalities = Arrays.asList(ModalityType.CT, ModalityType.MR);
		Long excludeId = 2L; // Same as weasisRule.getId()

		when(viewerSelectionRepository.findByArchiveAndViewer(archive, viewer)).thenReturn(List.of(weasisRule));

		// When
		boolean result = viewerSelectionService.checkDuplicate(archive, viewer, modalities, excludeId);

		// Then
		assertFalse(result);
	}

	@Test
	void when_checkDuplicate_with_differentModalities_should_returnFalse() {
		// Given
		String archive = "dcm4chee";
		ViewerType viewer = ViewerType.WEASIS;
		List<ModalityType> modalities = Arrays.asList(ModalityType.US); // Different
																		// modalities
		Long excludeId = 999L;

		when(viewerSelectionRepository.findByArchiveAndViewer(archive, viewer)).thenReturn(List.of(weasisRule));

		// When
		boolean result = viewerSelectionService.checkDuplicate(archive, viewer, modalities, excludeId);

		// Then
		assertFalse(result);
	}

	@Test
	void when_checkDuplicate_with_nullModalities_should_handleCorrectly() {
		// Given
		String archive = "dcm4chee";
		ViewerType viewer = ViewerType.WEASIS;
		List<ModalityType> modalities = null;
		Long excludeId = 999L;

		ViewerSelectionEntity entityWithNullModalities = new ViewerSelectionEntity();
		entityWithNullModalities.setId(5L);
		entityWithNullModalities.setModalities(null);

		when(viewerSelectionRepository.findByArchiveAndViewer(archive, viewer))
			.thenReturn(List.of(entityWithNullModalities));

		// When
		boolean result = viewerSelectionService.checkDuplicate(archive, viewer, modalities, excludeId);

		// Then
		assertTrue(result);
	}

	// ========== Test retrieveViewerSelection with Sort.Direction ==========

	@Test
	void when_retrieveViewerSelection_with_sortDirection_should_returnSortedList() {
		// Given
		Sort.Direction direction = Sort.Direction.DESC;
		List<ViewerSelectionEntity> entities = Arrays.asList(slicerRule, ohifRule, weasisRule, defaultRule);

		when(viewerSelectionRepository.findAll(Sort.by(direction, "priority"))).thenReturn(entities);

		// When
		List<ViewerSelectionEntity> result = viewerSelectionService.retrieveViewerSelection(direction);

		// Then
		assertNotNull(result);
		assertEquals(4, result.size());
		verify(viewerSelectionRepository).findAll(Sort.by(direction, "priority"));
	}

	// ========== Test retrieveViewerSelectionRule ==========

	@Test
	void when_retrieveViewerSelectionRule_with_viewerSpecified_should_returnRuleForThatViewerTypeFrom() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.OHIF);
		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(ViewerType.OHIF, result);
	}

	@Test
	void when_retrieveViewerSelectionRule_with_viewerTypeSpecified_should_returnSpecifiedViewer() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.SLICER);
		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(ViewerType.SLICER, result); // Returns the specified viewer directly
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRule_with_multipleArchives_should_returnWeasisRules() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		LinkedHashSet<String> archives = new LinkedHashSet<>();
		archives.add("dcm4chee");
		archives.add("orthanc");
		searchCriteria.setArchive(archives);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(ViewerType.WEASIS, result);
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRule_with_multipleArchivesInPatients_should_returnWeasisRules() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("dcm4chee", new HashSet<>());
		patientsByArchive.put("orthanc", new HashSet<>());

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(ViewerType.WEASIS, result);
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRules_with_nullPatients_should_returnDefault() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria, null);

		// Then
		assertNotNull(result);
		assertEquals(defaultRule.getViewer(), result);
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRules_with_emptyPatients_should_returnDefault() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(defaultRule.getViewer(), result);
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRule_with_matchingModalityAndArchive_should_returnMatchingRules() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();

		Patient patient = createPatientWithModality(ModalityType.CT);
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("dcm4chee", patients);

		List<ViewerSelectionEntity> allRules = Arrays.asList(slicerRule, ohifRule, weasisRule, defaultRule);
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.DESC, "priority"))).thenReturn(allRules);

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(ViewerType.WEASIS, result);
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRules_with_noMatchingModality_should_returnDefault() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();

		Patient patient = createPatientWithModality(ModalityType.XA); // Not matching any
																		// rule
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("dcm4chee", patients);

		List<ViewerSelectionEntity> allRules = Arrays.asList(slicerRule, ohifRule, weasisRule, defaultRule);
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.DESC, "priority"))).thenReturn(allRules);

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(defaultRule.getViewer(), result);
	}

	@Test
	void when_retrieveViewerTypeFromViewerSelectionRules_with_archiveALL_should_match() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();

		ViewerSelectionEntity allArchiveRule = new ViewerSelectionEntity();
		allArchiveRule.setId(10L);
		allArchiveRule.setPriority(5);
		allArchiveRule.setArchive("ALL");
		allArchiveRule.setViewer(ViewerType.OHIF);
		allArchiveRule.setModalities(new ArrayList<>());

		Patient patient = createPatientWithModality(ModalityType.CT);
		Set<Patient> patients = new HashSet<>();
		patients.add(patient);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("any-archive", patients);

		List<ViewerSelectionEntity> allRules = Arrays.asList(allArchiveRule, slicerRule, ohifRule, weasisRule,
				defaultRule);
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.DESC, "priority"))).thenReturn(allRules);

		// When
		ViewerType result = viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria,
				patientsByArchive);

		// Then
		assertNotNull(result);
		assertEquals(ViewerType.OHIF, result);
	}

	// ========== Test countViewerSelection ==========

	@Test
	void when_countViewerSelection_should_returnCount() {
		// Given
		when(viewerSelectionRepository.count()).thenReturn(5L);

		// When
		int result = viewerSelectionService.countViewerSelection();

		// Then
		assertEquals(5, result);
		verify(viewerSelectionRepository).count();
	}

	// ========== Test update ==========

	@Test
	void when_update_with_noDuplicate_should_returnTrue() {
		// Given
		ViewerSelectionEntity entityToUpdate = new ViewerSelectionEntity();
		entityToUpdate.setId(100L);
		entityToUpdate.setArchive("dcm4chee");
		entityToUpdate.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = Arrays.asList(ModalityType.XA);
		entityToUpdate.setModalities(modalities);

		when(viewerSelectionRepository.findByArchiveAndViewer("dcm4chee", ViewerType.WEASIS))
			.thenReturn(List.of(weasisRule)); // Different modalities

		// When
		boolean result = viewerSelectionService.update(entityToUpdate);

		// Then
		assertTrue(result);
		verify(viewerSelectionRepository).save(entityToUpdate);
	}

	@Test
	void when_update_with_existingDuplicate_should_returnFalse() {
		// Given
		ViewerSelectionEntity entityToUpdate = new ViewerSelectionEntity();
		entityToUpdate.setId(100L);
		entityToUpdate.setArchive("dcm4chee");
		entityToUpdate.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = Arrays.asList(ModalityType.CT, ModalityType.MR); // Same
																							// as
																							// weasisRule
		entityToUpdate.setModalities(modalities);

		when(viewerSelectionRepository.findByArchiveAndViewer("dcm4chee", ViewerType.WEASIS))
			.thenReturn(List.of(weasisRule));

		// When
		boolean result = viewerSelectionService.update(entityToUpdate);

		// Then
		assertFalse(result);
		verify(viewerSelectionRepository, never()).save(any());
	}

	@Test
	void when_update_with_sameId_should_returnTrue() {
		// Given
		ViewerSelectionEntity entityToUpdate = new ViewerSelectionEntity();
		entityToUpdate.setId(2L); // Same as weasisRule
		entityToUpdate.setArchive("dcm4chee");
		entityToUpdate.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = Arrays.asList(ModalityType.CT, ModalityType.MR);
		entityToUpdate.setModalities(modalities);

		when(viewerSelectionRepository.findByArchiveAndViewer("dcm4chee", ViewerType.WEASIS))
			.thenReturn(List.of(weasisRule));

		// When
		boolean result = viewerSelectionService.update(entityToUpdate);

		// Then
		assertTrue(result);
		verify(viewerSelectionRepository).save(entityToUpdate);
	}

	// ========== Test createViewerSelection ==========

	@Test
	void when_createViewerSelection_with_noDuplicate_should_returnTrue() {
		// Given
		ViewerSelectionEntity newEntity = new ViewerSelectionEntity();
		newEntity.setArchive("orthanc");
		newEntity.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = Arrays.asList(ModalityType.CT);
		newEntity.setModalities(modalities);

		when(viewerSelectionRepository.findByArchive("orthanc")).thenReturn(List.of());

		List<ViewerSelectionEntity> allEntities = Arrays.asList(defaultRule, weasisRule);
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.ASC, "priority"))).thenReturn(allEntities);

		// When
		boolean result = viewerSelectionService.createViewerSelection(newEntity);

		// Then
		assertTrue(result);
		verify(viewerSelectionRepository).save(newEntity);
		verify(viewerSelectionRepository).saveAll(any());
	}

	@Test
	void when_createViewerSelection_with_existingDuplicate_should_returnFalse() {
		// Given
		ViewerSelectionEntity newEntity = new ViewerSelectionEntity();
		newEntity.setArchive("dcm4chee");
		newEntity.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = Arrays.asList(ModalityType.CT, ModalityType.MR); // Same
																							// as
																							// weasisRule
		newEntity.setModalities(modalities);

		when(viewerSelectionRepository.findByArchive("dcm4chee")).thenReturn(List.of(weasisRule));

		// When
		boolean result = viewerSelectionService.createViewerSelection(newEntity);

		// Then
		assertFalse(result);
		verify(viewerSelectionRepository, never()).save(any());
	}

	// ========== Test deleteViewerSelection ==========

	@Test
	void when_deleteViewerSelection_should_deleteAndUpdatePriorities() {
		// Given
		ViewerSelectionEntity entityToDelete = weasisRule;

		List<ViewerSelectionEntity> remainingEntities = Arrays.asList(defaultRule, ohifRule);
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.ASC, "priority"))).thenReturn(remainingEntities);

		// When
		viewerSelectionService.deleteViewerSelection(entityToDelete);

		// Then
		verify(viewerSelectionRepository).delete(entityToDelete);
		verify(viewerSelectionRepository).saveAll(entitiesCaptor.capture());

		List<ViewerSelectionEntity> savedEntities = entitiesCaptor.getValue();
		assertEquals(2, savedEntities.size());
		// Verify priorities are updated
		assertEquals(0, savedEntities.get(0).getPriority());
		assertEquals(1, savedEntities.get(1).getPriority());
	}

	// ========== Test updatePriority ==========

	@Test
	void when_updatePriority_should_reorderAndUpdatePriorities() {
		// Given
		List<ViewerSelectionEntity> allEntities = new ArrayList<>(
				Arrays.asList(defaultRule, weasisRule, ohifRule, slicerRule));
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.ASC, "priority"))).thenReturn(allEntities);

		// When - Move weasisRule to position 2
		viewerSelectionService.updatePriority(weasisRule, 2);

		// Then
		verify(viewerSelectionRepository).saveAll(entitiesCaptor.capture());

		List<ViewerSelectionEntity> savedEntities = entitiesCaptor.getValue();
		assertEquals(4, savedEntities.size());
		// Verify priorities are sequential
		for (int i = 0; i < savedEntities.size(); i++) {
			assertEquals(i, savedEntities.get(i).getPriority());
		}
	}

	@Test
	void when_updatePriority_with_valueBeyondListSize_should_placeAtEnd() {
		// Given
		List<ViewerSelectionEntity> allEntities = new ArrayList<>(Arrays.asList(defaultRule, weasisRule, ohifRule));
		when(viewerSelectionRepository.findAll(Sort.by(Sort.Direction.ASC, "priority"))).thenReturn(allEntities);

		// When - Move weasisRule to position 100 (beyond size)
		viewerSelectionService.updatePriority(weasisRule, 100);

		// Then
		verify(viewerSelectionRepository).saveAll(entitiesCaptor.capture());

		List<ViewerSelectionEntity> savedEntities = entitiesCaptor.getValue();
		assertEquals(3, savedEntities.size());
		// Verify weasisRule is at the end
		assertEquals(weasisRule.getId(), savedEntities.get(2).getId());
		assertEquals(2, savedEntities.get(2).getPriority());
	}

	// ========== Helper methods ==========

	private Patient createPatientWithModality(ModalityType modalityType) {
		Patient patient = new Patient();
		patient.setPatientID("P001");

		Serie serie = Serie.builder().seriesInstanceUID("1.2.3.4.5").modality(modalityType.name()).build();

		Study study = Study.builder().studyInstanceUID("1.2.3.4").series(Set.of(serie)).build();

		patient.setStudies(Set.of(study));
		return patient;
	}

}
