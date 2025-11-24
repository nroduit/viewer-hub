package org.viewer.hub.back.service;

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

public interface DisplaySelectViewerRuleService {

    Viewer getViewer(String archive, String viewer, SearchCriteria searchCriteria, Authentication authentication);

    String getViewerUrl(String archive, Viewer viewer, IHESearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication);

    String getViewerUrl(String archive, Viewer viewer, ArchiveSearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication);

    String getQidoViewerUrl(String archive, Viewer viewer, ArchiveSearchCriteria archiveSearchCriteria);

}
