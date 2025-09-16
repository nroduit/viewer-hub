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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.ViewerAssociationModel;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.*;

import java.util.List;

@Service
public class DisplaySelectViewerRuleServiceImpl implements DisplaySelectViewerRuleService {

    // Services
    private final ViewerAssociationService viewerAssociationService;
    private final WeasisDisplayService weasisDisplayService;
    private final OhifDisplayService ohifDisplayService;
    private final SlicerDisplayService slicerDisplayService;
    private final MicroDicomDisplayService microDicomDisplayService;

    @Autowired
    public DisplaySelectViewerRuleServiceImpl(final ViewerAssociationService viewerAssociationService,
                                              final WeasisDisplayService weasisDisplayService, final OhifDisplayService ohifDisplayService,
                                              final SlicerDisplayService slicerDisplayService, final MicroDicomDisplayService microDicomDisplayService) {
        this.viewerAssociationService = viewerAssociationService;
        this.weasisDisplayService = weasisDisplayService;
        this.ohifDisplayService = ohifDisplayService;
        this.slicerDisplayService = slicerDisplayService;
        this.microDicomDisplayService = microDicomDisplayService;
    }

    @Override
    public String determineViewerToDisplay(SearchCriteria searchCriteria, Authentication authentication) {
        // TODO: rules to select the viewer to display
        List<ViewerAssociationModel> viewerAssociationModels = viewerAssociationService.retrieveViewerAssociationModels();
        ViewerAssociationModel targetAssociation = viewerAssociationModels.stream()
                .filter(association ->
                        association.getArchive().equals(searchCriteria.getArchive().getFirst()))
                .findFirst()
                .orElse(null);
        if (targetAssociation == null) {
            targetAssociation = viewerAssociationModels.stream()
                    .filter(association ->
                            association.getArchive().equals("DEFAULT"))
                    .findFirst()
                    .get();
        }

        return switch (targetAssociation.getViewer()) {
            case WEASIS -> this.weasisDisplayService.retrieveWeasisLaunchUrl(searchCriteria, authentication);
            case OHIF -> this.ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, authentication);
            case SLICER -> this.slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, authentication);
            case MICRODICOM -> this.microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, authentication);
            case null -> throw new ParameterException("Invalid viewer");
        };
    }
}
