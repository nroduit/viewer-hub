package org.viewer.hub.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.WeasisIHESearchCriteria;
import org.viewer.hub.back.service.*;

@Service
@Slf4j
public class DisplaySelectViewerRuleServiceImpl implements DisplaySelectViewerRuleService {

    // Services
    private final WeasisDisplayService weasisDisplayService;
    private final OHIFDisplayService ohifDisplayService;
    private final SlicerDisplayService slicerDisplayService;
    private final RadiantDisplayService radiantDisplayService;
    private final MicroDicomDisplayService microDicomDisplayService;

    @Autowired
    public DisplaySelectViewerRuleServiceImpl(final WeasisDisplayService weasisDisplayService,
                                              final OHIFDisplayService ohifDisplayService,
                                              final SlicerDisplayService slicerDisplayService,
                                              final RadiantDisplayService radiantDisplayService,
                                              final MicroDicomDisplayService microDicomDisplayService) {
        this.weasisDisplayService = weasisDisplayService;
        this.ohifDisplayService = ohifDisplayService;
        this.slicerDisplayService = slicerDisplayService;
        this.radiantDisplayService = radiantDisplayService;
        this.microDicomDisplayService = microDicomDisplayService;
    }

    @Override
    public String getViewerUrl(String archive, String viewer, IHESearchCriteria iheSearchCriteria, String extCfg, Authentication authentication) {
        Viewer targetViewer = Viewer.fromString(viewer);
        // TODO : add other viewer for IHESearchCriteria
        return switch (targetViewer) {
            case WEASIS -> {
                WeasisIHESearchCriteria weasisIHESearchCriteria = (WeasisIHESearchCriteria) iheSearchCriteria;
                // to do JacksonConfig
                if (extCfg != null) {
                    weasisIHESearchCriteria.setExtCfg(extCfg);
                }
                yield this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(weasisIHESearchCriteria, authentication);
            }
            case OHIF -> this.ohifDisplayService.retrieveDicomUrl(iheSearchCriteria, archive);
            case SLICER -> this.slicerDisplayService.retrieveSlicerQidoLaunchUrl(iheSearchCriteria, archive);
            case RADIANT -> this.radiantDisplayService.retrieveRadiantWadoLaunchUrl(iheSearchCriteria, archive);
            case MICRODICOM -> this.microDicomDisplayService.retrieveMicroDicomWadoLaunchUrl(iheSearchCriteria, archive);
        };
    }

    @Override
    public String getViewerUrl(String archive, String viewer, ArchiveSearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication) {
        Viewer targetViewer = Viewer.fromString(viewer);
        return switch (targetViewer) {
            case WEASIS -> {
                // to do JacksonConfig
                WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
                if (extCfg != null) {
                    weasisArchiveSearchCriteria.setExtCfg(extCfg);
                }
                yield this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(weasisArchiveSearchCriteria, authentication);
            }
            case OHIF -> this.ohifDisplayService.retrieveDicomUrl(archiveSearchCriteria, archive);
            case SLICER -> this.slicerDisplayService.retrieveSlicerQidoLaunchUrl(archiveSearchCriteria, archive);
            case RADIANT -> this.radiantDisplayService.retrieveRadiantWadoLaunchUrl(archiveSearchCriteria, archive);
            case MICRODICOM -> this.microDicomDisplayService.retrieveMicroDicomWadoLaunchUrl(archiveSearchCriteria, archive);
        };
    }

    @Override
    public String getQidoViewerUrl(String archive, String viewer, ArchiveSearchCriteria archiveSearchCriteria) {
        Viewer targetViewer = Viewer.fromString(viewer);
        return switch (targetViewer) {
            case WEASIS -> {
                WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
                yield weasisDisplayService.retrieveWeasisQidoLaunchUrl(weasisArchiveSearchCriteria, archive);
            }
            case OHIF -> this.ohifDisplayService.retrieveDicomUrl(archiveSearchCriteria, archive);
            case SLICER -> this.slicerDisplayService.retrieveSlicerQidoLaunchUrl(archiveSearchCriteria, archive);
            case RADIANT -> this.radiantDisplayService.retrieveRadiantWadoLaunchUrl(archiveSearchCriteria, archive);
            case MICRODICOM ->
                    this.microDicomDisplayService.retrieveMicroDicomWadoLaunchUrl(archiveSearchCriteria, archive);
        };
    }
}
