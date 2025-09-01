package org.viewer.hub.back.service;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

/**
 * Service used to launch the application 3D Slicer
 */
public interface SlicerDisplayService {

	/**
	 * Retrieve url which will launch 3D Slicer
	 * @param searchCriteria search criteria
	 * @param authentication Authentication
	 * @return url which will launch 3D Slicer
	 */
	String retrieveSlicerLaunchUrl(@Valid SearchCriteria searchCriteria, Authentication authentication);

}