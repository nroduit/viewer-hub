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

package org.viewer.hub.back.model.searchcriteria;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.validator.ExistingConnector;
import org.viewer.hub.back.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.weasis.core.util.StringUtil.deAccent;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.DEDUCTION,
		defaultImpl= WeasisArchiveSearchCriteria.class
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = ArchiveSearchCriteria.class),
		@JsonSubTypes.Type(value = IHESearchCriteria.class),
		@JsonSubTypes.Type(value = WeasisIHESearchCriteria.class),
		@JsonSubTypes.Type(value = WeasisArchiveSearchCriteria.class)
})
@Getter
@ExistingConnector
@ToString
@EqualsAndHashCode
public abstract class SearchCriteria implements Serializable {

	@Serial
	private static final long serialVersionUID = 3062479886665643364L;

	@Setter
	@Schema(description = "Provide user context for the request. Used to retrieve specific properties depending on user/user group",
			name = "user", type = "String", example = "abcd")
	private String user;

	@Setter
	@Schema(description = "Provide host context for the request. Used to retrieve specific properties depending on host/host group",
			name = "host", type = "String", example = "pc-1234")
	private String host;

	@Setter
	@Schema(description = "Define the client making the request", name = "client", type = "String", example = "compacs")
	private String client;

	@Schema(description = "Request should be done by using these archives in parameter", name = "archive",
			type = "LinkedHashSet<String>", example = "vnaDb, vnaDicom, pacsDcm4chee")
	private LinkedHashSet<String> archive = new LinkedHashSet<>();

	@Setter
	@Schema(description = "Request should be done by using this viewer", name = "viewer",
			type = "ViewerType", example = "WEASIS")
	private ViewerType viewer;


	// Patient request filters
	@Setter
	@Schema(description = "Filter the results depending on StudyDateTime (min)", name = "lowerDateTime",
			type = "LocalDateTime", example = "2024-07-19T10:15:30")
	private LocalDateTime lowerDateTime;

	@Setter
	@Schema(description = "Filter the results depending on StudyDateTime (max)", name = "upperDateTime",
			type = "LocalDateTime", example = "2024-07-19T10:15:30")
	private LocalDateTime upperDateTime;

	@Setter
	@Schema(description = "Provide the most recent studies (compared by StudyDateTime) and limit the number of results by this parameter",
			name = "mostRecentResults", type = "Integer", example = "5")
	private Integer mostRecentResults;

	@Schema(description = "Filter the result of the request depending on the modalities in the study",
			name = "modalitiesInStudy", type = "Set<String>", example = "XC, CT")
	private Set<String> modalitiesInStudy = new HashSet<>();

	@Schema(description = "Filter the result of the request depending of the content of the Study description",
			name = "containsInDescription", type = "Set<String>", example = "abc, def")
	private Set<String> containsInDescription = new HashSet<>();

	public void setModalitiesInStudy(Set<String> modalitiesInStudy) {
		this.modalitiesInStudy = StringUtil.splitCommaSeparatedValuesToList(modalitiesInStudy);
	}

	public void setContainsInDescription(Set<String> containsInDescription) {
		this.containsInDescription = StringUtil.splitCommaSeparatedValuesToList(containsInDescription)
			.stream()
			.map(d -> deAccent(d).toLowerCase())
			.collect(Collectors.toSet());
	}

	public void setArchive(LinkedHashSet<String> archive) {
		this.archive = (LinkedHashSet<String>) StringUtil.splitCommaSeparatedValuesToList(archive);
	}

	public Set<Patient> applyPatientRequestSearchCriteriaFilters(Set<Patient> patientsToFilter) {
		// modalitiesInStudy
		this.applyPatientRequestFilterModalitiesInStudy(patientsToFilter);

		// containsInDescription
		this.applyPatientRequestFilterContainsInDescription(patientsToFilter);

		// lowerDateTime
		this.applyPatientRequestFilterLowerDateTime(patientsToFilter);

		// upperDateTime
		this.applyPatientRequestFilterUpperDateTime(patientsToFilter);

		// mostRecentResults
		this.applyPatientRequestFilterMostRecentResults(patientsToFilter);

		// clean patients without studies
		return patientsToFilter.stream().filter(patient -> !patient.getStudies().isEmpty()).collect(Collectors.toSet());
	}

	private void applyPatientRequestFilterMostRecentResults(Set<Patient> patientsToFilter) {
		if (this.mostRecentResults != null) {
			patientsToFilter.stream()
				.filter(patient -> patient.getStudies().size() > this.mostRecentResults)
				.forEach(patient -> patient.setStudies(patient.getStudies()
					.stream()
					.filter(study -> study.getStudyDateTime() != null)
					.sorted((s1, s2) -> s2.getStudyDateTime().compareTo(s1.getStudyDateTime()))
					.limit(this.mostRecentResults)
					.collect(Collectors.toSet())));
		}
	}

	private void applyPatientRequestFilterUpperDateTime(Set<Patient> patientsToFilter) {
		if (this.upperDateTime != null) {
			patientsToFilter.forEach(patient -> patient.setStudies(patient.getStudies()
				.stream()
				.filter(study -> study.getStudyDateTime() != null)
				.filter(study -> study.getStudyDateTime().isBefore(this.upperDateTime))
				.collect(Collectors.toSet())));
		}
	}

	private void applyPatientRequestFilterLowerDateTime(Set<Patient> patientsToFilter) {
		if (this.lowerDateTime != null) {
			patientsToFilter.forEach(patient -> patient.setStudies(patient.getStudies()
				.stream()
				.filter(study -> study.getStudyDateTime() != null)
				.filter(study -> study.getStudyDateTime().isAfter(this.lowerDateTime))
				.collect(Collectors.toSet())));
		}
	}

	private void applyPatientRequestFilterContainsInDescription(Set<Patient> patientsToFilter) {
		if (!this.containsInDescription.isEmpty()) {
			patientsToFilter.forEach(patient -> patient.setStudies(patient.getStudies()
				.stream()
				.filter(study -> Objects.nonNull(study.getStudyDescription()) && !study.getStudyDescription().isBlank())
				.filter(study -> this.containsInDescription.stream()
					.anyMatch(description -> deAccent(study.getStudyDescription()).toLowerCase().contains(description)))
				.collect(Collectors.toSet())));
		}
	}

	private void applyPatientRequestFilterModalitiesInStudy(Set<Patient> patientsToFilter) {
		if (!this.modalitiesInStudy.isEmpty()) {
			patientsToFilter.forEach(patient -> patient.setStudies(patient.getStudies()
				.stream()
				.filter(study -> study.getSeries()
					.stream()
					.anyMatch(serie -> this.modalitiesInStudy.contains(serie.getModality())))
				.collect(Collectors.toSet())));
		}
	}

	/**
	 * Jackson will deduct the object to build based on the criteria received in the request
	 * @param params Request params received
	 * @return built object can be one of the following: ArchiveSearchCriteria, IHESearchCriteria,
	 * WeasisIHESearchCriteria, WeasisArchiveSearchCriteria
	 */
	public static SearchCriteria jacksonDeduction(MultiValueMap<String, String> params) {
		// Jackson 3 mappers are immutable: features/modules are set through the builder.
		// configureForJackson2() keeps the Jackson 2 defaults - notably reading enums by name()
		// rather than toString() (ViewerType has a Lombok @ToString), so "WEASIS" still maps.
		ObjectMapper mapper = JsonMapper.builder()
			.configureForJackson2()
			.findAndAddModules()
			.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.build();
		return mapper.convertValue(params, SearchCriteria.class);
	}

}
