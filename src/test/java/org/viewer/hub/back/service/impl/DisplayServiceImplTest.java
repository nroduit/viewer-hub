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
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.service.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DisplayServiceImplTest {

	@Mock
	private ViewerSelectionService viewerSelectionService;

	@Mock
	private WeasisDisplayService weasisDisplayService;

	@Mock
	private OhifDisplayService ohifDisplayService;

	@Mock
	private SlicerDisplayService slicerDisplayService;

	@Mock
	private MicroDicomDisplayService microDicomDisplayService;

	@Mock
	private ConnectorQueryService connectorQueryService;

	@InjectMocks
	private DisplayServiceImpl displayService;

	@Mock
	private Authentication authentication;

	private ViewerSelectionEntity viewerSelectionWeasis;
	private ViewerSelectionEntity viewerSelectionOhif;
	private ViewerSelectionEntity viewerSelectionSlicer;
	private ViewerSelectionEntity viewerSelectionMicroDicom;

	@BeforeEach
	void setUp() {
		viewerSelectionWeasis = new ViewerSelectionEntity();
		viewerSelectionWeasis.setViewer(ViewerType.WEASIS);

		viewerSelectionOhif = new ViewerSelectionEntity();
		viewerSelectionOhif.setViewer(ViewerType.OHIF);

		viewerSelectionSlicer = new ViewerSelectionEntity();
		viewerSelectionSlicer.setViewer(ViewerType.SLICER);

		viewerSelectionMicroDicom = new ViewerSelectionEntity();
		viewerSelectionMicroDicom.setViewer(ViewerType.MICRODICOM);
	}

	// ========== Tests for viewerLaunchUrl with ArchiveSearchCriteria ==========

	@Test
	void when_viewerLaunchUrl_with_archiveSearchCriteria_and_weasisViewer_should_notRetrievePatients() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.WEASIS);

		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionWeasis);
		when(weasisDisplayService.retrieveWeasisLaunchUrl(any(), any(), any()))
				.thenReturn("weasis://test-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("weasis://test-url", result);
		verify(connectorQueryService, never()).retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any());
		verify(weasisDisplayService).retrieveWeasisLaunchUrl(eq(searchCriteria), isNull(), eq(authentication));
	}

	@Test
	void when_viewerLaunchUrl_with_archiveSearchCriteria_and_ohifViewer_should_retrievePatients() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.OHIF);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionOhif);
		when(ohifDisplayService.retrieveOhifLaunchUrl(any(), any(), any()))
				.thenReturn("http://ohif-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("http://ohif-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithoutIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(ohifDisplayService).retrieveOhifLaunchUrl(eq(searchCriteria), eq(patientsByArchive), eq(authentication));
	}

	@Test
	void when_viewerLaunchUrl_with_archiveSearchCriteria_and_slicerViewer_should_retrievePatients() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.SLICER);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionSlicer);
		when(slicerDisplayService.retrieveSlicerLaunchUrl(any(), any(), any()))
				.thenReturn("http://slicer-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("http://slicer-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithoutIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(slicerDisplayService).retrieveSlicerLaunchUrl(eq(searchCriteria), eq(patientsByArchive), eq(authentication));
	}

	@Test
	void when_viewerLaunchUrl_with_archiveSearchCriteria_and_microDicomViewer_should_retrievePatients() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.MICRODICOM);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionMicroDicom);
		when(microDicomDisplayService.retrieveMicroDicomLaunchUrl(any(), any()))
				.thenReturn("microdicom://test-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("microdicom://test-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithoutIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(microDicomDisplayService).retrieveMicroDicomLaunchUrl(eq(searchCriteria), eq(patientsByArchive));
	}

	// ========== Tests for viewerLaunchUrl with IHESearchCriteria ==========

	@Test
	void when_viewerLaunchUrl_with_iheSearchCriteria_and_weasisViewer_should_notRetrievePatients() {
		// Given
		IHESearchCriteria searchCriteria = new IHESearchCriteria();
		searchCriteria.setViewer(ViewerType.WEASIS);

		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionWeasis);
		when(weasisDisplayService.retrieveWeasisLaunchUrl(any(), any(), any()))
				.thenReturn("weasis://test-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("weasis://test-url", result);
		verify(connectorQueryService, never()).retrievePatientsByArchiveWithIHESearchCriteria(any(), any());
		verify(weasisDisplayService).retrieveWeasisLaunchUrl(eq(searchCriteria), isNull(), eq(authentication));
	}

	@Test
	void when_viewerLaunchUrl_with_iheSearchCriteria_and_ohifViewer_should_retrievePatients() {
		// Given
		IHESearchCriteria searchCriteria = new IHESearchCriteria();
		searchCriteria.setViewer(ViewerType.OHIF);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionOhif);
		when(ohifDisplayService.retrieveOhifLaunchUrl(any(), any(), any()))
				.thenReturn("http://ohif-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("http://ohif-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(ohifDisplayService).retrieveOhifLaunchUrl(eq(searchCriteria), eq(patientsByArchive), eq(authentication));
	}

	@Test
	void when_viewerLaunchUrl_with_iheSearchCriteria_and_slicerViewer_should_retrievePatients() {
		// Given
		IHESearchCriteria searchCriteria = new IHESearchCriteria();
		searchCriteria.setViewer(ViewerType.SLICER);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionSlicer);
		when(slicerDisplayService.retrieveSlicerLaunchUrl(any(), any(), any()))
				.thenReturn("http://slicer-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("http://slicer-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(slicerDisplayService).retrieveSlicerLaunchUrl(eq(searchCriteria), eq(patientsByArchive), eq(authentication));
	}

	@Test
	void when_viewerLaunchUrl_with_iheSearchCriteria_and_microDicomViewer_should_retrievePatients() {
		// Given
		IHESearchCriteria searchCriteria = new IHESearchCriteria();
		searchCriteria.setViewer(ViewerType.MICRODICOM);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionMicroDicom);
		when(microDicomDisplayService.retrieveMicroDicomLaunchUrl(any(), any()))
				.thenReturn("microdicom://test-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("microdicom://test-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(microDicomDisplayService).retrieveMicroDicomLaunchUrl(eq(searchCriteria), eq(patientsByArchive));
	}

	// ========== Tests for null viewer ==========

	@Test
	void when_viewerLaunchUrl_with_nullViewer_should_throwParameterException() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.OHIF);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		ViewerSelectionEntity viewerSelectionNull = new ViewerSelectionEntity();
		viewerSelectionNull.setViewer(null);

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionNull);

		// When & Then
		ParameterException exception = assertThrows(ParameterException.class, () ->
				displayService.viewerLaunchUrl(searchCriteria, authentication));

		assertEquals("Invalid viewer", exception.getMessage());
	}

	// ========== Tests for viewer selection without specified viewer (null) ==========

	@Test
	void when_viewerLaunchUrl_with_noViewerSpecified_should_retrievePatientsAndSelectViewer() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		// No viewer specified (null)

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();
		patientsByArchive.put("archive1", new HashSet<>());

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionOhif);
		when(ohifDisplayService.retrieveOhifLaunchUrl(any(), any(), any()))
				.thenReturn("http://ohif-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("http://ohif-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithoutIHESearchCriteria(eq(searchCriteria), eq(authentication));
		verify(viewerSelectionService).retrieveViewerSelectionRule(eq(searchCriteria), eq(patientsByArchive));
		verify(ohifDisplayService).retrieveOhifLaunchUrl(eq(searchCriteria), eq(patientsByArchive), eq(authentication));
	}

	// ========== Tests with empty patients map ==========

	@Test
	void when_viewerLaunchUrl_with_emptyPatientsMap_should_stillCallDisplayService() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.OHIF);

		Map<String, Set<Patient>> emptyPatientsByArchive = new HashMap<>();

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(emptyPatientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionOhif);
		when(ohifDisplayService.retrieveOhifLaunchUrl(any(), any(), any()))
				.thenReturn("http://ohif-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, authentication);

		// Then
		assertNotNull(result);
		assertEquals("http://ohif-url", result);
		verify(ohifDisplayService).retrieveOhifLaunchUrl(eq(searchCriteria), eq(emptyPatientsByArchive), eq(authentication));
	}

	// ========== Tests with null authentication ==========

	@Test
	void when_viewerLaunchUrl_with_nullAuthentication_should_workCorrectly() {
		// Given
		ArchiveSearchCriteria searchCriteria = new ArchiveSearchCriteria();
		searchCriteria.setViewer(ViewerType.OHIF);

		Map<String, Set<Patient>> patientsByArchive = new HashMap<>();

		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any()))
				.thenReturn(patientsByArchive);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any()))
				.thenReturn(viewerSelectionOhif);
		when(ohifDisplayService.retrieveOhifLaunchUrl(any(), any(), any()))
				.thenReturn("http://ohif-url");

		// When
		String result = displayService.viewerLaunchUrl(searchCriteria, null);

		// Then
		assertNotNull(result);
		assertEquals("http://ohif-url", result);
		verify(connectorQueryService).retrievePatientsByArchiveWithoutIHESearchCriteria(eq(searchCriteria), isNull());
		verify(ohifDisplayService).retrieveOhifLaunchUrl(eq(searchCriteria), eq(patientsByArchive), isNull());
	}

	// ========== Tests for all viewer types with ArchiveSearchCriteria ==========

	@Test
	void when_viewerLaunchUrl_with_allViewerTypes_should_callCorrectDisplayService() {
		// Test WEASIS
		ArchiveSearchCriteria searchCriteriaWeasis = new ArchiveSearchCriteria();
		searchCriteriaWeasis.setViewer(ViewerType.WEASIS);
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any())).thenReturn(viewerSelectionWeasis);
		when(weasisDisplayService.retrieveWeasisLaunchUrl(any(), any(), any())).thenReturn("weasis://url");
		displayService.viewerLaunchUrl(searchCriteriaWeasis, authentication);
		verify(weasisDisplayService).retrieveWeasisLaunchUrl(any(), any(), any());

		// Reset mocks
		Mockito.reset(viewerSelectionService, weasisDisplayService, ohifDisplayService, slicerDisplayService, microDicomDisplayService, connectorQueryService);

		// Test OHIF
		ArchiveSearchCriteria searchCriteriaOhif = new ArchiveSearchCriteria();
		searchCriteriaOhif.setViewer(ViewerType.OHIF);
		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any())).thenReturn(new HashMap<>());
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any())).thenReturn(viewerSelectionOhif);
		when(ohifDisplayService.retrieveOhifLaunchUrl(any(), any(), any())).thenReturn("http://ohif");
		displayService.viewerLaunchUrl(searchCriteriaOhif, authentication);
		verify(ohifDisplayService).retrieveOhifLaunchUrl(any(), any(), any());

		// Reset mocks
		Mockito.reset(viewerSelectionService, weasisDisplayService, ohifDisplayService, slicerDisplayService, microDicomDisplayService, connectorQueryService);

		// Test SLICER
		ArchiveSearchCriteria searchCriteriaSlicer = new ArchiveSearchCriteria();
		searchCriteriaSlicer.setViewer(ViewerType.SLICER);
		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any())).thenReturn(new HashMap<>());
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any())).thenReturn(viewerSelectionSlicer);
		when(slicerDisplayService.retrieveSlicerLaunchUrl(any(), any(), any())).thenReturn("http://slicer");
		displayService.viewerLaunchUrl(searchCriteriaSlicer, authentication);
		verify(slicerDisplayService).retrieveSlicerLaunchUrl(any(), any(), any());

		// Reset mocks
		Mockito.reset(viewerSelectionService, weasisDisplayService, ohifDisplayService, slicerDisplayService, microDicomDisplayService, connectorQueryService);

		// Test MICRODICOM
		ArchiveSearchCriteria searchCriteriaMicroDicom = new ArchiveSearchCriteria();
		searchCriteriaMicroDicom.setViewer(ViewerType.MICRODICOM);
		when(connectorQueryService.retrievePatientsByArchiveWithoutIHESearchCriteria(any(), any())).thenReturn(new HashMap<>());
		when(viewerSelectionService.retrieveViewerSelectionRule(any(), any())).thenReturn(viewerSelectionMicroDicom);
		when(microDicomDisplayService.retrieveMicroDicomLaunchUrl(any(), any())).thenReturn("microdicom://url");
		displayService.viewerLaunchUrl(searchCriteriaMicroDicom, authentication);
		verify(microDicomDisplayService).retrieveMicroDicomLaunchUrl(any(), any());
	}
}

