package org.viewer.hub.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.*;

@Service
public class DisplaySelectViewerRuleServiceImpl implements DisplaySelectViewerRuleService {

    // Services
    private final WeasisDisplayService weasisDisplayService;
    private final OhifDisplayService ohifDisplayService;
    private final SlicerDisplayService slicerDisplayService;
    private final MicroDicomDisplayService microDicomDisplayService;

    @Autowired
    public DisplaySelectViewerRuleServiceImpl(final WeasisDisplayService weasisDisplayService, final OhifDisplayService ohifDisplayService,
                                              final SlicerDisplayService slicerDisplayService, final MicroDicomDisplayService microDicomDisplayService) {
        this.weasisDisplayService = weasisDisplayService;
        this.ohifDisplayService = ohifDisplayService;
        this.slicerDisplayService = slicerDisplayService;
        this.microDicomDisplayService = microDicomDisplayService;
    }

    @Override
    public String determineViewerToDisplay(SearchCriteria searchCriteria, Authentication authentication) {
        // TODO: rules to select the viewer to display
       return switch (searchCriteria.getViewer()) {
            case WEASIS -> this.weasisDisplayService.retrieveWeasisLaunchUrl(searchCriteria, authentication);
            case OHIF -> this.ohifDisplayService.retrieveOhifLaunchUrl(searchCriteria, authentication);
            case SLICER -> this.slicerDisplayService.retrieveSlicerLaunchUrl(searchCriteria, authentication);
            case MICRODICOM -> this.microDicomDisplayService.retrieveMicroDicomLaunchUrl(searchCriteria, authentication);
            case null -> throw new ParameterException("Invalid viewer");
        };
    }
}
