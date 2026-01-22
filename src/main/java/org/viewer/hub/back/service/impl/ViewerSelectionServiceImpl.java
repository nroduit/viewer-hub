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

			if (viewerSelectionEntity.getModality() != null) {
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

				// TODO : rather define a list of modality instead of splitted comma String and in the front view use a badge component for modalities: will be done in another task
				List<String> modalities = List.of(viewerSelectionEntity.getModality().split(","));

				if (retrievedModalities == null || retrievedModalities.stream().noneMatch(modalities::contains)) {
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
		if (this.viewerSelectionRepository.existsByModalityAndArchiveAndViewer(viewerSelectionEntity.getModality(), viewerSelectionEntity.getArchive(), viewerSelectionEntity.getViewer())) {
			return false;
		}
		this.viewerSelectionRepository.save(viewerSelectionEntity);
		return true;
	}

	@Override
	public boolean createViewerSelection(ViewerSelectionEntity viewerSelectionEntity) {
		if (this.viewerSelectionRepository.existsByModalityAndArchive(viewerSelectionEntity.getModality(), viewerSelectionEntity.getArchive())) {
			return false;
		}
		boolean saved = !this.viewerSelectionRepository.saveAll(Collections.singletonList(viewerSelectionEntity)).isEmpty();
		if (saved) {
			updatePriorities();
		}
		return saved;
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
