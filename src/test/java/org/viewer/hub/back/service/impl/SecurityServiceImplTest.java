/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.viewer.hub.back.enums.ConnectorAuthType;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.manifest.ArcQuery;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.ConnectorAuthenticationProperty;
import org.viewer.hub.back.model.property.ConnectorBasicAuthProperty;
import org.viewer.hub.back.model.property.ConnectorDicomWebProperty;
import org.viewer.hub.back.model.property.ConnectorOauth2AuthProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.property.ConnectorServerProperty;
import org.viewer.hub.back.model.property.DicomWebConnectorProperty;
import org.viewer.hub.back.service.ConnectorService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityServiceImplTest {

	private static final String ARC_ID = "arc";

	private static final String OIDC_ID = "oidc";

	@Mock
	private ConnectorService connectorService;

	@Mock
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@Mock
	private OAuth2AuthorizedClientManager clientCredentialsAuthorizedClientManager;

	@Mock
	private ClientRegistrationRepository clientRegistrationRepository;

	private SecurityServiceImpl securityService;

	@BeforeEach
	void setUp() {
		securityService = new SecurityServiceImpl(connectorService, oAuth2AuthorizedClientService,
				clientCredentialsAuthorizedClientManager, clientRegistrationRepository);
	}

	@Test
	void when_handleManifestAuthentication_with_basicType_and_noOauth2_should_useBasicUrlAndCredentials() {
		Manifest manifest = manifestWith(authentication(ConnectorAuthType.BASIC, null, basic()));

		assertDoesNotThrow(() -> securityService.handleManifestAuthentication(manifest, null));

		ArcQuery arcQuery = manifest.getArcQueries().get(0);
		assertEquals("https://basic.test:8443/wado", arcQuery.getBaseUrl());
		assertEquals("bG9naW46cGFzc3dvcmQ=", arcQuery.getWebLogin());
	}

	@Test
	void when_handleManifestAuthentication_with_basicType_and_oauth2WithoutServer_should_useBasicUrl() {
		Manifest manifest = manifestWith(
				authentication(ConnectorAuthType.BASIC, new ConnectorOauth2AuthProperty(null, null), basic()));

		assertDoesNotThrow(() -> securityService.handleManifestAuthentication(manifest, null));

		assertEquals("https://basic.test:8443/wado", manifest.getArcQueries().get(0).getBaseUrl());
	}

	@Test
	void when_handleManifestAuthentication_with_basicType_and_noServerPortContext_should_useUrlOnly() {
		ConnectorBasicAuthProperty basic = ConnectorBasicAuthProperty.builder()
			.login("login")
			.password("password")
			.server(ConnectorServerProperty.builder().url("https://basic.test").build())
			.build();
		Manifest manifest = manifestWith(authentication(ConnectorAuthType.BASIC, null, basic));

		securityService.handleManifestAuthentication(manifest, null);

		assertEquals("https://basic.test", manifest.getArcQueries().get(0).getBaseUrl());
	}

	@Test
	void when_handleManifestAuthentication_with_oauth2ClientCredentials_and_noBasic_should_useOauth2UrlAndToken() {
		mockRegistration(AuthorizationGrantType.CLIENT_CREDENTIALS);
		OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
		when(client.getAccessToken()).thenReturn(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token",
				Instant.now(), Instant.now().plusSeconds(60)));
		when(clientCredentialsAuthorizedClientManager.authorize(any())).thenReturn(client);
		Manifest manifest = manifestWith(authentication(ConnectorAuthType.OAUTH2, oauth2(), null));

		assertDoesNotThrow(() -> securityService.handleManifestAuthentication(manifest, null));

		ArcQuery arcQuery = manifest.getArcQueries().get(0);
		assertEquals("https://oauth2.test:9443/wado", arcQuery.getBaseUrl());
		assertNull(arcQuery.getWebLogin());
		assertEquals(1, arcQuery.getHttpTags().size());
		assertEquals("Bearer token", arcQuery.getHttpTags().iterator().next().getValue());
	}

	@Test
	void when_handleManifestAuthentication_with_oauth2AuthorizationCode_and_notAuthenticated_should_useBasic() {
		mockRegistration(AuthorizationGrantType.AUTHORIZATION_CODE);
		Manifest manifest = manifestWith(authentication(ConnectorAuthType.OAUTH2, oauth2(), basic()));

		assertDoesNotThrow(() -> securityService.handleManifestAuthentication(manifest, null));

		ArcQuery arcQuery = manifest.getArcQueries().get(0);
		assertEquals("https://basic.test:8443/wado", arcQuery.getBaseUrl());
		assertEquals("bG9naW46cGFzc3dvcmQ=", arcQuery.getWebLogin());
		assertEquals(0, arcQuery.getHttpTags().size());
	}

	@Test
	void when_handleManifestAuthentication_with_oauth2AuthorizationCode_and_authenticated_should_useOauth2Token() {
		mockRegistration(AuthorizationGrantType.AUTHORIZATION_CODE);
		Authentication userAuthentication = mock(Authentication.class);
		when(userAuthentication.getName()).thenReturn("user");
		OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
		when(client.getAccessToken()).thenReturn(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token",
				Instant.now(), Instant.now().plusSeconds(60)));
		when(oAuth2AuthorizedClientService.loadAuthorizedClient(OIDC_ID, "user")).thenReturn(client);
		Manifest manifest = manifestWith(authentication(ConnectorAuthType.OAUTH2, oauth2(), basic()));

		assertDoesNotThrow(() -> securityService.handleManifestAuthentication(manifest, userAuthentication));

		ArcQuery arcQuery = manifest.getArcQueries().get(0);
		assertEquals("https://oauth2.test:9443/wado", arcQuery.getBaseUrl());
		assertNull(arcQuery.getWebLogin());
		assertEquals("Bearer token", arcQuery.getHttpTags().iterator().next().getValue());
	}

	@Test
	void when_handleManifestAuthentication_with_oauth2AuthorizationCode_and_authenticated_and_noBasicServer_should_notFail() {
		mockRegistration(AuthorizationGrantType.AUTHORIZATION_CODE);
		Authentication userAuthentication = mock(Authentication.class);
		when(userAuthentication.getName()).thenReturn("user");
		OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
		when(client.getAccessToken()).thenReturn(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token",
				Instant.now(), Instant.now().plusSeconds(60)));
		when(oAuth2AuthorizedClientService.loadAuthorizedClient(OIDC_ID, "user")).thenReturn(client);
		ConnectorBasicAuthProperty basicWithoutServer = ConnectorBasicAuthProperty.builder()
			.login("login")
			.password("password")
			.build();
		Manifest manifest = manifestWith(authentication(ConnectorAuthType.OAUTH2, oauth2(), basicWithoutServer));

		assertDoesNotThrow(() -> securityService.handleManifestAuthentication(manifest, userAuthentication));

		assertEquals("https://oauth2.test:9443/wado", manifest.getArcQueries().get(0).getBaseUrl());
	}

	private void mockRegistration(AuthorizationGrantType grantType) {
		ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(OIDC_ID)
			.clientId("id")
			.authorizationGrantType(grantType)
			.tokenUri("https://idp.test/token");
		if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(grantType)) {
			builder.authorizationUri("https://idp.test/auth").redirectUri("https://app.test/cb");
		}
		when(clientRegistrationRepository.findByRegistrationId(OIDC_ID)).thenReturn(builder.build());
	}

	private Manifest manifestWith(ConnectorAuthenticationProperty authentication) {
		ConnectorProperty connector = ConnectorProperty.builder()
			.id(ARC_ID)
			.type(ConnectorType.DICOM_WEB)
			.dicomWebConnector(DicomWebConnectorProperty.builder()
				.wadoRs(ConnectorDicomWebProperty.builder().authentication(authentication).build())
				.build())
			.build();
		when(connectorService.retrieveConnectorFromId(ARC_ID)).thenReturn(connector);

		ArcQuery arcQuery = ArcQuery.builder().arcId(ARC_ID).httpTags(new HashSet<>()).baseUrl("").build();
		List<ArcQuery> arcQueries = new ArrayList<>();
		arcQueries.add(arcQuery);
		return Manifest.builder().arcQueries(arcQueries).build();
	}

	private ConnectorAuthenticationProperty authentication(ConnectorAuthType type, ConnectorOauth2AuthProperty oauth2,
			ConnectorBasicAuthProperty basic) {
		return ConnectorAuthenticationProperty.builder().type(type).oauth2(oauth2).basic(basic).build();
	}

	private ConnectorBasicAuthProperty basic() {
		return ConnectorBasicAuthProperty.builder()
			.login("login")
			.password("password")
			.server(ConnectorServerProperty.builder().url("https://basic.test").port("8443").context("/wado").build())
			.build();
	}

	private ConnectorOauth2AuthProperty oauth2() {
		return new ConnectorOauth2AuthProperty(OIDC_ID,
				ConnectorServerProperty.builder().url("https://oauth2.test").port("9443").context("/wado").build());
	}

}
