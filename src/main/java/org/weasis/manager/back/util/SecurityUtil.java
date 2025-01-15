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
package org.weasis.manager.back.util;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.shared.ApplicationConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.weasis.manager.back.enums.SecurityRole;

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
