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
