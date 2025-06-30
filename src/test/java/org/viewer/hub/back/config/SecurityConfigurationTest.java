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

package org.viewer.hub.back.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.viewer.hub.back.constant.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationTest {

	private SecurityConfiguration securityConfiguration;

	@BeforeEach
	public void setUp() {
		securityConfiguration = new SecurityConfiguration();
	}

	@Test
	void givenNullResourceAccess_whenRetrieveRolesFromAccessToken_thenShouldReturnEmptySet() {
		Jwt jwt = new Jwt("a", null, null, Map.of("b", "c"), Map.of("d", "e"));
		Set<SimpleGrantedAuthority> rolesFromAccessToken = this.securityConfiguration.retrieveRolesFromAccessToken(jwt);
		assertThat(rolesFromAccessToken).isEmpty();
	}

	@Test
	void givenNullResourceName_whenRetrieveRolesFromAccessToken_thenShouldReturnEmptySet() {
		Jwt jwt = new Jwt("a", null, null, Map.of("b", "c"), Map.of(Token.RESOURCE_ACCESS, Map.of("d", "e")));
		Set<SimpleGrantedAuthority> rolesFromAccessToken = this.securityConfiguration.retrieveRolesFromAccessToken(jwt);
		assertThat(rolesFromAccessToken).isEmpty();
	}

	@Test
	void givenNullRoles_whenRetrieveRolesFromAccessToken_thenShouldReturnEmptySet() {
		Jwt jwt = new Jwt("a", null, null, Map.of("b", "c"),
				Map.of(Token.RESOURCE_ACCESS, Map.of(Token.RESOURCE_NAME, Map.of("d", "e"))));
		Set<SimpleGrantedAuthority> rolesFromAccessToken = this.securityConfiguration.retrieveRolesFromAccessToken(jwt);
		assertThat(rolesFromAccessToken).isEmpty();
	}

	@Test
	void givenNoRoles_whenRetrieveRolesFromAccessToken_thenShouldReturnEmptySet() {
		Jwt jwt = new Jwt("a", null, null, Map.of("b", "c"), Map.of(Token.RESOURCE_ACCESS,
				Map.of(Token.RESOURCE_NAME, Map.of(Token.ROLES, new ArrayList<String>()))));
		Set<SimpleGrantedAuthority> rolesFromAccessToken = this.securityConfiguration.retrieveRolesFromAccessToken(jwt);
		assertThat(rolesFromAccessToken).isEmpty();
	}

	@Test
	void givenValidJwt_whenRetrieveRolesFromAccessToken_thenShouldReturnNotEmptySet() {
		Jwt jwt = new Jwt("a", null, null, Map.of("b", "c"),
				Map.of(Token.RESOURCE_ACCESS, Map.of(Token.RESOURCE_NAME, Map.of(Token.ROLES, List.of("admin")))));
		Set<SimpleGrantedAuthority> rolesFromAccessToken = this.securityConfiguration.retrieveRolesFromAccessToken(jwt);
		assertThat(rolesFromAccessToken).isNotEmpty();
	}

}
