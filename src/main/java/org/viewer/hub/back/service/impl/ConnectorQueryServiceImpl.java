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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.enums.QueryLevelType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.service.ConnectorQueryService;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.DbConnectorQueryService;
import org.viewer.hub.back.service.DicomConnectorQueryService;

import java.util.*;

@Service
public class ConnectorQueryServiceImpl implements ConnectorQueryService {

    // Services
    private final DbConnectorQueryService dbConnectorQueryService;

    private final DicomConnectorQueryService dicomConnectorQueryService;

    private final ConnectorService connectorService;

    @Autowired
    public ConnectorQueryServiceImpl(final DbConnectorQueryService dbConnectorQueryService, final DicomConnectorQueryService dicomConnectorQueryService, final ConnectorService connectorService) {
        this.dbConnectorQueryService = dbConnectorQueryService;
        this.dicomConnectorQueryService = dicomConnectorQueryService;
        this.connectorService = connectorService;
    }

    @Override
    public Map<String, Set<Patient>> retrievePatientsByArchiveWithoutIHESearchCriteria(ArchiveSearchCriteria searchCriteria, Authentication authentication) {
        Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(searchCriteria.getArchive())).forEach(archive -> {
            Set<Patient> patients = retrievePatientsWithoutIHESearchCriteria(searchCriteria, Set.of(archive.getId()), authentication);
            if (!patients.isEmpty()) {
                patientsByArchive.put(archive.getId(), patients);
            }
        });
        return patientsByArchive;
    }

    @Override
    public Map<String, Set<Patient>> retrievePatientsByArchiveWithIHESearchCriteria(IHESearchCriteria searchCriteria, Authentication authentication) {
        Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(searchCriteria.getArchive())).forEach(archive -> {
            Set<Patient> patients = retrievePatientsWithIHESearchCriteria(searchCriteria, Set.of(archive.getId()), authentication);
            if (!patients.isEmpty()) {
                patientsByArchive.put(archive.getId(), patients);
            }
        });
        return patientsByArchive;
    }

    @Override
    public Set<Patient> retrievePatientsWithoutIHESearchCriteria(ArchiveSearchCriteria searchCriteria, Set<String> archives, Authentication authentication) {
        Set<Patient> patients = new HashSet<>();

        // Sop Instance Uid
        if (!searchCriteria.getObjectUID().isEmpty()) {
            buildFromSopInstanceUids(patients, searchCriteria.getObjectUID(), archives, authentication);
        }
        // Series Instance Uid
        if (!searchCriteria.getSeriesUID().isEmpty()) {
            buildFromSeriesInstanceUids(patients, searchCriteria.getSeriesUID(), archives, authentication);
        }
        // Accession Number
        if (!searchCriteria.getAccessionNumber().isEmpty()) {
            buildFromStudyAccessionNumbers(patients, searchCriteria.getAccessionNumber(), archives, authentication);
        }
        // Study Uid
        if (!searchCriteria.getStudyUID().isEmpty()) {
            buildFromStudyInstanceUids(patients, searchCriteria.getStudyUID(), archives, authentication);
        }
        // Patient ID
        if (!searchCriteria.getPatientID().isEmpty()) {
            buildFromPatientIds(patients, searchCriteria.getPatientID(), archives, authentication);
        }

        // Apply search criteria filters
        return searchCriteria.applyPatientRequestSearchCriteriaFilters(patients);
    }

    @Override
    public Set<Patient> retrievePatientsWithIHESearchCriteria(IHESearchCriteria searchCriteria, Set<String> archives, Authentication authentication) {
        Set<Patient> patients = new HashSet<>();

        // Study level
        if (searchCriteria.getRequestType() == IHERequestType.STUDY) {
            if (!searchCriteria.getAccessionNumber().isEmpty()) {
                buildFromStudyAccessionNumbers(patients, searchCriteria.getAccessionNumber(), archives, authentication);
            }
            else if (!searchCriteria.getStudyUID().isEmpty()) {
                buildFromStudyInstanceUids(patients, searchCriteria.getStudyUID(), archives, authentication);
            }
        }
        // Patient level
        else if (searchCriteria.getRequestType() == IHERequestType.PATIENT) {
            buildFromPatientIds(patients, Set.of(searchCriteria.getPatientID()), archives, authentication);
        }

        // Apply search criteria filters
        return searchCriteria.applyPatientRequestSearchCriteriaFilters(patients);
    }



    @Override
    public void buildFromSopInstanceUids(Set<Patient> patients, Set<String> sopInstanceUids, Set<String> archives, Authentication authentication) {
        // Retrieve default or specific connectors
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(archives)).forEach(connector -> {
            if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.SOP_INSTANCE_UID)) {
                if (Objects.equals(ConnectorType.DB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dbConnectorQueryService
                            .retrievePatientsFromSopInstanceUidsDbConnector(sopInstanceUids, connector));
                }
                else if (Objects.equals(ConnectorType.DICOM, connector.getType())
                        || Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dicomConnectorQueryService.retrievePatientsFromSopInstanceUidsDicomConnector(
                            sopInstanceUids, connector, authentication));
                }
            }
        });
    }

    @Override
    public void buildFromSeriesInstanceUids(Set<Patient> patients, Set<String> seriesUids, Set<String> archives, Authentication authentication) {
        // Retrieve default or specific connectors
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(archives)).forEach(connector -> {
            if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.SERIE_INSTANCE_UID)) {
                if (Objects.equals(ConnectorType.DB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dbConnectorQueryService
                            .retrievePatientsFromSeriesInstanceUidsDbConnector(seriesUids, connector));
                }
                else if (Objects.equals(ConnectorType.DICOM, connector.getType())
                        || Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dicomConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDicomConnector(
                            seriesUids, connector, authentication));
                }
            }
        });
    }

    @Override
    public void buildFromStudyAccessionNumbers(Set<Patient> patients, Set<String> accessionNumbers, Set<String> archives, Authentication authentication) {
        // Retrieve default or specific connectors
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(archives)).forEach(connector -> {
            if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.STUDY_ACCESSION_NUMBER)) {
                if (Objects.equals(ConnectorType.DB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dbConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDbConnector(
                            accessionNumbers, connector));
                }
                else if (Objects.equals(ConnectorType.DICOM, connector.getType())
                        || Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dicomConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDicomConnector(
                            accessionNumbers, connector, authentication));
                }
            }
        });
    }

    @Override
    public void buildFromStudyInstanceUids(Set<Patient> patients, Set<String> studyUids, Set<String> archives, Authentication authentication) {
        // Retrieve default or specific connectors
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(archives)).forEach(connector -> {
            if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.STUDY_INSTANCE_UID)) {
                if (Objects.equals(ConnectorType.DB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dbConnectorQueryService
                            .retrievePatientsFromStudyInstanceUidsDbConnector(studyUids, connector));
                }
                else if (Objects.equals(ConnectorType.DICOM, connector.getType())
                        || Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dicomConnectorQueryService.retrievePatientsFromStudyInstanceUidsDicomConnector(
                            studyUids, connector, authentication));
                }
            }
        });
    }

    @Override
    public void buildFromPatientIds(Set<Patient> patients, Set<String> patientIds, Set<String> archives, Authentication authentication) {
        // Retrieve default or specific connectors
        this.connectorService.retrieveConnectors(new LinkedHashSet<>(archives)).forEach(connector -> {
            if (!connector.getSearchCriteria().getDeactivated().contains(QueryLevelType.PATIENT_ID)) {
                if (Objects.equals(ConnectorType.DB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dbConnectorQueryService
                            .retrievePatientsFromPatientIdsDbConnector(patientIds, connector));
                }
                else if (Objects.equals(ConnectorType.DICOM, connector.getType())
                        || Objects.equals(ConnectorType.DICOM_WEB, connector.getType())) {
                    // Update list with patients found
                    Patient.mergePatients(patients, this.dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                            patientIds, connector, authentication));
                }
            }
        });
    }
}
