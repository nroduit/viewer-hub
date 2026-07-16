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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class DisplayServiceImpl implements DisplayService {

	// Services
	private final ViewerSelectionService viewerSelectionService;

	private final WeasisDisplayService weasisDisplayService;

	private final OhifDisplayService ohifDisplayService;

	private final SlicerDisplayService slicerDisplayService;

	private final MicroDicomDisplayService microDicomDisplayService;

	private final ConnectorQueryService connectorQueryService;

	@Autowired
	public DisplayServiceImpl(final ViewerSelectionService viewerSelectionService,
			final WeasisDisplayService weasisDisplayService, final OhifDisplayService ohifDisplayService,
			final SlicerDisplayService slicerDisplayService, final MicroDicomDisplayService microDicomDisplayService,
			final ConnectorQueryService connectorQueryService) {
		this.viewerSelectionService = viewerSelectionService;
		this.weasisDisplayService = weasisDisplayService;
		this.ohifDisplayService = ohifDisplayService;
		this.slicerDisplayService = slicerDisplayService;
		this.microDicomDisplayService = microDicomDisplayService;
		this.connectorQueryService = connectorQueryService;
	}

	@Override
	public String viewerLaunchUrl(SearchCriteria searchCriteria, Authentication authentication) {
		Map<String, Set<Patient>> patientsByArchive = null;
		// Specific case for Weasis: if Weasis directly requested as a viewer, determine
		// patients later in the process in a
		// separated thread in order to increase launch speed of Weasis
		if (!Objects.equals(searchCriteria.getViewer(), ViewerType.WEASIS)) {
			// Retrieve patients map depending on search criteria
			patientsByArchive = searchCriteria instanceof ArchiveSearchCriteria
					? this.connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(
							(ArchiveSearchCriteria) searchCriteria, authentication)
					: this.connectorQueryService.retrievePatientsByArchiveWithIHESearchCriteria(
							(IHESearchCriteria) searchCriteria, authentication);
		}

		// Select viewer and retrieve viewer launch URL
		return retrieveViewerLaunchUrl(
				viewerSelectionService.retrieveViewerTypeFromViewerSelectionRules(searchCriteria, patientsByArchive),
				searchCriteria, patientsByArchive, authentication);
	}

	/**
	 * Retrieve the viewer launch URL based on the selected viewer
	 * @param viewerType The viewer type
	 * @param searchCriteria The search criteria
	 * @param patientsByArchive The patients by archive map
	 * @param authentication The authentication object
	 * @return The viewer launch URL
	 */
	private String retrieveViewerLaunchUrl(ViewerType viewerType, SearchCriteria searchCriteria,
			Map<String, Set<Patient>> patientsByArchive, Authentication authentication) {
		return switch (viewerType) {
			case WEASIS ->
				weasisDisplayService.retrieveWeasisLaunchUrl(searchCriteria, patientsByArchive, authentication);
			case OHIF -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, patientsByArchive, authentication);
			case SLICER ->
				slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, patientsByArchive, authentication);
			case MICRODICOM -> microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, patientsByArchive);
			case null -> throw new ParameterException("Invalid viewer");
		};
	}

}
