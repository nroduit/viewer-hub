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

package org.weasis.manager.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.weasis.manager.back.service.JWTSecurityService;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Service
public class JWTSecurityServiceImpl implements JWTSecurityService {

	@Override
	public Optional<String> getClientId(Principal principal) {
		return this.extractClaimFromPrincipal(principal, IdTokenClaimNames.AZP);
	}

	@Override
	public Optional<String> extractClaimFromPrincipal(Principal principal, String claim) {
		if (!StringUtils.hasText(claim)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("claim is null.");
			}
			return Optional.empty();
		}
		if (principal == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("principal is null.");
			}
			return Optional.empty();
		}
		if (principal instanceof JwtAuthenticationToken) {
			JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
			Jwt jwt = token.getToken();
			return Optional.ofNullable(jwt.getClaimAsString(claim));
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("principal is not JwtAuthenticationToken instance.");
			}
		}
		return Optional.empty();
	}

}
