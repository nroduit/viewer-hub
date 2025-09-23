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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viewer.hub.back.model.ViewerAssociationModel;
import org.viewer.hub.back.repository.ViewerAssociationRepository;
import org.viewer.hub.back.service.*;

import java.util.*;

/**
 * Association service
 */
@Service
@Transactional
@Slf4j
public class ViewerAssociationServiceImpl implements ViewerAssociationService {

	// Repositories
	private final ViewerAssociationRepository viewerAssociationRepository;

	/**
	 * Constructor
	 * @param viewerAssociationRepository viewer association repository
	 */
	@Autowired
	public ViewerAssociationServiceImpl(final ViewerAssociationRepository viewerAssociationRepository) {
		this.viewerAssociationRepository = viewerAssociationRepository;
	}

	@Override
	public Page<ViewerAssociationModel> retrieveViewerAssociationModels(Pageable pageable) {
		return this.viewerAssociationRepository.findAll(pageable);
	}

	@Override
	public List<ViewerAssociationModel> retrieveViewerAssociationModels() {
		return this.viewerAssociationRepository.findAll();
	}

	@Override
	public ViewerAssociationModel getViewerAssociation(String archive) {
		List<ViewerAssociationModel> viewerAssociationModels = retrieveViewerAssociationModels();
		ViewerAssociationModel targetAssociation = viewerAssociationModels.stream()
				.filter(association ->
						association.getArchive().equals(archive))
				.findFirst()
				.orElse(null);
		if (targetAssociation == null) {
			targetAssociation = viewerAssociationModels.stream()
					.filter(association ->
							association.getArchive().equals("DEFAULT"))
					.findFirst()
					.get();
		}
		return targetAssociation;
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
		return !this.viewerAssociationRepository.existsByArchiveIgnoreCase(viewerAssociationModel.getArchive())
				&& !this.viewerAssociationRepository.saveAll(Collections.singletonList(viewerAssociationModel)).isEmpty();
	}

}
