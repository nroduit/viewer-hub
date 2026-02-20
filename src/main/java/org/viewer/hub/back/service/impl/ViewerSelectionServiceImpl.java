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

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerSelectionType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.repository.ViewerSelectionRepository;
import org.viewer.hub.back.service.ViewerSelectionService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
@Slf4j
public class ViewerSelectionServiceImpl implements ViewerSelectionService {

	// Repositories
	private final ViewerSelectionRepository viewerSelectionRepository;

	/**
	 * Constructor
	 * @param viewerSelectionRepository viewer selection repository
	 */
	@Autowired
	public ViewerSelectionServiceImpl(final ViewerSelectionRepository viewerSelectionRepository) {
		this.viewerSelectionRepository = viewerSelectionRepository;
	}

	@Override
	public Page<ViewerSelectionEntity> retrieveViewerSelection(Pageable pageable) {
		return this.viewerSelectionRepository.findAll(pageable);
	}

	@Override
	public boolean checkDuplicate(String archive, ViewerType viewer,
								  List<ModalityType> modalities, Long excludeId) {
		return viewerSelectionRepository.findByArchiveAndViewer(archive, viewer)
				.stream()
				.anyMatch(e -> !Objects.equals(e.getId(), excludeId) &&
						Objects.equals(
								new HashSet<>(Optional.ofNullable(e.getModalities()).orElse(Collections.emptyList())),
								new HashSet<>(Optional.ofNullable(modalities).orElse(Collections.emptyList()))));
	}

	@Override
	public List<ViewerSelectionEntity> retrieveViewerSelection(Sort.Direction prioritySortDirection) {
		return this.viewerSelectionRepository.findAll(Sort.by(prioritySortDirection, "priority"));
	}

	@Override
	public ViewerType retrieveViewerTypeFromViewerSelectionRules(SearchCriteria searchCriteria, Map<String, Set<Patient>> patientsByArchive) {
		// Retrieve default viewer
		ViewerType defaultViewer = viewerSelectionRepository.findByArchive(ViewerSelectionType.DEFAULT.name())
				.stream()
				.findFirst()
				.map(ViewerSelectionEntity::getViewer)
				.orElse(ViewerType.WEASIS);

		// If viewer is specified in search criteria, bypass selection rules
		if (searchCriteria != null && searchCriteria.getViewer() != null) {
			return searchCriteria.getViewer();
		}

		// Only Weasis supports multiple archives for now, so if multiple archives are requested, bypass selection rules and return Weasis viewer
		if ((searchCriteria != null && searchCriteria.getArchive() != null && searchCriteria.getArchive().size() > 1)
				|| (patientsByArchive != null && patientsByArchive.size() > 1)) {
			return ViewerType.WEASIS;
		}

		// If no patients found, bypass selection rules and return default viewer
		if(patientsByArchive == null || patientsByArchive.isEmpty()) {
			return defaultViewer;
		}

		// Extract all modalities from patients
		List<String> retrievedModalities = patientsByArchive.values().stream()
				.flatMap(Set::stream)
				.flatMap(patient -> patient.getStudies().stream())
				.flatMap(study -> study.getSeries().stream())
				.map(Serie::getModality)
				.distinct()
				.toList();

		// Get all viewer selection rules sorted by priority (reversed for highest priority first)
		// Find first matching rule
		return retrieveViewerSelection(Sort.Direction.DESC).stream()
				.filter(entity -> viewerSelectionRulesMatchingCondition(patientsByArchive, entity, retrievedModalities))
				.findFirst()
				.map(ViewerSelectionEntity::getViewer)
				// Fallback to default rule if no matching rule found
				.orElse(defaultViewer);
	}

	@Override
	public int countViewerSelection() {
		return (int) this.viewerSelectionRepository.count();
	}

	@Override
	public boolean update(@Valid ViewerSelectionEntity viewerSelectionEntity) {
		List<ViewerSelectionEntity> existing = viewerSelectionRepository
				.findByArchiveAndViewer(viewerSelectionEntity.getArchive(), viewerSelectionEntity.getViewer())
				.stream()
				.filter(e -> !Objects.equals(e.getId(), viewerSelectionEntity.getId()))
				.filter(e -> Objects.equals(
						new HashSet<>(Optional.ofNullable(e.getModalities()).orElse(Collections.emptyList())),
						new HashSet<>(Optional.ofNullable(viewerSelectionEntity.getModalities()).orElse(Collections.emptyList()))))
				.toList();

		if (!existing.isEmpty()) {
			return false;
		}

		viewerSelectionRepository.save(viewerSelectionEntity);
		return true;
	}

	@Override
	public boolean createViewerSelection(ViewerSelectionEntity viewerSelectionEntity) {
		// Check for existing entry with same archive and modalities
		List<ViewerSelectionEntity> existing = viewerSelectionRepository
				.findByArchive(viewerSelectionEntity.getArchive())
				.stream()
				.filter(e ->
						Objects.equals(
								new HashSet<>(Optional.ofNullable(e.getModalities()).orElse(Collections.emptyList())),
								new HashSet<>(Optional.ofNullable(viewerSelectionEntity.getModalities()).orElse(Collections.emptyList()))))
				.toList();

		// If found, do not create and return false
		if (!existing.isEmpty()) {
			return false;
		}

		viewerSelectionRepository.save(viewerSelectionEntity);
		updatePriorities();
		return true;
	}

	@Override
	public void deleteViewerSelection(ViewerSelectionEntity viewerSelectionEntity) {
		this.viewerSelectionRepository.delete(viewerSelectionEntity);
		updatePriorities();
	}

	@Override
	public void updatePriority(ViewerSelectionEntity viewerSelectionEntity, int value) {
		List<ViewerSelectionEntity> allItems = this.retrieveViewerSelection(Sort.Direction.ASC);
		allItems.removeIf(e -> Objects.equals(viewerSelectionEntity.getId(), e.getId()));
		allItems.add(Math.min(value, allItems.size()), viewerSelectionEntity);
		updatePriorities(allItems);
	}

	/**
	 * Check if viewer selection rules match the search criteria and retrieved patients modalities
	 * @param patientsByArchive Map of patients by archive
	 * @param entity Viewer selection entity to check
	 * @param retrievedModalities List of modalities retrieved from patients
	 * @return true if rules match, false otherwise
	 */
	private static boolean viewerSelectionRulesMatchingCondition(Map<String, Set<Patient>> patientsByArchive, ViewerSelectionEntity entity, List<String> retrievedModalities) {
		// Check modality partial match (at least one modality matches)
		if (entity.getModalities() != null && !entity.getModalities().isEmpty()
				&& retrievedModalities.stream().noneMatch(entity.getModalities().stream()
				.map(ModalityType::name)
				.collect(Collectors.toSet())::contains)) {
			return false;
		}

		// Check archive exact match or ALL
		return Objects.equals(entity.getArchive(), ViewerSelectionType.ALL.name())
				|| patientsByArchive.keySet().stream()
				.findFirst()
				.map(archive -> Objects.equals(entity.getArchive(), archive))
				.orElse(false);
	}

	/**
	 * Update priorities of all viewer selection entities
	 */
	private void updatePriorities() {
		updatePriorities(this.retrieveViewerSelection(Sort.Direction.ASC));
	}

	/**
	 * Update priorities of all viewer selection entities
	 * @param allItems list of all viewer selection entities
	 */
	private void updatePriorities(List<ViewerSelectionEntity> allItems) {
		IntStream.range(0, allItems.size()).forEach(i -> allItems.get(i).setPriority(i));
		this.viewerSelectionRepository.saveAll(allItems);
	}

}
