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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.patient.Serie;
import org.viewer.hub.back.model.patient.Study;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.property.SearchCriteriaProperty;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.service.ConnectorService;
import org.viewer.hub.back.service.DbConnectorQueryService;
import org.viewer.hub.back.service.DicomConnectorQueryService;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ConnectorQueryServiceImplTest {

    @Mock
    private DbConnectorQueryService dbConnectorQueryService;

    @Mock
    private DicomConnectorQueryService dicomConnectorQueryService;

    @Mock
    private ConnectorService connectorService;

    @InjectMocks
    private ConnectorQueryServiceImpl connectorQueryService;

    @Mock
    private Authentication authentication;

    private ConnectorProperty dicomConnector;

    @BeforeEach
    void setUp() {
        dicomConnector = ConnectorProperty.builder()
                .id("dicom-archive")
                .type(ConnectorType.DICOM)
                .searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
                .build();
    }

    @Test
    void when_retrievePatientsWithoutIHESearchCriteria_with_patientId_should_returnPatients() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        LinkedHashSet<String> patientIds = new LinkedHashSet<>();
        patientIds.add("P001");
        searchCriteria.setPatientID(patientIds);

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // Mock patient ID query
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithoutIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertEquals(1, result.size());
        assertEquals("P001", result.iterator().next().getPatientID());
    }

    @Test
    void when_retrievePatientsWithIHESearchCriteria_with_patientLevel_should_returnPatients() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        searchCriteria.setRequestType(IHERequestType.PATIENT);
        searchCriteria.setPatientID("P001");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // Mock patient ID query
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertEquals(1, result.size());
        assertEquals("P001", result.iterator().next().getPatientID());
    }

    @Test
    void when_retrievePatientsByArchiveWithoutIHESearchCriteria_with_dicomConnector_should_returnPatientsByArchive() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        LinkedHashSet<String> archives = new LinkedHashSet<>();
        archives.add("dicom-archive");
        searchCriteria.setArchive(archives);

        LinkedHashSet<String> patientIds = new LinkedHashSet<>();
        patientIds.add("P001");
        searchCriteria.setPatientID(patientIds);

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // Mock patient ID query
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Map<String, Set<Patient>> result = connectorQueryService
                .retrievePatientsByArchiveWithoutIHESearchCriteria(searchCriteria, authentication);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertTrue(result.containsKey("dicom-archive"));
        assertEquals(1, result.get("dicom-archive").size());
    }

    @Test
    void when_retrievePatientsByArchiveWithIHESearchCriteria_with_patientLevel_should_returnPatientsByArchive() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        LinkedHashSet<String> archives = new LinkedHashSet<>();
        archives.add("dicom-archive");
        searchCriteria.setArchive(archives);
        searchCriteria.setRequestType(IHERequestType.PATIENT);
        searchCriteria.setPatientID("P001");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // Mock patient ID query
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Map<String, Set<Patient>> result = connectorQueryService
                .retrievePatientsByArchiveWithIHESearchCriteria(searchCriteria, authentication);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertTrue(result.containsKey("dicom-archive"));
        assertEquals(1, result.get("dicom-archive").size());
        assertEquals("P001", result.get("dicom-archive").iterator().next().getPatientID());
    }

    @Test
    void when_retrievePatientsByArchiveWithoutIHESearchCriteria_with_emptyResults_should_returnEmptyMap() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        LinkedHashSet<String> archives = new LinkedHashSet<>();
        archives.add("dicom-archive");
        searchCriteria.setArchive(archives);

        LinkedHashSet<String> patientIds = new LinkedHashSet<>();
        patientIds.add("P999");
        searchCriteria.setPatientID(patientIds);

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);
        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new HashSet<>());

        // When
        Map<String, Set<Patient>> result = connectorQueryService
                .retrievePatientsByArchiveWithoutIHESearchCriteria(searchCriteria, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void debug_test_filterMethod() {
        // Test that our patient creation method works correctly
        Patient patient = createPatientWithStudy("TEST001");

        assertNotNull(patient);
        assertNotNull(patient.getStudies());
        assertFalse(patient.getStudies().isEmpty());

        Study study = patient.getStudies().iterator().next();
        assertNotNull(study);
        assertNotNull(study.getSeries());
        assertFalse(study.getSeries().isEmpty());

        // Test that filter doesn't remove patients
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        Set<Patient> patients = new HashSet<>();
        patients.add(patient);

        Set<Patient> filtered = searchCriteria.applyPatientRequestSearchCriteriaFilters(patients);

        assertNotNull(filtered);
        assertFalse(filtered.isEmpty(), "Filtered patients should not be empty");
        assertEquals(1, filtered.size());
    }

    @Test
    void when_retrievePatientsByArchiveWithIHESearchCriteria_with_emptyResults_should_returnEmptyMap() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        LinkedHashSet<String> archives = new LinkedHashSet<>();
        archives.add("dicom-archive");
        searchCriteria.setArchive(archives);
        searchCriteria.setRequestType(IHERequestType.PATIENT);
        searchCriteria.setPatientID("P999");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);
        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new HashSet<>());

        // When
        Map<String, Set<Patient>> result = connectorQueryService
                .retrievePatientsByArchiveWithIHESearchCriteria(searchCriteria, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    // ========== Tests for buildFromSopInstanceUids ==========

    @Test
    void when_buildFromSopInstanceUids_with_dbConnector_should_callDbService() {
        // Given
        ConnectorProperty dbConnector = createDbConnector();
        Set<Patient> patients = new HashSet<>();
        Set<String> sopInstanceUids = new HashSet<>();
        sopInstanceUids.add("1.2.3.4.5.6");
        Set<String> archives = new HashSet<>();
        archives.add("db-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dbConnectorQueryService.retrievePatientsFromSopInstanceUidsDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromSopInstanceUids(patients, sopInstanceUids, archives, authentication);

        // Then
        Mockito.verify(dbConnectorQueryService).retrievePatientsFromSopInstanceUidsDbConnector(
                Mockito.eq(sopInstanceUids), Mockito.eq(dbConnector));
        assertEquals(1, patients.size());
    }

    @Test
    void when_buildFromSopInstanceUids_with_dicomConnector_should_callDicomService() {
        // Given
        Set<Patient> patients = new HashSet<>();
        Set<String> sopInstanceUids = new HashSet<>();
        sopInstanceUids.add("1.2.3.4.5.6");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSopInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromSopInstanceUids(patients, sopInstanceUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSopInstanceUidsDicomConnector(
                Mockito.eq(sopInstanceUids), Mockito.eq(dicomConnector), Mockito.eq(authentication));
        assertEquals(1, patients.size());
    }

    @Test
    void when_buildFromSopInstanceUids_with_dicomWebConnector_should_callDicomService() {
        // Given
        ConnectorProperty dicomWebConnector = createDicomWebConnector();
        Set<Patient> patients = new HashSet<>();
        Set<String> sopInstanceUids = new HashSet<>();
        sopInstanceUids.add("1.2.3.4.5.6");
        Set<String> archives = new HashSet<>();
        archives.add("dicomweb-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomWebConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSopInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromSopInstanceUids(patients, sopInstanceUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSopInstanceUidsDicomConnector(
                Mockito.eq(sopInstanceUids), Mockito.eq(dicomWebConnector), Mockito.eq(authentication));
        assertEquals(1, patients.size());
    }

    // ========== Tests for buildFromSeriesInstanceUids ==========

    @Test
    void when_buildFromSeriesInstanceUids_with_dbConnector_should_callDbService() {
        // Given
        ConnectorProperty dbConnector = createDbConnector();
        Set<Patient> patients = new HashSet<>();
        Set<String> seriesUids = new HashSet<>();
        seriesUids.add("1.2.3.4.5");
        Set<String> archives = new HashSet<>();
        archives.add("db-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dbConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromSeriesInstanceUids(patients, seriesUids, archives, authentication);

        // Then
        Mockito.verify(dbConnectorQueryService).retrievePatientsFromSeriesInstanceUidsDbConnector(
                Mockito.eq(seriesUids), Mockito.eq(dbConnector));
        assertEquals(1, patients.size());
    }

    @Test
    void when_buildFromSeriesInstanceUids_with_dicomConnector_should_callDicomService() {
        // Given
        Set<Patient> patients = new HashSet<>();
        Set<String> seriesUids = new HashSet<>();
        seriesUids.add("1.2.3.4.5");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromSeriesInstanceUids(patients, seriesUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSeriesInstanceUidsDicomConnector(
                Mockito.eq(seriesUids), Mockito.eq(dicomConnector), Mockito.eq(authentication));
        assertEquals(1, patients.size());
    }

    // ========== Tests for buildFromStudyAccessionNumbers ==========

    @Test
    void when_buildFromStudyAccessionNumbers_with_dbConnector_should_callDbService() {
        // Given
        ConnectorProperty dbConnector = createDbConnector();
        Set<Patient> patients = new HashSet<>();
        Set<String> accessionNumbers = new HashSet<>();
        accessionNumbers.add("ACC001");
        Set<String> archives = new HashSet<>();
        archives.add("db-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dbConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromStudyAccessionNumbers(patients, accessionNumbers, archives, authentication);

        // Then
        Mockito.verify(dbConnectorQueryService).retrievePatientsFromStudyAccessionNumbersDbConnector(
                Mockito.eq(accessionNumbers), Mockito.eq(dbConnector));
        assertEquals(1, patients.size());
    }

    @Test
    void when_buildFromStudyAccessionNumbers_with_dicomConnector_should_callDicomService() {
        // Given
        Set<Patient> patients = new HashSet<>();
        Set<String> accessionNumbers = new HashSet<>();
        accessionNumbers.add("ACC001");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromStudyAccessionNumbers(patients, accessionNumbers, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromStudyAccessionNumbersDicomConnector(
                Mockito.eq(accessionNumbers), Mockito.eq(dicomConnector), Mockito.eq(authentication));
        assertEquals(1, patients.size());
    }

    // ========== Tests for buildFromStudyInstanceUids ==========

    @Test
    void when_buildFromStudyInstanceUids_with_dbConnector_should_callDbService() {
        // Given
        ConnectorProperty dbConnector = createDbConnector();
        Set<Patient> patients = new HashSet<>();
        Set<String> studyUids = new HashSet<>();
        studyUids.add("1.2.3.4");
        Set<String> archives = new HashSet<>();
        archives.add("db-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dbConnectorQueryService.retrievePatientsFromStudyInstanceUidsDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromStudyInstanceUids(patients, studyUids, archives, authentication);

        // Then
        Mockito.verify(dbConnectorQueryService).retrievePatientsFromStudyInstanceUidsDbConnector(
                Mockito.eq(studyUids), Mockito.eq(dbConnector));
        assertEquals(1, patients.size());
    }

    @Test
    void when_buildFromStudyInstanceUids_with_dicomConnector_should_callDicomService() {
        // Given
        Set<Patient> patients = new HashSet<>();
        Set<String> studyUids = new HashSet<>();
        studyUids.add("1.2.3.4");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromStudyInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromStudyInstanceUids(patients, studyUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromStudyInstanceUidsDicomConnector(
                Mockito.eq(studyUids), Mockito.eq(dicomConnector), Mockito.eq(authentication));
        assertEquals(1, patients.size());
    }

    // ========== Tests for buildFromPatientIds ==========

    @Test
    void when_buildFromPatientIds_with_dbConnector_should_callDbService() {
        // Given
        ConnectorProperty dbConnector = createDbConnector();
        Set<Patient> patients = new HashSet<>();
        Set<String> patientIds = new HashSet<>();
        patientIds.add("P001");
        Set<String> archives = new HashSet<>();
        archives.add("db-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dbConnectorQueryService.retrievePatientsFromPatientIdsDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromPatientIds(patients, patientIds, archives, authentication);

        // Then
        Mockito.verify(dbConnectorQueryService).retrievePatientsFromPatientIdsDbConnector(
                Mockito.eq(patientIds), Mockito.eq(dbConnector));
        assertEquals(1, patients.size());
    }

    @Test
    void when_buildFromPatientIds_with_dicomConnector_should_callDicomService() {
        // Given
        Set<Patient> patients = new HashSet<>();
        Set<String> patientIds = new HashSet<>();
        patientIds.add("P001");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        connectorQueryService.buildFromPatientIds(patients, patientIds, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromPatientIdsDicomConnector(
                Mockito.eq(patientIds), Mockito.eq(dicomConnector), Mockito.eq(authentication));
        assertEquals(1, patients.size());
    }

    // ========== Tests for multiple UID types ==========

    @Test
    void when_retrievePatientsWithoutIHESearchCriteria_with_multipleUidTypes_should_callAllServices() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        LinkedHashSet<String> objectUIDs = new LinkedHashSet<>();
        objectUIDs.add("1.2.3.4.5.6");
        searchCriteria.setObjectUID(objectUIDs);

        LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
        seriesUIDs.add("1.2.3.4.5");
        searchCriteria.setSeriesUID(seriesUIDs);

        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSopInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P002"));

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithoutIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSopInstanceUidsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSeriesInstanceUidsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        assertNotNull(result);
    }

    @Test
    void when_retrievePatientsWithIHESearchCriteria_with_studyLevelAndStudyUID_should_callStudyUidService() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        searchCriteria.setRequestType(IHERequestType.STUDY);
        LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
        studyUIDs.add("1.2.3.4");
        searchCriteria.setStudyUID(studyUIDs);

        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromStudyInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromStudyInstanceUidsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void when_retrievePatientsWithIHESearchCriteria_with_studyLevelAndAccessionNumber_should_callAccessionNumberService() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        searchCriteria.setRequestType(IHERequestType.STUDY);
        LinkedHashSet<String> accessionNumbers = new LinkedHashSet<>();
        accessionNumbers.add("ACC001");
        searchCriteria.setAccessionNumber(accessionNumbers);

        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(createPatientSet("P001"));

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromStudyAccessionNumbersDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== Helper methods ==========

    /**
     * Helper method to create a patient with studies and series for testing
     */
    private Patient createPatientWithStudy(String patientId) {
        Patient patient = new Patient();
        patient.setPatientID(patientId);

        Set<Serie> series = new HashSet<>();
        Serie serie = Serie.builder()
                .instances(new HashSet<>()) // Initialize instances to avoid NullPointerException
                .seriesInstanceUID("1.2.3.4.5.1")
                .seriesDescription("Test Series")
                .modality("CT")
                .build();
        series.add(serie);

        Study study = Study.builder()
                .studyInstanceUID("1.2.3.4.5")
                .studyDescription("Test Study")
                .series(series)
                .build();

        Set<Study> studies = new HashSet<>();
        studies.add(study);
        patient.setStudies(studies);

        return patient;
    }

    private ConnectorProperty createDbConnector() {
        return ConnectorProperty.builder()
                .id("db-archive")
                .type(ConnectorType.DB)
                .searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
                .build();
    }

    private ConnectorProperty createDicomWebConnector() {
        return ConnectorProperty.builder()
                .id("dicomweb-archive")
                .type(ConnectorType.DICOM_WEB)
                .searchCriteria(new SearchCriteriaProperty(new HashSet<>()))
                .build();
    }

    private Set<Patient> createPatientSet(String patientId) {
        Set<Patient> result = new HashSet<>();
        result.add(createPatientWithStudy(patientId));
        return result;
    }

    // ========== Tests for QueryLevelType deactivated ==========

    @Test
    void when_buildFromSopInstanceUids_with_deactivatedQueryLevel_should_notCallService() {
        // Given
        Set<org.viewer.hub.back.enums.QueryLevelType> deactivated = new HashSet<>();
        deactivated.add(org.viewer.hub.back.enums.QueryLevelType.SOP_INSTANCE_UID);
        ConnectorProperty connectorWithDeactivated = ConnectorProperty.builder()
                .id("dicom-archive")
                .type(ConnectorType.DICOM)
                .searchCriteria(new SearchCriteriaProperty(deactivated))
                .build();

        Set<Patient> patients = new HashSet<>();
        Set<String> sopInstanceUids = new HashSet<>();
        sopInstanceUids.add("1.2.3.4.5.6");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(connectorWithDeactivated);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // When
        connectorQueryService.buildFromSopInstanceUids(patients, sopInstanceUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService, Mockito.never())
                .retrievePatientsFromSopInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(patients.isEmpty());
    }

    @Test
    void when_buildFromSeriesInstanceUids_with_deactivatedQueryLevel_should_notCallService() {
        // Given
        Set<org.viewer.hub.back.enums.QueryLevelType> deactivated = new HashSet<>();
        deactivated.add(org.viewer.hub.back.enums.QueryLevelType.SERIE_INSTANCE_UID);
        ConnectorProperty connectorWithDeactivated = ConnectorProperty.builder()
                .id("dicom-archive")
                .type(ConnectorType.DICOM)
                .searchCriteria(new SearchCriteriaProperty(deactivated))
                .build();

        Set<Patient> patients = new HashSet<>();
        Set<String> seriesUids = new HashSet<>();
        seriesUids.add("1.2.3.4.5");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(connectorWithDeactivated);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // When
        connectorQueryService.buildFromSeriesInstanceUids(patients, seriesUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService, Mockito.never())
                .retrievePatientsFromSeriesInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(patients.isEmpty());
    }

    @Test
    void when_buildFromStudyAccessionNumbers_with_deactivatedQueryLevel_should_notCallService() {
        // Given
        Set<org.viewer.hub.back.enums.QueryLevelType> deactivated = new HashSet<>();
        deactivated.add(org.viewer.hub.back.enums.QueryLevelType.STUDY_ACCESSION_NUMBER);
        ConnectorProperty connectorWithDeactivated = ConnectorProperty.builder()
                .id("dicom-archive")
                .type(ConnectorType.DICOM)
                .searchCriteria(new SearchCriteriaProperty(deactivated))
                .build();

        Set<Patient> patients = new HashSet<>();
        Set<String> accessionNumbers = new HashSet<>();
        accessionNumbers.add("ACC001");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(connectorWithDeactivated);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // When
        connectorQueryService.buildFromStudyAccessionNumbers(patients, accessionNumbers, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService, Mockito.never())
                .retrievePatientsFromStudyAccessionNumbersDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(patients.isEmpty());
    }

    @Test
    void when_buildFromStudyInstanceUids_with_deactivatedQueryLevel_should_notCallService() {
        // Given
        Set<org.viewer.hub.back.enums.QueryLevelType> deactivated = new HashSet<>();
        deactivated.add(org.viewer.hub.back.enums.QueryLevelType.STUDY_INSTANCE_UID);
        ConnectorProperty connectorWithDeactivated = ConnectorProperty.builder()
                .id("dicom-archive")
                .type(ConnectorType.DICOM)
                .searchCriteria(new SearchCriteriaProperty(deactivated))
                .build();

        Set<Patient> patients = new HashSet<>();
        Set<String> studyUids = new HashSet<>();
        studyUids.add("1.2.3.4");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(connectorWithDeactivated);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // When
        connectorQueryService.buildFromStudyInstanceUids(patients, studyUids, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService, Mockito.never())
                .retrievePatientsFromStudyInstanceUidsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(patients.isEmpty());
    }

    @Test
    void when_buildFromPatientIds_with_deactivatedQueryLevel_should_notCallService() {
        // Given
        Set<org.viewer.hub.back.enums.QueryLevelType> deactivated = new HashSet<>();
        deactivated.add(org.viewer.hub.back.enums.QueryLevelType.PATIENT_ID);
        ConnectorProperty connectorWithDeactivated = ConnectorProperty.builder()
                .id("dicom-archive")
                .type(ConnectorType.DICOM)
                .searchCriteria(new SearchCriteriaProperty(deactivated))
                .build();

        Set<Patient> patients = new HashSet<>();
        Set<String> patientIds = new HashSet<>();
        patientIds.add("P001");
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(connectorWithDeactivated);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        // When
        connectorQueryService.buildFromPatientIds(patients, patientIds, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService, Mockito.never())
                .retrievePatientsFromPatientIdsDicomConnector(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(patients.isEmpty());
    }

    // ========== Tests for empty search criteria ==========

    @Test
    void when_retrievePatientsWithoutIHESearchCriteria_with_emptySearchCriteria_should_returnEmptySet() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithoutIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void when_retrievePatientsWithIHESearchCriteria_with_studyLevelAndEmptyAccessionAndStudyUID_should_returnEmptySet() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        searchCriteria.setRequestType(IHERequestType.STUDY);
        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== Tests for multiple archives ==========

    @Test
    void when_retrievePatientsByArchiveWithoutIHESearchCriteria_with_multipleArchives_should_returnPatientsGroupedByArchive() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
        LinkedHashSet<String> archives = new LinkedHashSet<>();
        archives.add("dicom-archive");
        archives.add("db-archive");
        searchCriteria.setArchive(archives);

        LinkedHashSet<String> patientIds = new LinkedHashSet<>();
        patientIds.add("P001");
        searchCriteria.setPatientID(patientIds);

        ConnectorProperty dbConnector = createDbConnector();
        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        Mockito.when(dbConnectorQueryService.retrievePatientsFromPatientIdsDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Map<String, Set<Patient>> result = connectorQueryService
                .retrievePatientsByArchiveWithoutIHESearchCriteria(searchCriteria, authentication);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void when_retrievePatientsByArchiveWithIHESearchCriteria_with_multipleArchives_should_returnPatientsGroupedByArchive() {
        // Given
        IHESearchCriteria searchCriteria = new IHESearchCriteria();
        LinkedHashSet<String> archives = new LinkedHashSet<>();
        archives.add("dicom-archive");
        archives.add("db-archive");
        searchCriteria.setArchive(archives);
        searchCriteria.setRequestType(IHERequestType.PATIENT);
        searchCriteria.setPatientID("P001");

        ConnectorProperty dbConnector = createDbConnector();
        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);
        connectors.add(dbConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        Mockito.when(dbConnectorQueryService.retrievePatientsFromPatientIdsDbConnector(
                        Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Map<String, Set<Patient>> result = connectorQueryService
                .retrievePatientsByArchiveWithIHESearchCriteria(searchCriteria, authentication);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ========== Tests for all UID types in ArchiveSearchCriteria ==========

    @Test
    void when_retrievePatientsWithoutIHESearchCriteria_with_allUidTypes_should_callAllServices() {
        // Given
        ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();

        LinkedHashSet<String> objectUIDs = new LinkedHashSet<>();
        objectUIDs.add("1.2.3.4.5.6");
        searchCriteria.setObjectUID(objectUIDs);

        LinkedHashSet<String> seriesUIDs = new LinkedHashSet<>();
        seriesUIDs.add("1.2.3.4.5");
        searchCriteria.setSeriesUID(seriesUIDs);

        LinkedHashSet<String> accessionNumbers = new LinkedHashSet<>();
        accessionNumbers.add("ACC001");
        searchCriteria.setAccessionNumber(accessionNumbers);

        LinkedHashSet<String> studyUIDs = new LinkedHashSet<>();
        studyUIDs.add("1.2.3.4");
        searchCriteria.setStudyUID(studyUIDs);

        LinkedHashSet<String> patientIDs = new LinkedHashSet<>();
        patientIDs.add("P001");
        searchCriteria.setPatientID(patientIDs);

        Set<String> archives = new HashSet<>();
        archives.add("dicom-archive");

        LinkedHashSet<ConnectorProperty> connectors = new LinkedHashSet<>();
        connectors.add(dicomConnector);

        Mockito.when(connectorService.retrieveConnectors(Mockito.any()))
                .thenReturn(connectors);

        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSopInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromSeriesInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P002"));
                    return result;
                });
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromStudyAccessionNumbersDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P003"));
                    return result;
                });
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromStudyInstanceUidsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P004"));
                    return result;
                });
        Mockito.when(dicomConnectorQueryService.retrievePatientsFromPatientIdsDicomConnector(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Set<Patient> result = new HashSet<>();
                    result.add(createPatientWithStudy("P001"));
                    return result;
                });

        // When
        Set<Patient> result = connectorQueryService
                .retrievePatientsWithoutIHESearchCriteria(searchCriteria, archives, authentication);

        // Then
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSopInstanceUidsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromSeriesInstanceUidsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromStudyAccessionNumbersDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromStudyInstanceUidsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dicomConnectorQueryService).retrievePatientsFromPatientIdsDicomConnector(
                Mockito.any(), Mockito.any(), Mockito.any());
        assertNotNull(result);
    }

}

