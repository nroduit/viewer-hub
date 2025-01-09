package org.weasis.manager.back.service;

import java.security.Principal;
import java.util.Optional;

public interface JWTSecurityService {

	/**
	 * Extracts the user's client ID (the client is the application the user is logged in
	 * with) from the object that represents the current user logged.
	 * @param principal : the current user logged
	 * @return the claim AZP from the principal
	 */
	Optional<String> getClientId(Principal principal);

	/**
	 * Extracts a user's claim from the object that represents the current user logged.
	 * @param principal : the current user logged
	 * @param claim : claim to extract
	 * @return the claim from the principal
	 */
	Optional<String> extractClaimFromPrincipal(Principal principal, String claim);

}
