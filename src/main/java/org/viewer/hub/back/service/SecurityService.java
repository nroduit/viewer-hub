package org.viewer.hub.back.service;

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.manifest.Manifest;

public interface SecurityService {

	/**
	 * Handle the authentication to set in the Weasis manifest
	 * @param manifest Manifest to fill
	 * @param authentication Authentication if request is authenticated
	 */
	void handleManifestAuthentication(Manifest manifest, Authentication authentication);

}
