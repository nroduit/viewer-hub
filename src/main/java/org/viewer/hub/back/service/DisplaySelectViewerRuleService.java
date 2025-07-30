package org.viewer.hub.back.service;

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;

public interface DisplaySelectViewerRuleService {

    String getViewerUrl(String archive, String viewer, IHESearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication);

    String getViewerUrl(String archive, String viewer, ArchiveSearchCriteria archiveSearchCriteria, String extCfg, Authentication authentication);

    String getQidoViewerUrl(String archive, String viewer, ArchiveSearchCriteria archiveSearchCriteria);

}
