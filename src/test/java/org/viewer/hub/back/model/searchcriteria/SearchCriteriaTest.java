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

package org.viewer.hub.back.model.searchcriteria;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.enums.IHERequestType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Jackson deduction mechanism used in DisplayController.
 * <p>
 * Tests verify that the @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION) annotation
 * correctly deduces the SearchCriteria subtype based on the provided parameters.
 * </p>
 */
class SearchCriteriaTest {

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper().findAndRegisterModules()
			.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	}

	@Nested
	@DisplayName("Deduction to WeasisArchiveSearchCriteria")
	class WeasisArchiveSearchCriteriaDeductionTests {

		@Test
		@DisplayName("Should deduce WeasisArchiveSearchCriteria when 'pro' parameter is present")
		void shouldDeduceWeasisArchiveSearchCriteria_whenProParamPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient123");
			params.add("pro", "weasis.property=value");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			WeasisArchiveSearchCriteria weasisCriteria = (WeasisArchiveSearchCriteria) result;
			assertThat(weasisCriteria.getPro()).contains("weasis.property=value");
			assertThat(weasisCriteria.getPatientID()).contains("patient123");
			assertThat(weasisCriteria.getUser()).isEqualTo("testUser");
		}

		@Test
		@DisplayName("Should deduce WeasisArchiveSearchCriteria when 'config' parameter is present with archive params")
		void shouldDeduceWeasisArchiveSearchCriteria_whenConfigParamPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient456");
			params.add("config", "myConfig");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			WeasisArchiveSearchCriteria weasisCriteria = (WeasisArchiveSearchCriteria) result;
			assertThat(weasisCriteria.getConfig()).isEqualTo("myConfig");
			assertThat(weasisCriteria.getPatientID()).contains("patient456");
		}

		@Test
		@DisplayName("Should deduce WeasisArchiveSearchCriteria when 'arg' parameter is present")
		void shouldDeduceWeasisArchiveSearchCriteria_whenArgParamPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient789");
			params.add("arg", "someArgument");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			WeasisArchiveSearchCriteria weasisCriteria = (WeasisArchiveSearchCriteria) result;
			assertThat(weasisCriteria.getArg()).contains("someArgument");
		}

		@Test
		@DisplayName("Should deduce WeasisArchiveSearchCriteria when 'pro' and 'config' and 'arg' are all present")
		void shouldDeduceWeasisArchiveSearchCriteria_whenAllWeasisParamsPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient001");
			params.add("pro", "prop1=val1");
			params.add("config", "extConfig");
			params.add("arg", "arg1");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			WeasisArchiveSearchCriteria weasisCriteria = (WeasisArchiveSearchCriteria) result;
			assertThat(weasisCriteria.getPro()).contains("prop1=val1");
			assertThat(weasisCriteria.getConfig()).isEqualTo("extConfig");
			assertThat(weasisCriteria.getArg()).contains("arg1");
			assertThat(weasisCriteria.getPatientID()).contains("patient001");
		}

	}

	@Nested
	@DisplayName("Deduction to ArchiveSearchCriteria")
	class ArchiveSearchCriteriaDeductionTests {

		@Test
		@DisplayName("Should deduce ArchiveSearchCriteria when only patientID is present (no weasis-specific params)")
		void shouldDeduceArchiveSearchCriteria_whenOnlyPatientIDPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient123");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getPatientID()).contains("patient123");
			assertThat(archiveCriteria.getUser()).isEqualTo("testUser");
		}

		@Test
		@DisplayName("Should deduce ArchiveSearchCriteria when studyUID is present")
		void shouldDeduceArchiveSearchCriteria_whenStudyUIDPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("studyUID", "1.2.3.4.5");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getStudyUID()).contains("1.2.3.4.5");
		}

		@Test
		@DisplayName("Should deduce ArchiveSearchCriteria when accessionNumber is present")
		void shouldDeduceArchiveSearchCriteria_whenAccessionNumberPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("accessionNumber", "ACC001");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getAccessionNumber()).contains("ACC001");
		}

		@Test
		@DisplayName("Should deduce ArchiveSearchCriteria when seriesUID is present")
		void shouldDeduceArchiveSearchCriteria_whenSeriesUIDPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("seriesUID", "1.2.3.4.5.6");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getSeriesUID()).contains("1.2.3.4.5.6");
		}

		@Test
		@DisplayName("Should deduce ArchiveSearchCriteria when objectUID is present")
		void shouldDeduceArchiveSearchCriteria_whenObjectUIDPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("objectUID", "1.2.3.4.5.6.7");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getObjectUID()).contains("1.2.3.4.5.6.7");
		}

		@Test
		@DisplayName("Should deduce ArchiveSearchCriteria with multiple archive identifiers")
		void shouldDeduceArchiveSearchCriteria_withMultipleIdentifiers() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient1");
			params.add("studyUID", "1.2.3");
			params.add("accessionNumber", "ACC002");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getPatientID()).contains("patient1");
			assertThat(archiveCriteria.getStudyUID()).contains("1.2.3");
			assertThat(archiveCriteria.getAccessionNumber()).contains("ACC002");
		}

	}

	@Nested
	@DisplayName("Deduction to WeasisIHESearchCriteria")
	class WeasisIHESearchCriteriaDeductionTests {

		@Test
		@DisplayName("Should deduce WeasisIHESearchCriteria when IHE params + 'pro' are present")
		void shouldDeduceWeasisIHESearchCriteria_whenIHEParamsAndProPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "ihePatient");
			params.add("pro", "weasis.prop=value");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisIHESearchCriteria.class);
			WeasisIHESearchCriteria weasisIHECriteria = (WeasisIHESearchCriteria) result;
			assertThat(weasisIHECriteria.getPro()).contains("weasis.prop=value");
			assertThat(weasisIHECriteria.getRequestType()).isEqualTo(IHERequestType.PATIENT);
			assertThat(weasisIHECriteria.getPatientID()).isEqualTo("ihePatient");
		}

		@Test
		@DisplayName("Should deduce WeasisIHESearchCriteria when IHE params + 'config' are present")
		void shouldDeduceWeasisIHESearchCriteria_whenIHEParamsAndConfigPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.STUDY.getCode());
			params.add("studyUID", "1.2.3.4");
			params.add("config", "weasisConfig");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisIHESearchCriteria.class);
			WeasisIHESearchCriteria weasisIHECriteria = (WeasisIHESearchCriteria) result;
			assertThat(weasisIHECriteria.getConfig()).isEqualTo("weasisConfig");
			assertThat(weasisIHECriteria.getRequestType()).isEqualTo(IHERequestType.STUDY);
		}

		@Test
		@DisplayName("Should deduce WeasisIHESearchCriteria when IHE params + 'arg' are present")
		void shouldDeduceWeasisIHESearchCriteria_whenIHEParamsAndArgPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("arg", "someArg");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(WeasisIHESearchCriteria.class);
			WeasisIHESearchCriteria weasisIHECriteria = (WeasisIHESearchCriteria) result;
			assertThat(weasisIHECriteria.getArg()).contains("someArg");
		}

	}

	@Nested
	@DisplayName("Deduction to IHESearchCriteria")
	class IHESearchCriteriaDeductionTests {

		@Test
		@DisplayName("Should deduce IHESearchCriteria when requestType is present without weasis params")
		void shouldDeduceIHESearchCriteria_whenRequestTypePresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "ihePatient");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			IHESearchCriteria iheCriteria = (IHESearchCriteria) result;
			assertThat(iheCriteria.getRequestType()).isEqualTo(IHERequestType.PATIENT);
			assertThat(iheCriteria.getPatientID()).isEqualTo("ihePatient");
			assertThat(iheCriteria.getUser()).isEqualTo("testUser");
		}

		@Test
		@DisplayName("Should deduce IHESearchCriteria when requestType STUDY with studyUID")
		void shouldDeduceIHESearchCriteria_whenRequestTypeStudy() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.STUDY.getCode());
			params.add("studyUID", "1.2.3.4.5");
			params.add("user", "testUser");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			IHESearchCriteria iheCriteria = (IHESearchCriteria) result;
			assertThat(iheCriteria.getRequestType()).isEqualTo(IHERequestType.STUDY);
			assertThat(iheCriteria.getStudyUID()).contains("1.2.3.4.5");
		}

		@Test
		@DisplayName("Should deduce IHESearchCriteria when diagnosticQuality is present")
		void shouldDeduceIHESearchCriteria_whenDiagnosticQualityPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("diagnosticQuality", "true");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			IHESearchCriteria iheCriteria = (IHESearchCriteria) result;
			assertThat(iheCriteria.getDiagnosticQuality()).isTrue();
		}

		@Test
		@DisplayName("Should deduce IHESearchCriteria when keyImagesOnly is present")
		void shouldDeduceIHESearchCriteria_whenKeyImagesOnlyPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("keyImagesOnly", "true");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			IHESearchCriteria iheCriteria = (IHESearchCriteria) result;
			assertThat(iheCriteria.getKeyImagesOnly()).isTrue();
		}

		@Test
		@DisplayName("Should deduce IHESearchCriteria with patientName")
		void shouldDeduceIHESearchCriteria_whenPatientNamePresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("patientName", "Doe^John");

			// When
			SearchCriteria result = objectMapper.convertValue(params, IHESearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			IHESearchCriteria iheCriteria = (IHESearchCriteria) result;
			assertThat(iheCriteria.getPatientName()).isEqualTo("Doe^John");
		}

	}

	@Nested
	@DisplayName("Default implementation fallback")
	class DefaultImplFallbackTests {

		@Test
		@DisplayName("Should fallback to ArchiveSearchCriteria (defaultImpl) when only common params are present")
		void shouldFallbackToDefaultImpl_whenOnlyCommonParamsPresent() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("user", "testUser");
			params.add("host", "testHost");

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			assertThat(result.getUser()).isEqualTo("testUser");
			assertThat(result.getHost()).isEqualTo("testHost");
		}

		@Test
		@DisplayName("Should fallback to ArchiveSearchCriteria (defaultImpl) when empty params")
		void shouldFallbackToDefaultImpl_whenEmptyParams() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

			// When
			SearchCriteria result = objectMapper.convertValue(params, ArchiveSearchCriteria.class);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
		}

	}

	@Nested
	@DisplayName("jacksonDeduction static method")
	class JacksonDeductionMethodTests {

		@Test
		@DisplayName("Should correctly deduce via jacksonDeduction method - WeasisArchiveSearchCriteria")
		void shouldDeduceViaStaticMethod_weasisArchive() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient123");
			params.add("pro", "weasis.property=value");
			params.add("config", "extCfg");
			params.add("user", "testUser");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			WeasisArchiveSearchCriteria weasisCriteria = (WeasisArchiveSearchCriteria) result;
			assertThat(weasisCriteria.getPro()).contains("weasis.property=value");
			assertThat(weasisCriteria.getConfig()).isEqualTo("extCfg");
			assertThat(weasisCriteria.getPatientID()).contains("patient123");
		}

		@Test
		@DisplayName("Should correctly deduce via jacksonDeduction method - IHESearchCriteria")
		void shouldDeduceViaStaticMethod_ihe() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "ihePatient");
			params.add("user", "testUser");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			IHESearchCriteria iheCriteria = (IHESearchCriteria) result;
			assertThat(iheCriteria.getRequestType()).isEqualTo(IHERequestType.PATIENT);
			assertThat(iheCriteria.getPatientID()).isEqualTo("ihePatient");
		}

		@Test
		@DisplayName("Should correctly deduce via jacksonDeduction method - WeasisIHESearchCriteria")
		void shouldDeduceViaStaticMethod_weasisIHE() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("pro", "weasis.prop=val");
			params.add("config", "cfg");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then
			assertThat(result).isInstanceOf(WeasisIHESearchCriteria.class);
			WeasisIHESearchCriteria weasisIHECriteria = (WeasisIHESearchCriteria) result;
			assertThat(weasisIHECriteria.getPro()).contains("weasis.prop=val");
			assertThat(weasisIHECriteria.getConfig()).isEqualTo("cfg");
			assertThat(weasisIHECriteria.getRequestType()).isEqualTo(IHERequestType.PATIENT);
		}

		@Test
		@DisplayName("Should correctly deduce via jacksonDeduction method - ArchiveSearchCriteria (no weasis params)")
		void shouldDeduceViaStaticMethod_archive() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patientArchive");
			params.add("user", "testUser");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
			ArchiveSearchCriteria archiveCriteria = (ArchiveSearchCriteria) result;
			assertThat(archiveCriteria.getPatientID()).contains("patientArchive");
		}

	}

	@Nested
	@DisplayName("DisplayController class type validation simulation")
	class DisplayControllerClassTypeValidationTests {

		@Test
		@DisplayName("ArchiveSearchCriteria endpoint: should accept ArchiveSearchCriteria subtype")
		void archiveEndpoint_shouldAcceptArchiveSearchCriteria() {
			// Given - simulating GET /display with archive params
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient123");
			params.add("user", "testUser");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then - the controller expects
			// ArchiveSearchCriteria.class.isInstance(result)
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
		}

		@Test
		@DisplayName("ArchiveSearchCriteria endpoint: should accept WeasisArchiveSearchCriteria (extends ArchiveSearchCriteria)")
		void archiveEndpoint_shouldAcceptWeasisArchiveSearchCriteria() {
			// Given - simulating GET /display with weasis archive params
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient123");
			params.add("pro", "prop=val");
			params.add("config", "cfg");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then - WeasisArchiveSearchCriteria extends ArchiveSearchCriteria, so
			// isInstance should be true
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			assertThat(result).isInstanceOf(ArchiveSearchCriteria.class);
		}

		@Test
		@DisplayName("ArchiveSearchCriteria endpoint: should reject IHESearchCriteria")
		void archiveEndpoint_shouldRejectIHESearchCriteria() {
			// Given - simulating GET /display with IHE params (wrong endpoint)
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient123");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then - IHESearchCriteria is not an instance of ArchiveSearchCriteria
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			assertThat(result).isNotInstanceOf(ArchiveSearchCriteria.class);
		}

		@Test
		@DisplayName("IHESearchCriteria endpoint: should accept IHESearchCriteria subtype")
		void iheEndpoint_shouldAcceptIHESearchCriteria() {
			// Given - simulating GET /display/IHEInvokeImageDisplay with IHE params
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "ihePatient");
			params.add("user", "testUser");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then - the controller expects IHESearchCriteria.class.isInstance(result)
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
		}

		@Test
		@DisplayName("IHESearchCriteria endpoint: should accept WeasisIHESearchCriteria (extends IHESearchCriteria)")
		void iheEndpoint_shouldAcceptWeasisIHESearchCriteria() {
			// Given - simulating GET /display/IHEInvokeImageDisplay with weasis IHE
			// params
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("pro", "prop=val");
			params.add("config", "cfg");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then - WeasisIHESearchCriteria extends IHESearchCriteria, so isInstance
			// should be true
			assertThat(result).isInstanceOf(WeasisIHESearchCriteria.class);
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
		}

		@Test
		@DisplayName("IHESearchCriteria endpoint: should reject ArchiveSearchCriteria")
		void iheEndpoint_shouldRejectArchiveSearchCriteria() {
			// Given - simulating GET /display/IHEInvokeImageDisplay with archive params
			// (wrong endpoint)
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient123");
			params.add("user", "testUser");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then - ArchiveSearchCriteria is not an instance of IHESearchCriteria
			assertThat(result).isNotInstanceOf(IHESearchCriteria.class);
		}

	}

	@Nested
	@DisplayName("Common fields preservation")
	class CommonFieldsPreservationTests {

		@Test
		@DisplayName("Should preserve common fields in deduced WeasisArchiveSearchCriteria")
		void shouldPreserveCommonFields_weasisArchive() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("patientID", "patient1");
			params.add("user", "userValue");
			params.add("host", "hostValue");
			params.add("client", "clientValue");
			params.add("archive", "archive1");
			params.add("pro", "prop=val");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then
			assertThat(result).isInstanceOf(WeasisArchiveSearchCriteria.class);
			assertThat(result.getUser()).isEqualTo("userValue");
			assertThat(result.getHost()).isEqualTo("hostValue");
			assertThat(result.getClient()).isEqualTo("clientValue");
			assertThat(result.getArchive()).contains("archive1");
		}

		@Test
		@DisplayName("Should preserve common fields in deduced IHESearchCriteria")
		void shouldPreserveCommonFields_ihe() {
			// Given
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("requestType", IHERequestType.PATIENT.getCode());
			params.add("patientID", "patient1");
			params.add("user", "userValue");
			params.add("host", "hostValue");
			params.add("client", "clientValue");
			params.add("archive", "archive1");

			// When
			SearchCriteria result = SearchCriteria.jacksonDeduction(params);

			// Then
			assertThat(result).isInstanceOf(IHESearchCriteria.class);
			assertThat(result.getUser()).isEqualTo("userValue");
			assertThat(result.getHost()).isEqualTo("hostValue");
			assertThat(result.getClient()).isEqualTo("clientValue");
			assertThat(result.getArchive()).contains("archive1");
		}

	}

}
