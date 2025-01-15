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

package org.viewer.hub.back.config.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.service.JWTSecurityService;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

	private final JWTSecurityService jwtSecurityService;

	private final String applicationName;

	@Autowired
	public AuditorAwareImpl(final JWTSecurityService jwtSecurityService,
			@Value("${spring.application.name}") String applicationName) {
		this.jwtSecurityService = jwtSecurityService;
		this.applicationName = applicationName;
	}

	@Override
	public Optional<String> getCurrentAuditor() {
		String by = this.applicationName;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() != null && authentication.isAuthenticated()) {
			by = this.jwtSecurityService.getClientId(authentication).orElse(this.applicationName);
		}
		return Optional.of(by);
	}

}