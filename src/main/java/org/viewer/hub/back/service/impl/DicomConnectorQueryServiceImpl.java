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

import jakarta.validation.Valid;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import org.viewer.hub.back.enums.SearchCriteriaType;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.manifest.DicomPatientSex;
import org.viewer.hub.back.model.manifest.Instance;
import org.viewer.hub.back.model.manifest.Manifest;
import org.viewer.hub.back.model.manifest.Patient;
import org.viewer.hub.back.model.manifest.Serie;
import org.viewer.hub.back.model.manifest.Study;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.DicomConnectorQueryService;
import org.viewer.hub.back.util.ConnectorUtil;
import org.viewer.hub.back.util.DateTimeUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DicomConnectorQueryServiceImpl implements DicomConnectorQueryService {

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public DicomConnectorQueryServiceImpl() {
		// Autowired constructor.
	}

	@Override
	public void buildFromStudyAccessionNumbersDicomConnector(Manifest manifest, Set<String> studyAccessionNumbers,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, studyAccessionNumbers,
				SearchCriteriaType.ACCESSION_NUMBER);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromPatientIdsDicomConnector(Manifest manifest, Set<String> patientIds,
			@Valid ConnectorProperty connector, @Valid SearchCriteria searchCriteria) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, patientIds,
				SearchCriteriaType.PATIENT_ID);

		// Apply patient request filters
		patientsFound = searchCriteria.applyPatientRequestSearchCriteriaFilters(patientsFound);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromStudyInstanceUidsDicomConnector(Manifest manifest, Set<String> studyInstanceUids,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, studyInstanceUids,
				SearchCriteriaType.STUDY_INSTANCE_UID);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromSeriesInstanceUidsDicomConnector(Manifest manifest, Set<String> seriesInstanceUids,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, seriesInstanceUids,
				SearchCriteriaType.SERIE_INSTANCE_UID);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	@Override
	public void buildFromSopInstanceUidsDicomConnector(Manifest manifest, Set<String> sopInstanceUids,
			@Valid ConnectorProperty connector) {
		Set<Patient> patientsFound = this.retrieveDicomConnectorResults(connector, sopInstanceUids,
				SearchCriteriaType.SOP_INSTANCE_UID);

		// Update manifest with patients found
		manifest.update(patientsFound, connector);
	}

	/**
	 * Retrieve the list of patients found from dicom requests
	 * @param connector Connector
	 * @param searchValues Search criteria
	 * @param searchCriteriaType Level of search
	 * @return List of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResults(ConnectorProperty connector, Set<String> searchValues,
			SearchCriteriaType searchCriteriaType) {
		Set<Patient> patientsFound = new HashSet<>();
		if (Objects.equals(SearchCriteriaType.ACCESSION_NUMBER, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromAccessionNumbers(connector, searchValues);
		}
		else if (Objects.equals(SearchCriteriaType.PATIENT_ID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromPatientIds(connector, searchValues);
		}
		else if (Objects.equals(SearchCriteriaType.STUDY_INSTANCE_UID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromStudyInstanceUids(connector, searchValues);
		}
		else if (Objects.equals(SearchCriteriaType.SERIE_INSTANCE_UID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromSerieInstanceUids(connector, searchValues);
		}
		else if (Objects.equals(SearchCriteriaType.SOP_INSTANCE_UID, searchCriteriaType)) {
			patientsFound = this.retrieveDicomConnectorResultsFromSopInstanceUids(connector, searchValues);
		}
		return patientsFound;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with accession number
	 * criteria
	 * @param connector Connector
	 * @param accessionNumbers Accession numbers to look for
	 * @return List of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromAccessionNumbers(ConnectorProperty connector,
			Set<String> accessionNumbers) {
		Set<Patient> patients = new HashSet<>();
		accessionNumbers.stream()
			// Retrieve studies and patient
			.map(accessionNumber -> this.retrieveDicomPatientWithStudiesFromAccessionNumber(accessionNumber, connector))
			.filter(Objects::nonNull)
			// Retrieve series and sop instances
			.forEach(patient -> this.retrieveDicomSeriesSopInstancesAndUpdatePatients(connector, patients, patient));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with patient ids criteria
	 * @param connector Connector
	 * @param patientIds Patient ids to look for
	 * @return List of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromPatientIds(ConnectorProperty connector,
			Set<String> patientIds) {
		Set<Patient> patients = new HashSet<>();
		patientIds.stream()
			// Retrieve studies and patient
			.map(patientId -> this.retrieveDicomPatientStudiesFromPatientId(patientId, connector))
			.filter(Objects::nonNull)
			// Retrieve series and sop instances
			.forEach(patient -> this.retrieveDicomSeriesSopInstancesAndUpdatePatients(connector, patients, patient));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with study instance uids
	 * criteria
	 * @param connector Connector
	 * @param studyInstanceUids Study instance uids to look for
	 * @return List of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromStudyInstanceUids(ConnectorProperty connector,
			Set<String> studyInstanceUids) {
		Set<Patient> patients = new HashSet<>();
		studyInstanceUids.stream()
			// Retrieve studies and patient
			.map(studyInstanceUid -> this.retrieveDicomPatientStudiesFromStudyInstanceUid(studyInstanceUid, connector))
			.filter(Objects::nonNull)
			// Retrieve series and sop instances
			.forEach(patient -> this.retrieveDicomSeriesSopInstancesAndUpdatePatients(connector, patients, patient));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with serie instance uids
	 * criteria
	 * @param connector Connector
	 * @param serieInstanceUids Serie instance uids to look for
	 * @return List of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromSerieInstanceUids(ConnectorProperty connector,
			Set<String> serieInstanceUids) {
		Set<Patient> patients = new HashSet<>();
		serieInstanceUids.stream()
			// Retrieve serie, study and patient
			.map(serieInstanceUid -> this.retrieveDicomPatientStudySerieFromSerieInstanceUid(serieInstanceUid,
					connector))
			.filter(Objects::nonNull)
			// Retrieve sop instances
			.forEach(patient -> this.retrieveDicomSopInstancesAndUpdatePatients(connector, patients, patient));
		return patients;
	}

	/**
	 * Retrieve the list of patients found from dicom requests with sop instance uids
	 * criteria
	 * @param connector Connector
	 * @param sopInstanceUids Sop instance uids to look for
	 * @return List of patients found
	 */
	private Set<Patient> retrieveDicomConnectorResultsFromSopInstanceUids(ConnectorProperty connector,
			Set<String> sopInstanceUids) {
		Set<Patient> patients = new HashSet<>();
		sopInstanceUids.stream()
			// Retrieve serie, study and patient
			.map(sopInstanceUid -> this.retrieveDicomPatientStudySerieSopInstanceUidFromSopInstanceUid(sopInstanceUid,
					connector))
			.filter(Objects::nonNull)
			// Merge in existing patient or add in list of patients
			.forEach(patient -> mergeOrAddInPatients(patients, patient));
		return patients;
	}

	/**
	 * Retrieve Sop Instances From Study/Serie Instance Uids and update patients
	 * @param connector Connector
	 * @param patients Patients to update
	 * @param patient Patient result to add/merge
	 */
	private void retrieveDicomSopInstancesAndUpdatePatients(ConnectorProperty connector, Set<Patient> patients,
			Patient patient) {
		// Retrieve sop instances
		this.retrieveDicomSopInstancesFromStudySerieInstanceUids(patient, connector);

		// Merge in existing patient or add in list of patients
		mergeOrAddInPatients(patients, patient);
	}

	/**
	 * Retrieve Series and Sop Instances and update patients
	 * @param connector Connector
	 * @param patients Patients to update
	 * @param patient Patient result to add/merge
	 */
	private void retrieveDicomSeriesSopInstancesAndUpdatePatients(ConnectorProperty connector, Set<Patient> patients,
			Patient patient) {
		// Retrieve series
		this.retrieveDicomSeriesFromStudyInstanceUid(patient, connector);

		// Retrieve sop instances
		this.retrieveDicomSopInstancesFromStudySerieInstanceUids(patient, connector);

		// Merge in existing patient or add in list of patients
		mergeOrAddInPatients(patients, patient);
	}

	/**
	 * Retrieve Patient/Study/Serie From Serie Instance Uids and create patient
	 * @param serieInstanceUid Serie instance uid
	 * @param connector Connector
	 */
	private Patient retrieveDicomPatientStudySerieFromSerieInstanceUid(String serieInstanceUid,
			ConnectorProperty connector) {
		// Define query to retrieve patient,studies, serie from serie instance uid and
		// process dicom query
		List<Attributes> patientStudySerieAttributes = this.retrieveDicomQueryResults(connector,
				QueryRetrieveLevel.SERIES,
				this.definePatientStudySerieDicomParamsFromSerieInstanceUid(serieInstanceUid), true);

		return this.createPatientFromPatientStudySerieAttributes(patientStudySerieAttributes, connector);
	}

	/**
	 * Retrieve patient with studies from accession number and create patient
	 * @param accessionNumber Accession number
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientWithStudiesFromAccessionNumber(String accessionNumber,
			ConnectorProperty connector) {
		// Define query to retrieve patient,studies from accession number and process
		// dicom query
		List<Attributes> patientStudiesAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.STUDY,
				this.definePatientStudiesDicomParamsFromAccessionNumber(accessionNumber), false);

		return createPatientFromPatientStudiesAttributes(patientStudiesAttributes);
	}

	/**
	 * Retrieve patient with studies from patient id and create patient
	 * @param patientId Patient id
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientStudiesFromPatientId(String patientId, ConnectorProperty connector) {
		List<Attributes> patientStudiesAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.STUDY,
				this.definePatientStudiesDicomParamsFromPatientId(patientId), false);

		return createPatientFromPatientStudiesAttributes(patientStudiesAttributes);
	}

	/**
	 * Retrieve patient with studies from study instance uid and create patient
	 * @param studyInstanceUid Study instance uid
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientStudiesFromStudyInstanceUid(String studyInstanceUid,
			ConnectorProperty connector) {
		// Define query to retrieve patient,studies from study instance uid and process
		// dicom query
		List<Attributes> patientStudiesAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.STUDY,
				this.definePatientStudiesDicomParamsFromStudyInstanceUid(studyInstanceUid), false);

		return createPatientFromPatientStudiesAttributes(patientStudiesAttributes);
	}

	/**
	 * Retrieve patient,studies, serie, instance from sop instance uid from ssop instance
	 * uid and create patient
	 * @param sopInstanceUid Sop instance uid
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient retrieveDicomPatientStudySerieSopInstanceUidFromSopInstanceUid(String sopInstanceUid,
			ConnectorProperty connector) {
		// Define query to retrieve patient,studies, serie, instance from sop instance uid
		// and process dicom query
		List<Attributes> patientStudySerieSopInstanceAttributes = this.retrieveDicomQueryResults(connector,
				QueryRetrieveLevel.IMAGE,
				this.definePatientStudySerieSopInstanceDicomParamsFromSopInstanceUid(sopInstanceUid), true);

		return this.createPatientFromPatientStudySerieSopInstanceAttributes(patientStudySerieSopInstanceAttributes,
				connector);
	}

	/**
	 * Merge patient found in the list of existing patient list
	 * @param patients Patients to update
	 * @param patient Patient result to add/merge
	 */
	private static void mergeOrAddInPatients(Set<Patient> patients, Patient patient) {
		// Merge in existing patient or add in list of patients
		Optional<Patient> optionalPatient = patients.stream()
			.filter(p -> Objects.equals(p.getPatientID(), patient.getPatientID()))
			.findFirst();
		if (optionalPatient.isPresent()) {
			optionalPatient.get().merge(patient);
		}
		else {
			patients.add(patient);
		}
	}

	/**
	 * Create patient from Patient/Study Attributes
	 * @param patientStudiesAttributes Attributes to use
	 * @return Patient created
	 */
	private static Patient createPatientFromPatientStudiesAttributes(List<Attributes> patientStudiesAttributes) {
		Patient patient = null;

		// Retrieve studies
		Set<Study> studies = patientStudiesAttributes.stream()
			.map(studyFound -> new Study(studyFound.getString(Tag.StudyInstanceUID),
					studyFound.getString(Tag.StudyDescription),
					DateTimeUtil.toLocalDate(studyFound.getDate(Tag.StudyDate)),
					DateTimeUtil.toLocalTime(studyFound.getDate(Tag.StudyTime)),
					studyFound.getString(Tag.AccessionNumber), studyFound.getString(Tag.StudyID),
					studyFound.getString(Tag.ReferringPhysicianName)))
			.collect(Collectors.toSet());

		Optional<Attributes> optionalPatient = patientStudiesAttributes.stream().findFirst();
		if (optionalPatient.isPresent()) {
			Attributes attributesPatient = optionalPatient.get();
			// Build patient
			patient = buildPatientFromAttributes(attributesPatient);
			// Set studies found
			patient.setStudies(studies);
		}
		return patient;
	}

	/**
	 * Create patient from Patient/Study/Serie/Sop instance Attributes
	 * @param patientStudySerieSopInstanceAttributes Attributes to use
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient createPatientFromPatientStudySerieSopInstanceAttributes(
			List<Attributes> patientStudySerieSopInstanceAttributes, ConnectorProperty connector) {
		Patient patient = null;
		// Retrieve attribute
		Optional<Attributes> optionalPatientStudySerieSopInstanceAttributes = patientStudySerieSopInstanceAttributes
			.stream()
			.findFirst();

		if (optionalPatientStudySerieSopInstanceAttributes.isPresent()) {
			Attributes patientStudySerieSopInstanceAttribute = optionalPatientStudySerieSopInstanceAttributes.get();
			// Instance
			Instance instance = this.buildSopInstanceFromAttribute(patientStudySerieSopInstanceAttribute);
			// Serie
			Serie serie = buildSerieFromAttribute(connector, patientStudySerieSopInstanceAttribute);
			// Study
			Study study = buildStudyFromAttribute(patientStudySerieSopInstanceAttribute);
			// Patient
			patient = buildPatientFromAttributes(patientStudySerieSopInstanceAttribute);
			// Add in lists
			serie.getInstances().add(instance);
			study.getSeries().add(serie);
			patient.getStudies().add(study);
		}

		return patient;
	}

	/**
	 * Create patient from Patient/Study/Serie Attributes
	 * @param patientStudySerieAttributes Attributes to use
	 * @param connector Connector
	 * @return Patient created
	 */
	private Patient createPatientFromPatientStudySerieAttributes(List<Attributes> patientStudySerieAttributes,
			ConnectorProperty connector) {
		Patient patient = null;
		// Retrieve serie
		Optional<Attributes> optionalPatientStudySerieAttributes = patientStudySerieAttributes.stream().findFirst();

		if (optionalPatientStudySerieAttributes.isPresent()) {
			Attributes patientStudySerieAttribute = optionalPatientStudySerieAttributes.get();
			// Serie
			Serie serie = buildSerieFromAttribute(connector, patientStudySerieAttribute);
			// Study
			Study study = buildStudyFromAttribute(patientStudySerieAttribute);
			// Patient
			patient = buildPatientFromAttributes(patientStudySerieAttribute);
			// Add in lists
			study.getSeries().add(serie);
			patient.getStudies().add(study);
		}

		return patient;
	}

	/**
	 * Execute dicom requests depending on parameters Dicom Params
	 * @param connector Connector
	 * @param queryRetrieveLevel Query Level
	 * @param dicomParams Dicom params
	 * @param useQueryRelational Flag to know if relational queries should be used
	 * @return List of attributes found
	 */
	private List<Attributes> retrieveDicomQueryResults(ConnectorProperty connector,
			QueryRetrieveLevel queryRetrieveLevel, List<DicomParam> dicomParams, boolean useQueryRelational) {
		DicomState state = CFind.process(
				connector.getDicomConnector().retrieveAdvancedParamsFromProperties(useQueryRelational),
				new DicomNode(connector.getDicomConnector().getCallingAet()),
				connector.getDicomConnector().retrieveDicomNodeFromProperties(), 0, queryRetrieveLevel,
				dicomParams.toArray(new DicomParam[] {}));

		// Retrieve query results from dicom
		return state.getDicomRSP();
	}

	/**
	 * Build new Patient from attributes in parameter
	 * @param attributesPatient Attributes to use
	 * @return Patient created
	 */
	private static Patient buildPatientFromAttributes(Attributes attributesPatient) {
		return new Patient(attributesPatient.getString(Tag.PatientID), attributesPatient.getString(Tag.PatientName),
				attributesPatient.getString(Tag.IssuerOfPatientID),
				DateTimeUtil.toLocalDate(attributesPatient.getDate(Tag.PatientBirthDate)),
				DateTimeUtil.toLocalTime(attributesPatient.getDate(Tag.PatientBirthTime)),
				DicomPatientSex.valueOf(attributesPatient.getString(Tag.PatientSex)));
	}

	/**
	 * Build new Instance from attributes in parameter
	 * @param patientStudySerieSopInstanceAttribute Attributes to use
	 * @return Instance created
	 */
	private Instance buildSopInstanceFromAttribute(Attributes patientStudySerieSopInstanceAttribute) {
		return new Instance(patientStudySerieSopInstanceAttribute.getString(Tag.SOPInstanceUID),
				patientStudySerieSopInstanceAttribute.getString(Tag.InstanceNumber) != null
						? Integer.parseInt(patientStudySerieSopInstanceAttribute.getString(Tag.InstanceNumber)) : null);
	}

	/**
	 * Build new Serie from attributes in parameter
	 * @param connector Connector
	 * @param patientStudySerieAttribute Attributes to use
	 * @return Serie created
	 */
	private static Serie buildSerieFromAttribute(ConnectorProperty connector, Attributes patientStudySerieAttribute) {
		return new Serie(patientStudySerieAttribute.getString(Tag.SeriesInstanceUID),
				patientStudySerieAttribute.getString(Tag.SeriesDescription),
				patientStudySerieAttribute.getString(Tag.SeriesNumber) != null
						? Integer.parseInt(patientStudySerieAttribute.getString(Tag.SeriesNumber)) : null,
				patientStudySerieAttribute.getString(Tag.Modality), connector.getWado().getTransferSyntaxUid(),
				connector.getWado().getCompressionRate());
	}

	/**
	 * Build new Study from attributes in parameter
	 * @param patientStudySerieAttribute Attributes to use
	 * @return Study created
	 */
	private static Study buildStudyFromAttribute(Attributes patientStudySerieAttribute) {
		return new Study(patientStudySerieAttribute.getString(Tag.StudyInstanceUID),
				patientStudySerieAttribute.getString(Tag.StudyDescription),
				DateTimeUtil.toLocalDate(patientStudySerieAttribute.getDate(Tag.StudyDate)),
				DateTimeUtil.toLocalTime(patientStudySerieAttribute.getDate(Tag.StudyTime)),
				patientStudySerieAttribute.getString(Tag.AccessionNumber),
				patientStudySerieAttribute.getString(Tag.StudyID),
				patientStudySerieAttribute.getString(Tag.ReferringPhysicianName));
	}

	/**
	 * Retrieve series from study instance uids and fill patient in parameter
	 * @param patient Patient to fill
	 * @param connector Connector
	 */
	private void retrieveDicomSeriesFromStudyInstanceUid(Patient patient, ConnectorProperty connector) {
		patient.getStudies().forEach(study -> {
			// Define and process dicom query to retrieve series from study instance uid
			List<Attributes> seriesAttributes = this.retrieveDicomQueryResults(connector, QueryRetrieveLevel.SERIES,
					this.defineSeriesDicomParamsFromStudyInstanceUid(study.getStudyInstanceUID()), false);
			// Retrieve series from attributes and update study
			study
				.setSeries(seriesAttributes.stream()
					.map(s -> new Serie(s.getString(Tag.SeriesInstanceUID), s.getString(Tag.SeriesDescription),
							s.getString(Tag.SeriesNumber) == null ? null
									: Integer.parseInt(s.getString(Tag.SeriesNumber)),
							s.getString(Tag.Modality), connector.getWado().getTransferSyntaxUid(),
							connector.getWado().getCompressionRate()))
					.collect(Collectors.toSet()));
		});
	}

	/**
	 * Retrieve Sop instances from study/serie instance uids and fill patient in parameter
	 * @param patient Patient to fill
	 * @param connector Connector
	 */
	private void retrieveDicomSopInstancesFromStudySerieInstanceUids(Patient patient, ConnectorProperty connector) {
		patient.getStudies().forEach(study -> study.getSeries().forEach(serie -> {
			// Define and process dicom query to retrieve sop instances from serie
			// instance uid and study instance uid
			List<Attributes> sopInstancesAttributes = this.retrieveDicomQueryResults(connector,
					QueryRetrieveLevel.IMAGE, this.defineSopInstanceDicomParamsFromStudySerieInstanceUids(
							study.getStudyInstanceUID(), serie.getSeriesInstanceUID()),
					false);

			// Retrieve instances from attributes and update serie
			serie.setInstances(sopInstancesAttributes.stream()
				.map(s -> new Instance(s.getString(Tag.SOPInstanceUID),
						s.getString(Tag.InstanceNumber) == null ? null
								: Integer.parseInt(s.getString(Tag.InstanceNumber))))
				.collect(Collectors.toSet()));
		}));
	}

	/**
	 * Define dicom params with StudyInstanceUID and SeriesInstanceUID as search criteria
	 * @param studyInstanceUID search criteria
	 * @param seriesInstanceUID search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> defineSopInstanceDicomParamsFromStudySerieInstanceUids(String studyInstanceUID,
			String seriesInstanceUID) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.StudyInstanceUID, studyInstanceUID),
				new DicomParam(Tag.SeriesInstanceUID, seriesInstanceUID),
				// Return Keys
				CFind.SOPInstanceUID, CFind.InstanceNumber);
	}

	/**
	 * Define dicom params with AccessionNumber as search criteria
	 * @param accessionNumber search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudiesDicomParamsFromAccessionNumber(String accessionNumber) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.AccessionNumber, accessionNumber),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime,
				CFind.StudyInstanceUID, CFind.StudyID);
	}

	/**
	 * Define dicom params with StudyInstanceUID as search criteria
	 * @param studyInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> defineSeriesDicomParamsFromStudyInstanceUid(String studyInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.StudyInstanceUID, studyInstanceUid),
				// Return Keys
				CFind.SeriesInstanceUID, CFind.Modality, CFind.SeriesNumber, CFind.SeriesDescription);
	}

	/**
	 * Define dicom params with PatientID and IssuerOfPatientID as search criteria
	 * @param patientId search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudiesDicomParamsFromPatientId(String patientId) {
		// Determine dicom params patient id / issuer of patient id depending on the HL7
		// syntax
		DicomParam dicomParamPatientID = new DicomParam(Tag.PatientID,
				ConnectorUtil.determinePatientIdDependingHl7Syntax(patientId));
		DicomParam dicomParamIssuerOfPatientID = new DicomParam(Tag.IssuerOfPatientID,
				ConnectorUtil.determineIssuerPatientIdDependingHl7Syntax(patientId));

		return Arrays.asList(
				// Matching Keys
				dicomParamPatientID, dicomParamIssuerOfPatientID,
				// Return Keys
				CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex, CFind.ReferringPhysicianName,
				CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime, CFind.StudyInstanceUID, CFind.StudyID);
	}

	/**
	 * Define dicom params with StudyInstanceUID as search criteria
	 * @param studyInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudiesDicomParamsFromStudyInstanceUid(String studyInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.StudyInstanceUID, studyInstanceUid),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime, CFind.StudyID);
	}

	/**
	 * Define dicom params with SeriesInstanceUID as search criteria
	 * @param serieInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudySerieDicomParamsFromSerieInstanceUid(String serieInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.SeriesInstanceUID, serieInstanceUid),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime,
				CFind.AccessionNumber, CFind.StudyInstanceUID, CFind.StudyID, CFind.Modality, CFind.SeriesNumber,
				CFind.SeriesDescription);
	}

	/**
	 * Define dicom params with SOPInstanceUID as search criteria
	 * @param sopInstanceUid search criteria
	 * @return List of DicomParam search criteria created
	 */
	private List<DicomParam> definePatientStudySerieSopInstanceDicomParamsFromSopInstanceUid(String sopInstanceUid) {
		return Arrays.asList(
				// Matching Keys
				new DicomParam(Tag.SOPInstanceUID, sopInstanceUid),
				// Return Keys
				CFind.PatientID, CFind.IssuerOfPatientID, CFind.PatientName, CFind.PatientBirthDate, CFind.PatientSex,
				CFind.ReferringPhysicianName, CFind.StudyDescription, CFind.StudyDate, CFind.StudyTime,
				CFind.AccessionNumber, CFind.StudyInstanceUID, CFind.StudyID, CFind.Modality, CFind.SeriesNumber,
				CFind.SeriesDescription, CFind.InstanceNumber);
	}

}
