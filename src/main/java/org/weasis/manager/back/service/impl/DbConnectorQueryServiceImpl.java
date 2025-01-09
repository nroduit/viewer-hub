/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.service.impl;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.config.tenant.TenantIdentifierResolver;
import org.weasis.manager.back.constant.DbQueryConstant;
import org.weasis.manager.back.model.SearchCriteria;
import org.weasis.manager.back.model.connector.DbConnectorResult;
import org.weasis.manager.back.model.manifest.DicomPatientSex;
import org.weasis.manager.back.model.manifest.Instance;
import org.weasis.manager.back.model.manifest.Manifest;
import org.weasis.manager.back.model.manifest.Patient;
import org.weasis.manager.back.model.manifest.Serie;
import org.weasis.manager.back.model.manifest.Study;
import org.weasis.manager.back.model.property.ConnectorProperty;
import org.weasis.manager.back.service.DbConnectorQueryService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DbConnectorQueryServiceImpl implements DbConnectorQueryService {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private final TenantIdentifierResolver currentTenant;

	@Autowired
	public DbConnectorQueryServiceImpl(final TenantIdentifierResolver currentTenant,
			final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.currentTenant = currentTenant;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public void buildFromStudyAccessionNumbersDbConnector(Manifest manifest, Set<String> studyAccessionNumbers,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDbConnectorResults(connector, DbQueryConstant.PARAM_ACCESSION_NUMBERS,
				studyAccessionNumbers, connector.getDbConnector().getQuery().getAccessionNumberColumn(),
				DbQueryConstant.IN_ACCESSION_NUMBER);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromPatientIdsDbConnector(Manifest manifest, Set<String> patientIds,
			@Valid ConnectorProperty connector, @Valid SearchCriteria searchCriteria) {
		Set<Patient> patientsFound = this.retrieveDbConnectorResults(connector, DbQueryConstant.PARAM_PATIENT_IDS,
				patientIds, connector.getDbConnector().getQuery().getPatientIdColumn(), DbQueryConstant.IN_PATIENT_ID);

		// Apply patient request filters
		patientsFound = searchCriteria.applyPatientRequestSearchCriteriaFilters(patientsFound);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromStudyInstanceUidsDbConnector(Manifest manifest, Set<String> studyInstanceUids,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDbConnectorResults(connector,
				DbQueryConstant.PARAM_STUDY_INSTANCE_UIDS, studyInstanceUids,
				connector.getDbConnector().getQuery().getStudyInstanceUidColumn(),
				DbQueryConstant.IN_STUDY_INSTANCE_UID);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromSeriesInstanceUidsDbConnector(Manifest manifest, Set<String> seriesInstanceUids,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDbConnectorResults(connector,
				DbQueryConstant.PARAM_SERIE_INSTANCE_UIDS, seriesInstanceUids,
				connector.getDbConnector().getQuery().getSerieInstanceUidColumn(),
				DbQueryConstant.IN_SERIE_INSTANCE_UID);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromSopInstanceUidsDbConnector(Manifest manifest, Set<String> sopInstanceUids,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDbConnectorResults(connector, DbQueryConstant.PARAM_SOP_INSTANCE_UIDS,
				sopInstanceUids, connector.getDbConnector().getQuery().getSopInstanceUidColumn(),
				DbQueryConstant.IN_SOP_INSTANCE_UID);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	/**
	 * Execute DB request depending on search criteria
	 * @param connector Connector to use for the query
	 * @param paramName param name : level of request to look for
	 * @param searchValues Search criteria
	 * @param searchColumnName search column name
	 * @param inParamName param name for in clause
	 * @return List of patients found from request
	 */
	private Set<Patient> retrieveDbConnectorResults(ConnectorProperty connector, String paramName,
			Set<String> searchValues, String searchColumnName, String inParamName) {
		// Route to the connector db
		this.currentTenant.setCurrentTenant(connector.getId());

		// Request connector db with requested parameters
		SqlParameterSource parameters = new MapSqlParameterSource(paramName, searchValues);

		// Execute query
		List<DbConnectorResult> dbConnectorResults = this.namedParameterJdbcTemplate
			.query(connector.getDbConnector().getQuery().getSelect() + DbQueryConstant.AND + searchColumnName
					+ inParamName, parameters, new BeanPropertyRowMapper<>(DbConnectorResult.class));

		// Route to weasis-manager default db
		this.currentTenant.clear();

		// Transform results found to patients
		return this.transformDBConnectorResultsToPatients(dbConnectorResults, connector);
	}

	/**
	 * Transform results found to patients
	 * @param dbConnectorResults Results
	 * @param connector Connector used to execute the query
	 * @return List of patients found from request
	 */
	private Set<Patient> transformDBConnectorResultsToPatients(List<DbConnectorResult> dbConnectorResults,
			ConnectorProperty connector) {
		Set<Patient> patients = new HashSet<>();

		if (dbConnectorResults != null) {
			// Patients
			patients = dbConnectorResults.stream()
				.map(rs -> new Patient(rs.getPatientId(), rs.getPatientName(), rs.getPatientBirthDate(),
						DicomPatientSex.valueOf(rs.getPatientSex())))
				.collect(Collectors.toSet());

			// Studies
			patients.forEach(patient -> patient.setStudies(dbConnectorResults.stream()
				.filter(rs -> Objects.equals(rs.getPatientId(), patient.getPatientID()))
				.map(rsp -> new Study(rsp.getStudyInstanceUid(), rsp.getStudyDescription(), rsp.getStudyDate(),
						rsp.getAccessionNumber(), rsp.getStudyId(), rsp.getReferringPhysicianName()))
				.collect(Collectors.toSet())));

			// Series
			patients.forEach(patient -> patient.getStudies()
				.forEach(study -> study.setSeries(dbConnectorResults.stream()
					.filter(rs -> Objects.equals(rs.getStudyInstanceUid(), study.getStudyInstanceUID()))
					.map(rss -> new Serie(rss.getSeriesInstanceUid(), rss.getSeriesDescription(), rss.getSeriesNumber(),
							rss.getModality(), connector.getWado().getTransferSyntaxUid(),
							connector.getWado().getCompressionRate()))
					.collect(Collectors.toSet()))));

			// Instance
			patients.forEach(patient -> patient.getStudies()
				.forEach(study -> study.getSeries()
					.forEach(serie -> serie.setInstances(dbConnectorResults.stream()
						.filter(rs -> Objects.equals(rs.getSeriesInstanceUid(), serie.getSeriesInstanceUID()))
						.map(rss -> new Instance(rss.getSopInstanceUid(), rss.getInstanceNumber()))
						.collect(Collectors.toSet())))));
		}

		return patients;
	}

}
