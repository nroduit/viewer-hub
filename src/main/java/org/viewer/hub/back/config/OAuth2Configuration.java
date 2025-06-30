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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class OAuth2Configuration {

	/**
	 * Register an OAuth2AuthorizedClientManager and associate it with an
	 * OAuth2AuthorizedClientProvider that provides support for client_credentials
	 * authorization grant type
	 * @param client Client repository
	 * @return OAuth2AuthorizedClientManager Client manager
	 */
	@Bean
	public OAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager(ClientRegistrationRepository client) {
		OAuth2AuthorizedClientService service = new InMemoryOAuth2AuthorizedClientService(client);
		AuthorizedClientServiceOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
				client, service);
		OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
			.clientCredentials()
			.build();
		manager.setAuthorizedClientProvider(authorizedClientProvider);
		return manager;
	}

	/**
	 * Register an OAuth2AuthorizedClientManager and associate it with an
	 * OAuth2AuthorizedClientProvider that provides support for authorization code
	 * authorization grant type
	 * @param client Client repository
	 * @return OAuth2AuthorizedClientManager Client manager
	 */
	@Bean
	public OAuth2AuthorizedClientManager authorizationCodeAuthorizedClientManager(ClientRegistrationRepository client) {
		OAuth2AuthorizedClientService service = new InMemoryOAuth2AuthorizedClientService(client);
		AuthorizedClientServiceOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
				client, service);
		OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
			.authorizationCode()
			.build();
		manager.setAuthorizedClientProvider(authorizedClientProvider);
		return manager;
	}

}
