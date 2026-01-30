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
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.*;

@Service
public class DisplaySelectViewerRuleServiceImpl implements DisplaySelectViewerRuleService {

    // Services
    private final ViewerSelectionService viewerSelectionService;
    private final WeasisDisplayService weasisDisplayService;
    private final OhifDisplayService ohifDisplayService;
    private final SlicerDisplayService slicerDisplayService;
    private final MicroDicomDisplayService microDicomDisplayService;

    @Autowired
    public DisplaySelectViewerRuleServiceImpl(final ViewerSelectionService viewerSelectionService,
                                              final WeasisDisplayService weasisDisplayService, final OhifDisplayService ohifDisplayService,
                                              final SlicerDisplayService slicerDisplayService, final MicroDicomDisplayService microDicomDisplayService) {
        this.viewerSelectionService = viewerSelectionService;
        this.weasisDisplayService = weasisDisplayService;
        this.ohifDisplayService = ohifDisplayService;
        this.slicerDisplayService = slicerDisplayService;
        this.microDicomDisplayService = microDicomDisplayService;
    }

    // TODO to refactor with unique call to connectors
    @Override
    public String determineViewerToDisplay(SearchCriteria searchCriteria, Authentication authentication) {
        ViewerSelectionEntity viewerSelection = viewerSelectionService.retrieveViewerSelectionRule(
                searchCriteria.getArchive().getFirst(),
                searchCriteria instanceof ArchiveSearchCriteria ? ((ArchiveSearchCriteria) searchCriteria).getAccessionNumber() : ((IHESearchCriteria) searchCriteria).getAccessionNumber(),
                searchCriteria instanceof ArchiveSearchCriteria ? ((ArchiveSearchCriteria) searchCriteria).getStudyUID() : ((IHESearchCriteria) searchCriteria).getStudyUID(),
                null,
                authentication);
        return retrieveViewerLaunchUrl(viewerSelection, searchCriteria, authentication);
    }

    /**
     * Retrieve the viewer launch URL based on the selected viewer
     *
     * @param viewerSelection The viewer selection entity
     * @param searchCriteria  The search criteria
     * @param authentication  The authentication object
     * @return The viewer launch URL
     */
    private String retrieveViewerLaunchUrl(ViewerSelectionEntity viewerSelection, SearchCriteria searchCriteria, Authentication authentication) {
        return switch (viewerSelection.getViewer()) {
            case WEASIS -> weasisDisplayService.retrieveWeasisLaunchUrl(searchCriteria, authentication);
            case OHIF -> ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, authentication);
            case SLICER -> slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, authentication);
            case MICRODICOM -> microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, authentication);
            case null -> throw new ParameterException("Invalid viewer");
        };
    }

}
