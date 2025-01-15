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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.viewer.hub.back.config.tenant.TenantIdentifierResolver;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.WeasisSearchCriteria;
import org.viewer.hub.back.model.connector.DbConnectorResult;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.property.AuthenticationProperty;
import org.viewer.hub.back.model.property.BasicAuthenticationProperty;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.property.DbConnectorProperty;
import org.viewer.hub.back.model.property.DbConnectorQueryProperty;
import org.viewer.hub.back.model.property.OAuth2AuthenticationProperty;
import org.viewer.hub.back.model.property.SearchCriteriaProperty;
import org.viewer.hub.back.model.property.WadoConnectorProperty;
import org.viewer.hub.back.service.DbConnectorQueryService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DbConnectorQueryServiceImplTest {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplateMock = Mockito
		.mock(NamedParameterJdbcTemplate.class);

	private final TenantIdentifierResolver currentTenantMock = Mockito.mock(TenantIdentifierResolver.class);

	private DbConnectorQueryService dbConnectorQueryService;

	private ConnectorProperty connectorProperty;

	@BeforeEach
	public void setUp() {
		// Mock
		List<DbConnectorResult> dbConnectorResults = new ArrayList<>();
		DbConnectorResult dbConnectorResult = new DbConnectorResult("patientName", "patientId",
				LocalDate.of(2023, 1, 1), "O", "studyInstanceUid", "studyId", LocalDate.of(2023, 1, 1),
				"accessionNumber", "referringPhysicianName", "studyDescription", "seriesInstanceUid", "modality",
				"seriesDescription", 2, "sopInstanceUid", 1);
		dbConnectorResults.add(dbConnectorResult);

		Mockito
			.when(this.namedParameterJdbcTemplateMock.query(Mockito.anyString(), Mockito.any(SqlParameterSource.class),
					Mockito.any(RowMapper.class)))
			.thenReturn(dbConnectorResults);

		DbConnectorQueryProperty dbConnectorQueryProperty = new DbConnectorQueryProperty("select",
				"accessionNumberColumn", "patientIdColumn", "studyInstanceUidColumn", "serieInstanceUidColumn",
				"sopInstanceUidColumn");
		DbConnectorProperty dbConnectorProperty = new DbConnectorProperty("user", "password", "uri", "driver",
				dbConnectorQueryProperty);
		WadoConnectorProperty wadoConnectorProperty = new WadoConnectorProperty(
				new AuthenticationProperty(false, new OAuth2AuthenticationProperty("url"),
						new BasicAuthenticationProperty("url", "login", "password")),
				"transferSyntaxUid", 0, true, "additionnalParameters", null, new HashMap<>());
		this.connectorProperty = new ConnectorProperty("idDb", ConnectorType.DB,
				new SearchCriteriaProperty(new HashSet<>()), wadoConnectorProperty, dbConnectorProperty, null);

		// Create mocked service
		this.dbConnectorQueryService = new DbConnectorQueryServiceImpl(this.currentTenantMock,
				this.namedParameterJdbcTemplateMock);
	}

	@Test
	void when_buildingFromStudyAccessionNumbers_with_validData_should_callServices() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyAccessionNumbers = new HashSet<>();
		studyAccessionNumbers.add("studyAccessionNumber");

		// Call service
		this.dbConnectorQueryService.buildFromStudyAccessionNumbersDbConnector(manifest, studyAccessionNumbers,
				this.connectorProperty);

		// Test results
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).setCurrentTenant(Mockito.anyString());
		Mockito.verify(this.namedParameterJdbcTemplateMock, Mockito.times(1))
			.query(Mockito.anyString(), Mockito.any(SqlParameterSource.class),
					Mockito.any(BeanPropertyRowMapper.class));
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).clear();
	}

	@Test
	void when_buildingFromPatientIds_with_validData_should_callServices() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> patientIds = new HashSet<>();
		patientIds.add("patientIds");
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();

		// Call service
		this.dbConnectorQueryService.buildFromPatientIdsDbConnector(manifest, patientIds, this.connectorProperty,
				weasisSearchCriteria);

		// Test results
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).setCurrentTenant(Mockito.anyString());
		Mockito.verify(this.namedParameterJdbcTemplateMock, Mockito.times(1))
			.query(Mockito.anyString(), Mockito.any(SqlParameterSource.class),
					Mockito.any(BeanPropertyRowMapper.class));
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).clear();
	}

	@Test
	void when_buildingFromStudyInstanceUids_with_validData_should_callServices() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> studyInstanceUids = new HashSet<>();
		studyInstanceUids.add("studyInstanceUids");

		// Call service
		this.dbConnectorQueryService.buildFromStudyInstanceUidsDbConnector(manifest, studyInstanceUids,
				this.connectorProperty);

		// Test results
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).setCurrentTenant(Mockito.anyString());
		Mockito.verify(this.namedParameterJdbcTemplateMock, Mockito.times(1))
			.query(Mockito.anyString(), Mockito.any(SqlParameterSource.class),
					Mockito.any(BeanPropertyRowMapper.class));
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).clear();
	}

	@Test
	void when_buildingFromSeriesInstanceUids_with_validData_should_callServices() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> seriesInstanceUids = new HashSet<>();
		seriesInstanceUids.add("seriesInstanceUids");

		// Call service
		this.dbConnectorQueryService.buildFromSeriesInstanceUidsDbConnector(manifest, seriesInstanceUids,
				this.connectorProperty);

		// Test results
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).setCurrentTenant(Mockito.anyString());
		Mockito.verify(this.namedParameterJdbcTemplateMock, Mockito.times(1))
			.query(Mockito.anyString(), Mockito.any(SqlParameterSource.class),
					Mockito.any(BeanPropertyRowMapper.class));
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).clear();
	}

	@Test
	void when_buildingFromSopInstanceUids_with_validData_should_callServices() {
		// Init data
		Manifest manifest = new Manifest();
		Set<String> sopInstanceUids = new HashSet<>();
		sopInstanceUids.add("sopInstanceUids");

		// Call service
		this.dbConnectorQueryService.buildFromSopInstanceUidsDbConnector(manifest, sopInstanceUids,
				this.connectorProperty);

		// Test results
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).setCurrentTenant(Mockito.anyString());
		Mockito.verify(this.namedParameterJdbcTemplateMock, Mockito.times(1))
			.query(Mockito.anyString(), Mockito.any(SqlParameterSource.class),
					Mockito.any(BeanPropertyRowMapper.class));
		Mockito.verify(this.currentTenantMock, Mockito.times(1)).clear();
	}

}
