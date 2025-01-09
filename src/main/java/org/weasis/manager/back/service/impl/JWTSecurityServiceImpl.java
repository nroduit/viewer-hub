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
