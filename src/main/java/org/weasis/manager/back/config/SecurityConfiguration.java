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

package org.weasis.manager.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.constant.Token;
import org.weasis.manager.back.security.OpenIdConnectLogoutHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final String LOGIN_URL = "/login";

	@Value("${spring.security.oauth2.client.provider.keycloak.jwk-set-uri}")
	private String jwkSetUri;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// Disables cross-site request forgery (CSRF) protection for main route and
			// login
			.csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher(EndPoint.ALL_REMAINING_PATH),
					AntPathRequestMatcher.antMatcher(LOGIN_URL)))
			.authorizeHttpRequests(authorize -> authorize
				// TODO: currently no security on these endpoints: find a way for Weasis
				// (kiosque) + wait for secured client requests
				.requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**"),
						// Controllers
						AntPathRequestMatcher.antMatcher(EndPoint.DISPLAY_PATH + EndPoint.WEASIS_PATH),
						AntPathRequestMatcher
							.antMatcher(EndPoint.DISPLAY_PATH + EndPoint.IHE_INVOKE_IMAGE_DISPLAY_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.GROUP_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.LAUNCH_CONFIG_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.PREFERENCES_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.MANIFEST_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.MODULES_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.OVERRIDE_CONFIG_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.PREFERENCES_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.STATISTIC_PATH + EndPoint.ALL_REMAINING_PATH),
						AntPathRequestMatcher.antMatcher(EndPoint.TARGET_PATH + EndPoint.ALL_REMAINING_PATH),
						// Resources for weasis
						AntPathRequestMatcher.antMatcher(EndPoint.WEASIS_PATH + EndPoint.ALL_REMAINING_PATH))
				.permitAll()
				.requestMatchers(EndpointRequest.to(ShutdownEndpoint.class))
				.denyAll()
				.anyRequest()
				.authenticated())
			.oauth2Login(oauth2Login -> oauth2Login.userInfoEndpoint(
					// Extract roles from access token
					userInfoEndpoint -> userInfoEndpoint.oidcUserService(this.oidcUserService())))
			// Handle logout
			.logout(logout -> logout.addLogoutHandler(new OpenIdConnectLogoutHandler()));

		return http.build();
	}

	/**
	 * Extract roles from access token and set authorities in the authenticated user
	 * @return OAuth2UserService
	 */
	private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
		final OidcUserService oidcUserService = new OidcUserService();
		return userRequest -> {
			// Get the user from the request
			OidcUser oidcUser = oidcUserService.loadUser(userRequest);

			// Decode access token
			Jwt jwt = this.decodeAccessToken(userRequest.getAccessToken());

			// Retrieve roles from access token
			Set<SimpleGrantedAuthority> grantedAuthoritiesFromAccessToken = this.retrieveRolesFromAccessToken(jwt);

			// Update the user with roles found
			return new DefaultOidcUser(grantedAuthoritiesFromAccessToken, oidcUser.getIdToken(),
					oidcUser.getUserInfo());
		};
	}

	/**
	 * Decode access token
	 * @param accessToken Access token to decode
	 * @return access token decoded
	 */
	private Jwt decodeAccessToken(OAuth2AccessToken accessToken) {
		return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build().decode(accessToken.getTokenValue());
	}

	/**
	 * Retrieve roles from access token
	 * @param jwt access token
	 * @return Roles found
	 */
	private Set<SimpleGrantedAuthority> retrieveRolesFromAccessToken(Jwt jwt) {
		// Build roles
		return ((List<String>) ((Map<String, Object>) ((Map<String, Object>) jwt.getClaims().get(Token.RESOURCE_ACCESS))
			.get(Token.RESOURCE_NAME)).get(Token.ROLES)).stream()
			.map(roleName -> Token.PREFIX_ROLE + roleName)
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toSet());
	}

}