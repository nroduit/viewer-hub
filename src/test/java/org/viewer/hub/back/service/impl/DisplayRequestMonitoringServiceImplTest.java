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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringFilter;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringRecord;
import org.viewer.hub.back.repository.DisplayRequestMonitoringRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisplayRequestMonitoringServiceImplTest {

	@Mock
	private DisplayRequestMonitoringRepository displayRequestMonitoringRepository;

	@InjectMocks
	private DisplayRequestMonitoringServiceImpl displayRequestMonitoringService;

	@Test
	void when_record_should_saveRequestWithTruncatedValues() {
		DisplayRequestMonitoringRecord request = DisplayRequestMonitoringRecord.builder()
			.viewer("OHIF")
			.jwtUser("jdoe")
			.authenticated(true)
			.client("x".repeat(500))
			.durationMs(12)
			.success(true)
			.build();

		this.displayRequestMonitoringService.registerRequest(request);
		this.displayRequestMonitoringService.flush();

		ArgumentCaptor<List<DisplayRequestMonitoringEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(this.displayRequestMonitoringRepository).saveAll(captor.capture());
		DisplayRequestMonitoringEntity saved = captor.getValue().get(0);
		assertEquals("OHIF", saved.getViewer());
		assertEquals("jdoe", saved.getJwtUser());
		assertEquals(255, saved.getClient().length());
		assertEquals(12, saved.getDurationMs());
	}

	@Test
	void when_repositoryFails_flush_should_notThrow() {
		when(this.displayRequestMonitoringRepository.saveAll(any())).thenThrow(new IllegalStateException("db down"));

		this.displayRequestMonitoringService
			.registerRequest(DisplayRequestMonitoringRecord.builder().viewer("WEASIS").build());
		assertDoesNotThrow(() -> this.displayRequestMonitoringService.flush());
	}

	@Test
	@SuppressWarnings("unchecked")
	void when_exportActivityCsv_should_escapeFormulasAndQuotes() {
		DisplayRequestMonitoringEntity entity = new DisplayRequestMonitoringEntity();
		entity.setId(1L);
		entity.setCreatedAt(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
		entity.setViewer("OHIF");
		entity.setJwtUser("=cmd|' /C calc'!A0");
		entity.setPatientId("a\"b");
		Page<DisplayRequestMonitoringEntity> page = new PageImpl<>(List.of(entity));
		when(this.displayRequestMonitoringRepository.findAll(any(Specification.class), any(Pageable.class)))
			.thenReturn(page);

		String csv = new String(
				this.displayRequestMonitoringService.exportActivityCsv(new DisplayRequestMonitoringFilter()),
				StandardCharsets.UTF_8);

		assertTrue(csv.startsWith("\"id\",\"createdAt\""));
		assertTrue(csv.contains("\"'=cmd|' /C calc'!A0\""));
		assertTrue(csv.contains("\"a\"\"b\""));
	}

}