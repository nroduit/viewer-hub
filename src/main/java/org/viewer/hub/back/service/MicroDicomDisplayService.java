package org.viewer.hub.back.service;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;

/**
 * Service used to launch the application Micro Dicom
 */
public interface MicroDicomDisplayService {

	/**
	 * Retrieve url which will launch Micro Dicom
	 * @param searchCriteria search criteria
	 * @param authentication Authentication
	 * @return url which will launch Micro Dicom
	 */
	String retrieveMicroDicomLaunchUrl(@Valid SearchCriteria searchCriteria, Authentication authentication);

}