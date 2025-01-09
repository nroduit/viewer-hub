package org.weasis.manager.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.weasis.manager.back.model.WeasisIHESearchCriteria;
import org.weasis.manager.back.model.WeasisSearchCriteria;

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
	@DisplayName("WeasisSearchCriteria: When encoding/decoding should retrieve original values")
	void given_weasisSearchCriteria_when_encodingDecoding_shouldRetrieveOriginalValues() {
		// Given
		WeasisSearchCriteria weasisSearchCriteria = new WeasisSearchCriteria();
		weasisSearchCriteria.setPatientID(Set.of("patientID"));
		weasisSearchCriteria.setAccessionNumber(Set.of("accessionNumber"));
		weasisSearchCriteria.setStudyUID(Set.of("studyUID"));
		weasisSearchCriteria.setSeriesUID(Set.of("seriesUID"));
		weasisSearchCriteria.setObjectUID(Set.of("objectUID"));

		// When
		this.cryptographyService.encode(weasisSearchCriteria);

		// Then
		assertThat(weasisSearchCriteria.getPatientID()).hasSize(1);
		assertThat(weasisSearchCriteria.getPatientID().stream().findFirst().orElse("patientID"))
			.isNotEqualTo("patientID");
		assertThat(weasisSearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(weasisSearchCriteria.getAccessionNumber().stream().findFirst().orElse("accessionNumber"))
			.isNotEqualTo("accessionNumber");
		assertThat(weasisSearchCriteria.getStudyUID()).hasSize(1);
		assertThat(weasisSearchCriteria.getStudyUID().stream().findFirst().orElse("studyUID")).isNotEqualTo("studyUID");
		assertThat(weasisSearchCriteria.getSeriesUID()).hasSize(1);
		assertThat(weasisSearchCriteria.getSeriesUID().stream().findFirst().orElse("seriesUID"))
			.isNotEqualTo("seriesUID");
		assertThat(weasisSearchCriteria.getObjectUID()).hasSize(1);
		assertThat(weasisSearchCriteria.getObjectUID().stream().findFirst().orElse("objectUID"))
			.isNotEqualTo("objectUID");

		// When
		this.cryptographyService.decode(weasisSearchCriteria);

		// Then
		assertThat(weasisSearchCriteria.getPatientID()).hasSize(1);
		assertThat(weasisSearchCriteria.getPatientID().stream().findFirst().orElse(null)).isEqualTo("patientID");
		assertThat(weasisSearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(weasisSearchCriteria.getAccessionNumber().stream().findFirst().orElse(null))
			.isEqualTo("accessionNumber");
		assertThat(weasisSearchCriteria.getStudyUID()).hasSize(1);
		assertThat(weasisSearchCriteria.getStudyUID().stream().findFirst().orElse(null)).isEqualTo("studyUID");
		assertThat(weasisSearchCriteria.getSeriesUID()).hasSize(1);
		assertThat(weasisSearchCriteria.getSeriesUID().stream().findFirst().orElse(null)).isEqualTo("seriesUID");
		assertThat(weasisSearchCriteria.getObjectUID()).hasSize(1);
		assertThat(weasisSearchCriteria.getObjectUID().stream().findFirst().orElse(null)).isEqualTo("objectUID");
	}

	@Test
	@DisplayName("WeasisIHESearchCriteria: When encoding/decoding should retrieve original values")
	void given_weasisIHESearchCriteria_when_encodingDecoding_shouldRetrieveOriginalValues() {
		// Given
		WeasisIHESearchCriteria weasisIHESearchCriteria = new WeasisIHESearchCriteria();
		weasisIHESearchCriteria.setPatientID("patientID");
		weasisIHESearchCriteria.setAccessionNumber(Set.of("accessionNumber"));
		weasisIHESearchCriteria.setStudyUID(Set.of("studyUID"));

		// When
		this.cryptographyService.encode(weasisIHESearchCriteria);

		// Then
		assertThat(weasisIHESearchCriteria.getPatientID()).isNotBlank();
		assertThat(weasisIHESearchCriteria.getPatientID()).isNotEqualTo("patientID");
		assertThat(weasisIHESearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(weasisIHESearchCriteria.getAccessionNumber().stream().findFirst().orElse("accessionNumber"))
			.isNotEqualTo("accessionNumber");
		assertThat(weasisIHESearchCriteria.getStudyUID()).hasSize(1);
		assertThat(weasisIHESearchCriteria.getStudyUID().stream().findFirst().orElse("studyUID"))
			.isNotEqualTo("studyUID");

		// When
		this.cryptographyService.decode(weasisIHESearchCriteria);

		// Then
		assertThat(weasisIHESearchCriteria.getPatientID()).isNotBlank();
		assertThat(weasisIHESearchCriteria.getPatientID()).isEqualTo("patientID");
		assertThat(weasisIHESearchCriteria.getAccessionNumber()).hasSize(1);
		assertThat(weasisIHESearchCriteria.getAccessionNumber().stream().findFirst().orElse(null))
			.isEqualTo("accessionNumber");
		assertThat(weasisIHESearchCriteria.getStudyUID()).hasSize(1);
		assertThat(weasisIHESearchCriteria.getStudyUID().stream().findFirst().orElse(null)).isEqualTo("studyUID");
	}

}
