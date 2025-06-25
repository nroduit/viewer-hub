package org.viewer.hub.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.viewer.hub.back.enums.ConnectorAuthType;
import org.viewer.hub.back.model.property.ConnectorBasicAuthProperty;
import org.viewer.hub.back.model.property.ConnectorDicomWebProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.DicomWebClientService;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DicomWebClientServiceImpl implements DicomWebClientService {

	public static final String BASE_URL_FORMAT = "%s:%s";

	private final OAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager;

	private final OAuth2AuthorizedClientManager authorizationCodeAuthorizedClientManager;

	private final ClientRegistrationRepository clientRegistrationRepository;

	@Autowired
	public DicomWebClientServiceImpl(final OAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager,
			final OAuth2AuthorizedClientManager authorizationCodeAuthorizedClientManager,
			final ClientRegistrationRepository clientRegistrationRepository) {
		this.clientCredentialsAuthorizedClientManager = clientCredentialsAuthorizedClientManager;
		this.authorizationCodeAuthorizedClientManager = authorizationCodeAuthorizedClientManager;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Override
	public WebClient buildWebClientWadoRs(ConnectorProperty connectorProperty) {
		// Check because Wado-Rs currently not mandatory (has been replaced by Qido-Rs)
		return connectorProperty.getDicomWebConnector().getWadoRs() != null ? this.buildWebClient(
				connectorProperty.getDicomWebConnector().getWadoRs(),
				connectorProperty.getDicomWebConnector().getWadoRs().getAuthentication().getOauth2().getOidcId())
				: null;
	}

	@Override
	public WebClient buildWebClientQidoRs(ConnectorProperty connectorProperty) {
		return this.buildWebClient(connectorProperty.getDicomWebConnector().getQidoRs(),
				connectorProperty.getDicomWebConnector().getQidoRs().getAuthentication().getOauth2().getOidcId());
	}

	/**
	 * Build the webClient depending on auth type (OAUTH2/BASIC). If OAUTH2 check the
	 * authorisation grant type (client credentials or authorisation code)
	 * @param connectorDicomWebProperty active ConnectorDicomWebProperty
	 * @return WebClient built
	 */
	private WebClient buildWebClient(ConnectorDicomWebProperty connectorDicomWebProperty, String connectorId) {
		WebClient webClient;

		// ExchangeStrategies
		ExchangeStrategies strategies = ExchangeStrategies.builder()
			.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize((int) DataSize.ofMegabytes(10).toBytes()))
			.build();

		// Oauth2 webClient
		if (ConnectorAuthType.OAUTH2 == connectorDicomWebProperty.getAuthentication().getType()) {
			// Retrieve the authorisation grant type
			ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(connectorId);
			// .block();
			AuthorizationGrantType authorizationGrantType = clientRegistration != null
					? clientRegistration.getAuthorizationGrantType() : null;

			// Build the path request
			String requestUrl = buildRequestUrl(
					connectorDicomWebProperty.getAuthentication().getOauth2().getServer().getUrl(),
					connectorDicomWebProperty.getAuthentication().getOauth2().getServer().getPort(),
					connectorDicomWebProperty.getAuthentication().getOauth2().getServer().getContext());

			// Build the webClient depending on the authorisation grant type
			webClient = this
				.createOAuth2WebClientBuilder(
						AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authorizationGrantType)
								? clientCredentialsAuthorizedClientManager : authorizationCodeAuthorizedClientManager,
						connectorId)
				.baseUrl(requestUrl)
				.exchangeStrategies(strategies)
				.build();
		}
		else {
			// Build the path request
			String requestUrl = buildRequestUrl(
					connectorDicomWebProperty.getAuthentication().getBasic().getServer().getUrl(),
					connectorDicomWebProperty.getAuthentication().getBasic().getServer().getPort(),
					connectorDicomWebProperty.getAuthentication().getBasic().getServer().getContext());

			// Basic webClient
			webClient = this.createBasicWebClientBuilder(connectorDicomWebProperty.getAuthentication().getBasic())
				.baseUrl(requestUrl)
				.exchangeStrategies(strategies)
				.build();
		}
		return webClient;
	}

	/**
	 * Build the request url to use
	 * @param serverUrl Server url
	 * @param serverPort Server port
	 * @param path Path of the request
	 * @return request url
	 */
	private static String buildRequestUrl(String serverUrl, String serverPort, String path) {
		return "%s%s".formatted(BASE_URL_FORMAT.formatted(serverUrl, serverPort), path);
	}

	/**
	 * Create Basic WebClient builder.
	 * @param connectorBasicAuthProperty ConnectorBasicAuthProperty
	 * @return basic WebClient builder
	 */
	private WebClient.Builder createBasicWebClientBuilder(ConnectorBasicAuthProperty connectorBasicAuthProperty) {
		return WebClient.builder()
			.defaultHeaders(header -> header.setBasicAuth(connectorBasicAuthProperty.getLogin(),
					connectorBasicAuthProperty.getPassword()))
			.filter(this.logRequest())
			.filter(this.logResponse());
	}

	/**
	 * Create OAuth2 WebClient builder.
	 * @param authorizedClientManager Manager
	 * @param clientRegistrationId Client registration id
	 * @return WebClient Builder
	 */
	private WebClient.Builder createOAuth2WebClientBuilder(OAuth2AuthorizedClientManager authorizedClientManager,
			String clientRegistrationId) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
				authorizedClientManager);
		oauth2Client.setDefaultClientRegistrationId(clientRegistrationId);
		return WebClient.builder().filter(oauth2Client).filter(this.logRequest()).filter(this.logResponse());
	}

	/**
	 * Log requests
	 * @return ExchangeFilterFunction
	 */
	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			StringBuilder builder = new StringBuilder("Request: \n");
			clientRequest.headers()
				.forEach((name, values) -> values
					.forEach(value -> builder.append(name).append(":").append(value).append("\n")));
			LOG.debug(builder.toString());
			return Mono.just(clientRequest);
		});
	}

	/**
	 * Log responses
	 * @return ExchangeFilterFunction
	 */
	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			StringBuilder builder = new StringBuilder("Response: \n");
			clientResponse.headers()
				.asHttpHeaders()
				.forEach((name, values) -> values
					.forEach(value -> builder.append(name).append(":").append(value).append("\n")));
			LOG.debug(builder.toString());
			return Mono.just(clientResponse);
		});
	}

}
