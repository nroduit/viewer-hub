/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.front.views.association;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.model.AssociationModel;
import org.weasis.manager.back.model.AssociationModelFilter;
import org.weasis.manager.back.model.Message;
import org.weasis.manager.back.model.MessageFormat;
import org.weasis.manager.back.model.MessageLevel;
import org.weasis.manager.back.model.MessageType;
import org.weasis.manager.back.service.AssociationService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Logic managing associations
 */
@Service
public class AssociationLogic {

	// View
	private AssociationView associationView;

	// Service
	private final AssociationService associationService;

	@Autowired
	public AssociationLogic(final AssociationService associationService) {
		this.associationService = associationService;
		this.associationView = null;
	}

	/**
	 * Retrieve data from backend and transform into list of models
	 * @param associationModelFilter AssociationModel Filter
	 * @param pageable Pageable
	 * @return List of models to display
	 */
	public Page<AssociationModel> retrieveAssociationModels(AssociationModelFilter associationModelFilter,
			Pageable pageable) {

		// Check match target names for column BelongToMemberOf
		if (this.associationService.hasFilterInputBelongToMemberOf(associationModelFilter) && !this.associationService
			.hasNotTooMuchTargetsContainingNameBelongToMemberOfFilter(associationModelFilter.getBelongToMemberOf())) {
			this.associationView
				.displayMessage(
						new Message(MessageLevel.INFO, MessageFormat.TEXT,
								String.format(
										"Too much target names containing: %s. Please refine your search criteria.",
										associationModelFilter.getBelongToMemberOf())),
						MessageType.NOTIFICATION_MESSAGE);
		}

		return this.associationService.retrieveAssociationModels(associationModelFilter, pageable);
	}

	/**
	 * Retrieve all targets
	 * @return all targets
	 */
	public List<TargetEntity> retrieveAllTargets() {
		return this.associationService.retrieveTargets();
	}

	/**
	 * Following a change in the grid: update the values in backend
	 * @param associationModel Values to update
	 */
	public void updateAssociationModel(AssociationModel associationModel) {

		// Update BelongToMemberOf association
		this.associationService.updateBelongToMemberOf(associationModel.getTarget(),
				associationModel.getBelongToMemberOf());
	}

	/**
	 * Count association models
	 * @param associationModelFilter Filter
	 * @return count association models
	 */
	public int countAssociationModels(AssociationModelFilter associationModelFilter) {
		return this.associationService.countAssociationModels(associationModelFilter);
	}

	/**
	 * Create a target in backend
	 * @param targetToAdd Target to create
	 * @return true if target has been created
	 */
	public boolean addTarget(@Valid TargetEntity targetToAdd) {
		return this.associationService.createTarget(targetToAdd);
	}

	/**
	 * Retrieve the launches corresponding to the target id in parameter
	 * @param target Target
	 * @return launch found
	 */
	public List<LaunchEntity> retrieveLaunches(@Valid TargetEntity target) {
		return this.associationService.retrieveLaunches(target);
	}

	/**
	 * Retrieve the launches duplicate
	 * @param launchesSelected Launches Selected
	 * @param launchesBelongsToMemberOf Launches BelongsToMemberOf
	 * @return duplicates
	 */
	public List<LaunchEntity> retrieveLaunchesDuplicates(List<LaunchEntity> launchesSelected,
			List<LaunchEntity> launchesBelongsToMemberOf) {
		return launchesSelected.stream()
			.filter(launchSelected -> launchesBelongsToMemberOf.stream()
				.anyMatch(l -> Objects.equals(l.getAssociatedPreferred(), launchSelected.getAssociatedPreferred())
						&& Objects.equals(l.getAssociatedConfig(), launchSelected.getAssociatedConfig())))
			.collect(Collectors.toList());
	}

	public AssociationView getAssociationView() {
		return this.associationView;
	}

	public void setAssociationView(AssociationView associationView) {
		this.associationView = associationView;
	}

}
