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

    @Autowired
    public DisplaySelectViewerRuleServiceImpl(final WeasisDisplayService weasisDisplayService,
                                              final OHIFDisplayService ohifDisplayService,
                                              final SlicerDisplayService slicerDisplayService,
                                              final RadiantDisplayService radiantDisplayService) {
        this.weasisDisplayService = weasisDisplayService;
        this.ohifDisplayService = ohifDisplayService;
        this.slicerDisplayService = slicerDisplayService;
        this.radiantDisplayService = radiantDisplayService;
    }

    @Override
    public String getViewerUrl(String archive, String viewer, IHESearchCriteria iheSearchCriteria, String extCfg, Authentication authentication) {
        if (Viewer.WEASIS.toString().equals(viewer)) {
            WeasisIHESearchCriteria weasisIHESearchCriteria = (WeasisIHESearchCriteria) iheSearchCriteria;
            // to do JacksonConfig
            if (extCfg != null) {
                weasisIHESearchCriteria.setExtCfg(extCfg);
            }
            return this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(weasisIHESearchCriteria, authentication);
        }
//        else if (Viewer.OHIF.toString().equals(viewer)) {
//			return this.ohifDisplayService.retrieveDicomUrl(iheSearchCriteria);
//        }
        return null;
    }

    @Override
    public String getViewerUrl(String archive, String viewer, ArchiveSearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication) {
        String redirectUrl = null;
        if (Viewer.WEASIS.toString().equals(viewer)) {
            // to do JacksonConfig
            WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
            if (extCfg != null) {
                weasisArchiveSearchCriteria.setExtCfg(extCfg);
            }
            return this.weasisDisplayService.retrieveWeasisManifestLaunchUrl(weasisArchiveSearchCriteria, authentication);
        } else if (Viewer.OHIF.toString().equals(viewer)) {
            return this.ohifDisplayService.retrieveDicomUrl(archiveSearchCriteria, archive);
        } else if (Viewer.SLICER.toString().equals(viewer)) {
            return this.slicerDisplayService.retrieveSlicerQidoLaunchUrl(archiveSearchCriteria, archive);
        } else if (Viewer.RADIANT.toString().equals(viewer)) {
            return this.radiantDisplayService.retrieveRadiantQidoLaunchUrl(archiveSearchCriteria, archive);
        }
        return null;
    }

    @Override
    public String getQidoViewerUrl(String archive, String viewer, ArchiveSearchCriteria archiveSearchCriteria) {
        if (Viewer.WEASIS.toString().equals(viewer)) {
            WeasisArchiveSearchCriteria weasisArchiveSearchCriteria = new WeasisArchiveSearchCriteria(archiveSearchCriteria);
            return weasisDisplayService.retrieveWeasisQidoLaunchUrl(weasisArchiveSearchCriteria, archive);
        }
        else if (Viewer.OHIF.toString().equals(viewer)) {
            return ohifDisplayService.retrieveDicomUrl(archiveSearchCriteria, archive);
        } else if (Viewer.SLICER.toString().equals(viewer)) {
            return this.slicerDisplayService.retrieveSlicerQidoLaunchUrl(archiveSearchCriteria, archive);
        }else if (Viewer.RADIANT.toString().equals(viewer)) {
            return this.radiantDisplayService.retrieveRadiantQidoLaunchUrl(archiveSearchCriteria, archive);
        }
        return null;
    }
}
