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

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.model.ViewerAssociationModel;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.repository.ViewerAssociationRepository;
import org.viewer.hub.back.service.*;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Association service
 */
@Service
@Transactional
@Slf4j
public class ViewerAssociationServiceImpl implements ViewerAssociationService {

	// Repositories
	private final ViewerAssociationRepository viewerAssociationRepository;
	private final DicomConnectorQueryService dicomConnectorQueryService;
	private final ConnectorService connectorService;

	/**
	 * Constructor
	 * @param viewerAssociationRepository viewer association repository
	 */
	@Autowired
	public ViewerAssociationServiceImpl(final ViewerAssociationRepository viewerAssociationRepository,
										final DicomConnectorQueryService dicomConnectorQueryService,
										final ConnectorService connectorService) {
		this.viewerAssociationRepository = viewerAssociationRepository;
		this.dicomConnectorQueryService = dicomConnectorQueryService;
		this.connectorService = connectorService;
	}

	@Override
	public Page<ViewerAssociationModel> retrieveViewerAssociationModels(Pageable pageable) {
		return this.viewerAssociationRepository.findAll(pageable);
	}

	@Override
	public List<ViewerAssociationModel> retrieveViewerAssociationModels() {
		return this.viewerAssociationRepository.findAll(Sort.by(Sort.Direction.ASC, "priority"));
	}

	@Override
	public ViewerAssociationModel getViewerAssociation(String archive, Set<String> accessionNumber, Set<String> studyUID, Set<String> seriesUID, Authentication authentication) {
		List<String> retrievedModalities = null;
		boolean modalityNotFound = false;
		List<ViewerAssociationModel> viewerAssociationModels = retrieveViewerAssociationModels().reversed();
		for (ViewerAssociationModel viewerAssociationModel : viewerAssociationModels) {

			if (viewerAssociationModel.getModality() != null) {
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

				if (retrievedModalities == null || !retrievedModalities.contains(viewerAssociationModel.getModality())) {
					continue;
				}
			}

			if (viewerAssociationModel.getArchive() != null && !viewerAssociationModel.getArchive().equals(archive)) {
				continue;
			}

			return viewerAssociationModel;
		}

		return viewerAssociationModels.stream()
				.filter(association ->
						association.getArchive().equals("DEFAULT"))
				.findFirst()
				.get();
	}

	@Override
	public int countViewerAssociationModels() {
		return (int) this.viewerAssociationRepository.count();
	}

	@Override
	public void update(@Valid ViewerAssociationModel viewerAssociationModel) {
		this.viewerAssociationRepository.save(viewerAssociationModel);
	}

	@Override
	public boolean createViewerAssociationModel(ViewerAssociationModel viewerAssociationModel) {
		if (this.viewerAssociationRepository.existsByModalityAndArchive(viewerAssociationModel.getModality(), viewerAssociationModel.getArchive())) {
			return false;
		}
		boolean saved = !this.viewerAssociationRepository.saveAll(Collections.singletonList(viewerAssociationModel)).isEmpty();
		if (saved) {
			updatePriorities();
		}
		return saved;
	}

	@Override
	public void deleteViewerAssociationModel(ViewerAssociationModel viewerAssociationModel) {
		this.viewerAssociationRepository.delete(viewerAssociationModel);
		updatePriorities();
	}

	@Override
	public void updatePriority(ViewerAssociationModel draggedItem, int value) {
		List<ViewerAssociationModel> allItems = this.retrieveViewerAssociationModels();
		allItems.removeIf(e -> Objects.equals(draggedItem.getId(), e.getId()));
		allItems.add(Math.min(value, allItems.size()), draggedItem);
		updatePriorities(allItems);
	}

	private void updatePriorities() {
		updatePriorities(this.retrieveViewerAssociationModels());
	}

	private void updatePriorities(List<ViewerAssociationModel> allItems) {
		IntStream.range(0, allItems.size()).forEach(i -> allItems.get(i).setPriority(i));
		this.viewerAssociationRepository.saveAll(allItems);
	}

}
