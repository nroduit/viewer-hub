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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.model.ViewerAssociationModel;
import org.viewer.hub.back.model.searchcriteria.*;
import org.viewer.hub.back.service.*;

@Service
@Slf4j
public class DisplaySelectViewerRuleServiceImpl implements DisplaySelectViewerRuleService {

    // Services
    private final ViewerAssociationService viewerAssociationService;
    private final WeasisDisplayService weasisDisplayService;
    private final OHIFDisplayService ohifDisplayService;
    private final SlicerDisplayService slicerDisplayService;
    private final RadiantDisplayService radiantDisplayService;
    private final MicroDicomDisplayService microDicomDisplayService;

    @Autowired
    public DisplaySelectViewerRuleServiceImpl(final ViewerAssociationService viewerAssociationService,
                                              final WeasisDisplayService weasisDisplayService,
                                              final OHIFDisplayService ohifDisplayService,
                                              final SlicerDisplayService slicerDisplayService,
                                              final RadiantDisplayService radiantDisplayService,
                                              final MicroDicomDisplayService microDicomDisplayService) {
        this.viewerAssociationService = viewerAssociationService;
        this.weasisDisplayService = weasisDisplayService;
        this.ohifDisplayService = ohifDisplayService;
        this.slicerDisplayService = slicerDisplayService;
        this.radiantDisplayService = radiantDisplayService;
        this.microDicomDisplayService = microDicomDisplayService;
    }

    @Override
    public Viewer getViewer(String archive, String viewer, SearchCriteria searchCriteria, Authentication authentication) {
        if (viewer != null && !viewer.isEmpty()) {
            return  Viewer.fromString(viewer);
        }
        else {
            ViewerAssociationModel targetAssociation = viewerAssociationService.getViewerAssociation(archive, searchCriteria.getAccessionNumber(), searchCriteria.getStudyUID(), searchCriteria.getSeriesUID(), authentication);
            return targetAssociation.getViewer();
        }
    }

    @Override
    public String getViewerUrl(String archive, Viewer viewer, IHESearchCriteria iheSearchCriteria, String extCfg, Authentication authentication) {
        WeasisIHESearchCriteria weasisIHESearchCriteria = (WeasisIHESearchCriteria) iheSearchCriteria;
        weasisIHESearchCriteria.setExtCfg(extCfg);
        return getViewerUrl(archive, viewer, weasisIHESearchCriteria, authentication);
    }

    @Override
    public String getViewerUrl(String archive, Viewer viewer, ArchiveSearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication) {
        WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
        weasisArchiveSearchCriteria.setExtCfg(extCfg);
        return getViewerUrl(archive, viewer, weasisArchiveSearchCriteria, authentication);
    }

    @Override
    public String getQidoViewerUrl(String archive, Viewer viewer, ArchiveSearchCriteria archiveSearchCriteria) {
        WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
        return getViewerUrl(archive, viewer, weasisArchiveSearchCriteria, null);
    }

    private String getViewerUrl(String archive, Viewer viewer, SearchCriteria searchCriteria, Authentication authentication) {
        return switch (viewer) {
            case WEASIS -> this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(searchCriteria, authentication);
            case OHIF -> this.ohifDisplayService.retrieveDicomUrl(searchCriteria, archive);
            case SLICER -> this.slicerDisplayService.retrieveSlicerQidoLaunchUrl(searchCriteria, archive);
            case RADIANT -> this.radiantDisplayService.retrieveRadiantWadoLaunchUrl(searchCriteria, archive);
            case MICRODICOM -> this.microDicomDisplayService.retrieveMicroDicomWadoLaunchUrl(searchCriteria, archive);
        };
    }

}
