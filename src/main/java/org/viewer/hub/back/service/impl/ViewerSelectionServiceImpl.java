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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.repository.ViewerSelectionRepository;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.DicomConnectorQueryService;
import org.viewer.hub.back.service.ViewerSelectionService;

import java.util.*;
import java.util.stream.IntStream;

@Service
@Transactional
@Slf4j
public class ViewerSelectionServiceImpl implements ViewerSelectionService {

	public static final String DEFAULT = "DEFAULT";

	// Repositories
	private final ViewerSelectionRepository viewerSelectionRepository;
	private final DicomConnectorQueryService dicomConnectorQueryService;
	private final ConnectorService connectorService;

	/**
	 * Constructor
	 * @param viewerSelectionRepository viewer selection repository
	 */
	@Autowired
	public ViewerSelectionServiceImpl(final ViewerSelectionRepository viewerSelectionRepository,
									  final DicomConnectorQueryService dicomConnectorQueryService,
									  final ConnectorService connectorService) {
		this.viewerSelectionRepository = viewerSelectionRepository;
		this.dicomConnectorQueryService = dicomConnectorQueryService;
		this.connectorService = connectorService;
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
	public List<ViewerSelectionEntity> retrieveViewerSelection() {
		return this.viewerSelectionRepository.findAll(Sort.by(Sort.Direction.ASC, "priority"));
	}

	@Override
	// TODO to refactor with unique call to connectors: will be done in another task
	// TODO missing search by patient and instance uid: will be done in another task
	// TODO why condition on only one patient ? : to refactor and improve...
	public ViewerSelectionEntity retrieveViewerSelectionRule(String archive, Set<String> accessionNumber, Set<String> studyUID, Set<String> seriesUID, Authentication authentication) {
		List<String> retrievedModalities = null;
		boolean modalityNotFound = false;
		List<ViewerSelectionEntity> viewerSelectionEntities = retrieveViewerSelection().reversed();
		for (ViewerSelectionEntity viewerSelectionEntity : viewerSelectionEntities) {

			if (viewerSelectionEntity.getModalities() != null && !viewerSelectionEntity.getModalities().isEmpty()) {
				if (retrievedModalities == null && !modalityNotFound) {
					ConnectorProperty connector = this.connectorService.retrieveConnectorFromId(archive);
					Set<Patient> patients = new HashSet<>();
					if (seriesUID != null) {
						patients = dicomConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDicomConnector(seriesUID, connector, authentication);
					}
					else if (studyUID != null) {
						patients = dicomConnectorQueryService.retrievePatientsFromStudyInstanceUidsDicomConnector(studyUID, connector, authentication);
					}
					else if (accessionNumber != null) {
						patients = dicomConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDicomConnector(accessionNumber, connector, authentication);
					}
					if (patients.size() == 1) {
						retrievedModalities = patients.stream().findFirst().orElse(null).getStudies().stream().findFirst().orElse(null).getSeries().stream().map(serie -> serie.getModality()).toList();
					}
					if (retrievedModalities == null || retrievedModalities.isEmpty()) {
						modalityNotFound = true;
						continue;
					}
				}

				if (retrievedModalities == null || retrievedModalities.stream().noneMatch(viewerSelectionEntity.getModalities()::contains)) {
					continue;
				}
			}

			if (viewerSelectionEntity.getArchive() != null && !viewerSelectionEntity.getArchive().equals(archive)) {
				continue;
			}

			return viewerSelectionEntity;
		}

		return viewerSelectionEntities.stream()
				.filter(association ->
						association.getArchive().equals(DEFAULT))
				.findFirst()
				.get();
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
		List<ViewerSelectionEntity> allItems = this.retrieveViewerSelection();
		allItems.removeIf(e -> Objects.equals(viewerSelectionEntity.getId(), e.getId()));
		allItems.add(Math.min(value, allItems.size()), viewerSelectionEntity);
		updatePriorities(allItems);
	}

	/**
	 * Update priorities of all viewer selection entities
	 */
	private void updatePriorities() {
		updatePriorities(this.retrieveViewerSelection());
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
