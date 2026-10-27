/*
 *  Copyright (c) 2022-2025 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */
package org.viewer.hub.back.util;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.shared.ApplicationConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.enums.SecurityRole;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
public final class SecurityUtil {

	/**
	 * Determines if a request is internal to Vaadin
	 * @param request Request
	 * @return true if it is a internal request
	 */
	public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
		final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
		return parameterValue != null && Stream.of(HandlerHelper.RequestType.values())
			.anyMatch(r -> r.getIdentifier().equals(parameterValue));
	}

	/**
	 * Checks if the current user is logged in
	 * @return true if the current user is logged in
	 */
	public static boolean isUserLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
				&& authentication.isAuthenticated();
	}

	/**
	 * Checks if the user is logged and is an admin
	 * @return true if the user is logged and is an admin
	 */
	public static boolean isUserAdmin() {
		return SecurityUtil.isUserLoggedIn() && SecurityContextHolder.getContext()
			.getAuthentication()
			.getAuthorities()
			.stream()
			.anyMatch(ga -> Objects.equals(ga.getAuthority(), SecurityRole.ADMIN_ROLE.getRole()));
	}

	/**
	 * Check if the role of the user can access the view
	 * @param securedClass Secured Class
	 * @return true if access is granted
	 */
	public static boolean isAccessGranted(Class<?> securedClass) {
		boolean isAccessGranted = false;

		if (isUserLoggedIn()) {
			// get the secured annotation
			Secured secured = AnnotationUtils.findAnnotation(securedClass, Secured.class);

			// allow if no roles are required
			if (secured == null) {
				isAccessGranted = true;
			}
			else {
				// lookup needed role in user roles
				List<String> allowedRoles = Arrays.asList(secured.value());

				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				isAccessGranted = authentication != null && authentication.getAuthorities()
					.stream()
					.map(GrantedAuthority::getAuthority)
					.anyMatch(allowedRoles::contains);
			}
		}
		return isAccessGranted;
	}

	/**
	 * Resolve the user name from the authentication: preferred_username claim of the
	 * JWT/OIDC token, otherwise its subject
	 * @param authentication Authentication (can be null)
	 * @return the user name or null if the call is not authenticated
	 */
	public static String resolveUsername(Authentication authentication) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
				|| !authentication.isAuthenticated()) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof Jwt jwt) {
			return StringUtils.firstNonBlank(jwt.getClaimAsString("preferred_username"), jwt.getSubject());
		}
		if (principal instanceof OidcUser oidcUser) {
			return StringUtils.firstNonBlank(oidcUser.getPreferredUsername(), oidcUser.getSubject());
		}
		return authentication.getName();
	}

	/**
	 * Sign out method
	 */
	public static void signOut() {
		try {
			VaadinServletService.getCurrentServletRequest().logout();
		}
		catch (ServletException e) {
			LOG.error("Error during logout");
		}
	}

}
