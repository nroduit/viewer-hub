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

package org.viewer.hub.back.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.ConnectorAuthType;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.manifest.ArcQuery;
import org.viewer.hub.back.model.manifest.HttpTag;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.SecurityService;

import java.util.Base64;
import java.util.Objects;

@Service
public class SecurityServiceImpl implements SecurityService {

	// Services
	private final ConnectorQueryService connectorQueryService;

	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	private final OAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager;

	private final ClientRegistrationRepository clientRegistrationRepository;

	@Autowired
	public SecurityServiceImpl(ConnectorQueryService connectorQueryService,
			OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
			final OAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager,
			final ClientRegistrationRepository clientRegistrationRepository) {
		this.connectorQueryService = connectorQueryService;
		this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
		this.clientCredentialsAuthorizedClientManager = clientCredentialsAuthorizedClientManager;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Override
	public void handleManifestAuthentication(Manifest manifest, Authentication authentication) {
		// Loop on each arc ids
		manifest.getArcQueries().forEach(arcQuery -> {
			// Retrieve the connector corresponding to the arc id
			ConnectorProperty connectorProperty = this.connectorQueryService
				.retrieveConnectorFromId(arcQuery.getArcId());

			// Depending on the type of the connector and the type of authentication
			// set the token or basic credentials in the manifest
			if (Objects.equals(ConnectorType.DB, connectorProperty.getType())) {
				handleDbManifestAuthentication(authentication, arcQuery, connectorProperty);
			}
			else if (Objects.equals(ConnectorType.DICOM, connectorProperty.getType())) {
				handleDicomManifestAuthentication(authentication, arcQuery, connectorProperty);
			}
			else if (Objects.equals(ConnectorType.DICOM_WEB, connectorProperty.getType())) {
				handleDicomWebManifestAuthentication(authentication, arcQuery, connectorProperty);
			}
		});
	}

	/**
	 * Handle manifest authentication for dicom web
	 * @param authentication Authentication
	 * @param arcQuery ArcQuery to fill
	 * @param connectorProperty Connector property to evaluate
	 */
	private void handleDicomWebManifestAuthentication(Authentication authentication, ArcQuery arcQuery,
			ConnectorProperty connectorProperty) {
		String basicPort = retrieveProperty(connectorProperty.getDicomWebConnector()
			.getWadoRs()
			.getAuthentication()
			.getBasic()
			.getServer()
			.getPort());
		basicPort = StringUtils.isNotBlank(basicPort) ? ":%s".formatted(basicPort) : "";

		String basicContext = retrieveProperty(connectorProperty.getDicomWebConnector()
			.getWadoRs()
			.getAuthentication()
			.getBasic()
			.getServer()
			.getContext());

		String oAuth2Port = retrieveProperty(connectorProperty.getDicomWebConnector()
			.getWadoRs()
			.getAuthentication()
			.getOauth2()
			.getServer()
			.getPort());
		oAuth2Port = StringUtils.isNotBlank(oAuth2Port) ? ":%s".formatted(oAuth2Port) : "";

		String oAuth2Context = retrieveProperty(connectorProperty.getDicomWebConnector()
			.getWadoRs()
			.getAuthentication()
			.getOauth2()
			.getServer()
			.getContext());

		handleArcQueryManifestAuthentication(arcQuery, connectorProperty, authentication,
				connectorProperty.getDicomWebConnector().getWadoRs().getAuthentication().getType(),
				String.format("%s%s%s",
						connectorProperty.getDicomWebConnector()
							.getWadoRs()
							.getAuthentication()
							.getBasic()
							.getServer()
							.getUrl(),
						basicPort, basicContext),
				connectorProperty.getDicomWebConnector().getWadoRs().getAuthentication().getBasic().getLogin(),
				connectorProperty.getDicomWebConnector().getWadoRs().getAuthentication().getBasic().getPassword(),
				String.format("%s%s%s",
						connectorProperty.getDicomWebConnector()
							.getWadoRs()
							.getAuthentication()
							.getOauth2()
							.getServer()
							.getUrl(),
						oAuth2Port, oAuth2Context),
				connectorProperty.getDicomWebConnector().getWadoRs().getAuthentication().getOauth2().getOidcId());
	}

	/**
	 * Handle manifest authentication for dicom
	 * @param authentication Authentication
	 * @param arcQuery ArcQuery to fill
	 * @param connectorProperty Connector property to evaluate
	 */
	private void handleDicomManifestAuthentication(Authentication authentication, ArcQuery arcQuery,
			ConnectorProperty connectorProperty) {
		String basicPort = retrieveProperty(
				connectorProperty.getDicomConnector().getWado().getAuthentication().getBasic().getServer().getPort());
		basicPort = StringUtils.isNotBlank(basicPort) ? ":%s".formatted(basicPort) : "";

		String basicContext = retrieveProperty(connectorProperty.getDicomConnector()
			.getWado()
			.getAuthentication()
			.getBasic()
			.getServer()
			.getContext());

		String oAuth2Port = retrieveProperty(
				connectorProperty.getDicomConnector().getWado().getAuthentication().getOauth2().getServer().getPort());
		oAuth2Port = StringUtils.isNotBlank(oAuth2Port) ? ":%s".formatted(oAuth2Port) : "";

		String oAuth2Context = retrieveProperty(connectorProperty.getDicomConnector()
			.getWado()
			.getAuthentication()
			.getOauth2()
			.getServer()
			.getContext());

		handleArcQueryManifestAuthentication(arcQuery, connectorProperty, authentication,
				connectorProperty.getDicomConnector().getWado().getAuthentication().getType(),
				String.format("%s%s%s",
						connectorProperty.getDicomConnector()
							.getWado()
							.getAuthentication()
							.getBasic()
							.getServer()
							.getUrl(),
						basicPort, basicContext),
				connectorProperty.getDicomConnector().getWado().getAuthentication().getBasic().getLogin(),
				connectorProperty.getDicomConnector().getWado().getAuthentication().getBasic().getPassword(),
				String.format("%s%s%s",
						connectorProperty.getDicomConnector()
							.getWado()
							.getAuthentication()
							.getOauth2()
							.getServer()
							.getUrl(),
						oAuth2Port, oAuth2Context),
				connectorProperty.getDicomConnector().getWado().getAuthentication().getOauth2().getOidcId());
	}

	/**
	 * Handle manifest authentication for db
	 * @param authentication Authentication
	 * @param arcQuery ArcQuery to fill
	 * @param connectorProperty Connector property to evaluate
	 */
	private void handleDbManifestAuthentication(Authentication authentication, ArcQuery arcQuery,
			ConnectorProperty connectorProperty) {
		String basicPort = retrieveProperty(
				connectorProperty.getDbConnector().getWado().getAuthentication().getBasic().getServer().getPort());
		basicPort = StringUtils.isNotBlank(basicPort) ? ":%s".formatted(basicPort) : "";

		String basicContext = retrieveProperty(
				connectorProperty.getDbConnector().getWado().getAuthentication().getBasic().getServer().getContext());

		String oAuth2Port = retrieveProperty(
				connectorProperty.getDbConnector().getWado().getAuthentication().getOauth2().getServer().getPort());
		oAuth2Port = StringUtils.isNotBlank(oAuth2Port) ? ":%s".formatted(oAuth2Port) : "";

		String oAuth2Context = retrieveProperty(
				connectorProperty.getDbConnector().getWado().getAuthentication().getOauth2().getServer().getContext());

		handleArcQueryManifestAuthentication(arcQuery, connectorProperty, authentication,
				connectorProperty.getDbConnector().getWado().getAuthentication().getType(),
				String.format("%s%s%s",
						connectorProperty.getDbConnector()
							.getWado()
							.getAuthentication()
							.getBasic()
							.getServer()
							.getUrl(),
						basicPort, basicContext),
				connectorProperty.getDbConnector().getWado().getAuthentication().getBasic().getLogin(),
				connectorProperty.getDbConnector().getWado().getAuthentication().getBasic().getPassword(),
				String.format("%s%s%s",
						connectorProperty.getDbConnector()
							.getWado()
							.getAuthentication()
							.getOauth2()
							.getServer()
							.getUrl(),
						oAuth2Port, oAuth2Context),
				connectorProperty.getDbConnector().getWado().getAuthentication().getOauth2().getOidcId());
	}

	/**
	 * Manage manifest authentication in order for Weasis to retrieve the images. <br/>
	 * Rules: <br/>
	 * - if property is basic, use the basic authentication parameters <br/>
	 * - if request is authenticated in authorization_code flow or client_credentials
	 * configuration is set: set the oauth2 access token of the request in httpTags and
	 * use oauth2 url property <br/>
	 * - if request is not authenticated and authorization_code flow should be used: use
	 * the basic authentication parameters
	 * @param arcQuery arcQuery to fill
	 * @param connector Connector properties
	 */
	private void handleArcQueryManifestAuthentication(ArcQuery arcQuery, ConnectorProperty connector,
			Authentication authentication, ConnectorAuthType connectorAuthType, String basicUrl, String basicWadoLogin,
			String basicWadoPassword, String oauth2Url, String oAuth2OidcId) {
		AuthorizationGrantType authorizationGrantType = StringUtils.isNotBlank(oAuth2OidcId)
				&& clientRegistrationRepository.findByRegistrationId(oAuth2OidcId) != null
						? clientRegistrationRepository.findByRegistrationId(oAuth2OidcId).getAuthorizationGrantType()
						: null;

		boolean shouldUseBasic = Objects.equals(ConnectorAuthType.BASIC, connectorAuthType)
				// Case the client didn't make an authenticated request on
				// authorization_code flow: default use basic
				|| (Objects.equals(ConnectorAuthType.OAUTH2, connectorAuthType)
						&& Objects.equals(authorizationGrantType, AuthorizationGrantType.AUTHORIZATION_CODE)
						&& authentication == null);

		// Basic authentication: set basic authentication url + encode in base64
		// "login:password" and set it in weblogin field
		if (shouldUseBasic) {
			arcQuery.setBaseUrl(basicUrl);
			// Web login
			if (StringUtils.isNotBlank(basicWadoLogin) && StringUtils.isNotBlank(basicWadoPassword)) {
				// Encode in base64 login:password
				arcQuery.setWebLogin(Base64.getEncoder()
					.encodeToString("%s:%s".formatted(basicWadoLogin.trim(), basicWadoPassword.trim()).getBytes()));
			}
		}
		// OAuth2 authentication: use oAuth2 access token and set it in the httpTag field
		else {
			arcQuery.setBaseUrl(oauth2Url);
			arcQuery.getHttpTags()
				.add(new HttpTag(HttpHeaders.AUTHORIZATION, "%s %s"
					.formatted(OAuth2AccessToken.TokenType.BEARER.getValue(),
							authorizationGrantType != null
									&& Objects.equals(authorizationGrantType, AuthorizationGrantType.CLIENT_CREDENTIALS)
											?
											// Client credentials
											retrieveAccessTokenClientCredentials(oAuth2OidcId)
											// Authorisation code
											: retrieveAccessTokenAuthorizationCode(oAuth2OidcId, authentication))));
		}
	}

	/**
	 * If authenticated: retrieve the access token to use it when building the manifest
	 * @param authentication Authentication
	 * @return access token found
	 */
	private String retrieveAccessTokenAuthorizationCode(String clientRegistrationId, Authentication authentication) {
		String accessTokenFound = null;
		if (authentication != null) {
			OAuth2AuthorizedClient authorizedClient = this.oAuth2AuthorizedClientService
				.loadAuthorizedClient(clientRegistrationId, authentication.getName());
			accessTokenFound = authorizedClient != null && authorizedClient.getAccessToken() != null
					? authorizedClient.getAccessToken().getTokenValue() : null;
		}
		return accessTokenFound;

	}

	/**
	 * Retrieve the access token
	 * @param clientRegistrationId Client Registration Id
	 * @return access token found
	 */
	private String retrieveAccessTokenClientCredentials(String clientRegistrationId) {
		String accessTokenFound = null;
		if (clientRegistrationId != null) {
			OAuth2AuthorizedClient authorizedClient = clientCredentialsAuthorizedClientManager.authorize(
					OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId).principal("weasis").build());
			accessTokenFound = authorizedClient != null && authorizedClient.getAccessToken() != null
					? authorizedClient.getAccessToken().getTokenValue() : null;
		}
		return accessTokenFound;
	}

	private String retrieveProperty(String property) {
		return StringUtils.isNotBlank(property) ? property : "";
	}

}
