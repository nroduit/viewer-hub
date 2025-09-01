package org.viewer.hub.back.service;

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

public interface DisplaySelectViewerRuleService {

    String determineViewerToDisplay(SearchCriteria searchCriteria, Authentication authentication);


}
