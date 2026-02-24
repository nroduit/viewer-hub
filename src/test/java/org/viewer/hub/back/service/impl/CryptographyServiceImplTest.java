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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CryptographyServiceImplTest {

	private CryptographyServiceImpl cryptographyService;

	@BeforeEach
	void init() {
		// Init service
		this.cryptographyService = new CryptographyServiceImpl(true, "password", "5c0744940b5c369b");
	}

	@Test
	@DisplayName("Simple String: When encoding/decoding should retrieve original value")
	void when_encodingDecodingText_should_retrieveOriginalValue() {
		// Encode
		String toTestEncoded = this.cryptographyService.encode("test");
		assertThat(toTestEncoded).isNotEqualTo("test");

		// Decode
		String toTestDecoded = this.cryptographyService.decode(toTestEncoded);
		assertThat(toTestDecoded).isEqualTo("test");
	}

	@Test
	@DisplayName("ArchiveSearchCriteria: When encoding/decoding should retrieve original values")
	void given_archiveSearchCriteria_when_encodingDecoding_shouldRetrieveOriginalValues() {
		// Given
		ArchiveSearchCriteria archiveSearchCriteria = new ArchiveSearchCriteria();
		archiveSearchCriteria.setPatientID(Set.of("patientID"));
		archiveSearchCriteria.setAccessionNumber(Set.of("accessionNumber"));
		archiveSearchCriteria.setStudyUID(Set.of("studyUID"));
		archiveSearchCriteria.setSeriesUID(Set.of("seriesUID"));
		archiveSearchCriteria.setObjectUID(Set.of("objectUID"));

		// When
		this.cryptographyService.encode(archiveSearchCriteria);

		// Then
		assertThat(archiveSearchCriteria.getPatientID()).hasSize(1);
		assertThat(archiveSearchCriteria.getPatientID().stream().findFirst().orElse("patientID"))
			.isNotEqualTo("patientID");
		assertThat(archiveSearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(archiveSearchCriteria.getAccessionNumber().stream().findFirst().orElse("accessionNumber"))
			.isNotEqualTo("accessionNumber");
		assertThat(archiveSearchCriteria.getStudyUID()).hasSize(1);
		assertThat(archiveSearchCriteria.getStudyUID().stream().findFirst().orElse("studyUID")).isNotEqualTo("studyUID");
		assertThat(archiveSearchCriteria.getSeriesUID()).hasSize(1);
		assertThat(archiveSearchCriteria.getSeriesUID().stream().findFirst().orElse("seriesUID"))
			.isNotEqualTo("seriesUID");
		assertThat(archiveSearchCriteria.getObjectUID()).hasSize(1);
		assertThat(archiveSearchCriteria.getObjectUID().stream().findFirst().orElse("objectUID"))
			.isNotEqualTo("objectUID");

		// When
		this.cryptographyService.decode(archiveSearchCriteria);

		// Then
		assertThat(archiveSearchCriteria.getPatientID()).hasSize(1);
		assertThat(archiveSearchCriteria.getPatientID().stream().findFirst().orElse(null)).isEqualTo("patientID");
		assertThat(archiveSearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(archiveSearchCriteria.getAccessionNumber().stream().findFirst().orElse(null))
			.isEqualTo("accessionNumber");
		assertThat(archiveSearchCriteria.getStudyUID()).hasSize(1);
		assertThat(archiveSearchCriteria.getStudyUID().stream().findFirst().orElse(null)).isEqualTo("studyUID");
		assertThat(archiveSearchCriteria.getSeriesUID()).hasSize(1);
		assertThat(archiveSearchCriteria.getSeriesUID().stream().findFirst().orElse(null)).isEqualTo("seriesUID");
		assertThat(archiveSearchCriteria.getObjectUID()).hasSize(1);
		assertThat(archiveSearchCriteria.getObjectUID().stream().findFirst().orElse(null)).isEqualTo("objectUID");
	}

	@Test
	@DisplayName("IHESearchCriteria: When encoding/decoding should retrieve original values")
	void given_IHESearchCriteria_when_encodingDecoding_shouldRetrieveOriginalValues() {
		// Given
		IHESearchCriteria iheSearchCriteria = new IHESearchCriteria();
		iheSearchCriteria.setPatientID("patientID");
		iheSearchCriteria.setAccessionNumber(Set.of("accessionNumber"));
		iheSearchCriteria.setStudyUID(Set.of("studyUID"));

		// When
		this.cryptographyService.encode(iheSearchCriteria);

		// Then
		assertThat(iheSearchCriteria.getPatientID()).isNotBlank();
		assertThat(iheSearchCriteria.getPatientID()).isNotEqualTo("patientID");
		assertThat(iheSearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(iheSearchCriteria.getAccessionNumber().stream().findFirst().orElse("accessionNumber"))
			.isNotEqualTo("accessionNumber");
		assertThat(iheSearchCriteria.getStudyUID()).hasSize(1);
		assertThat(iheSearchCriteria.getStudyUID().stream().findFirst().orElse("studyUID"))
			.isNotEqualTo("studyUID");

		// When
		this.cryptographyService.decode(iheSearchCriteria);

		// Then
		assertThat(iheSearchCriteria.getPatientID()).isNotBlank();
		assertThat(iheSearchCriteria.getPatientID()).isEqualTo("patientID");
		assertThat(iheSearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(iheSearchCriteria.getAccessionNumber().stream().findFirst().orElse(null))
			.isEqualTo("accessionNumber");
		assertThat(iheSearchCriteria.getStudyUID()).hasSize(1);
		assertThat(iheSearchCriteria.getStudyUID().stream().findFirst().orElse(null)).isEqualTo("studyUID");
	}

}
