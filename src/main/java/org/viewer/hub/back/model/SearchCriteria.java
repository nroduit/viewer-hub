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

package org.viewer.hub.back.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.viewer.hub.back.model.manifest.Patient;
import org.viewer.hub.back.model.validator.ExistingConnector;
import org.viewer.hub.back.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.weasis.core.util.StringUtil.deAccent;

@ExistingConnector
@ToString
@EqualsAndHashCode
public abstract class SearchCriteria implements Serializable {

	@Serial
	private static final long serialVersionUID = 3062479886665643364L;

	@Schema(description = "Used to modify the properties of the launcher", name = "pro", type = "List<String>",
			example = "weasis.export.dicom true")
	private List<String> pro = new ArrayList<>();

	@Schema(description = "Provide user context for the request. Used to retrieve specific properties depending on user/user group",
			name = "user", type = "String", example = "abcd")
	private String user;

	@Schema(description = "Provide host context for the request. Used to retrieve specific properties depending on host/host group",
			name = "host", type = "String", example = "pc-1234")
	private String host;

	@Schema(description = "Define the client making the request", name = "client", type = "String", example = "compacs")
	private String client;

	@Schema(description = "[DEPRECATED][MARK_AS_REMOVAL] Define the context to use for the launcher. Will be replaced by 'config'",
			name = "extCfg", type = "String", example = "dicomizer")
	private String extCfg;

	@Schema(description = "Define the context to use for the launcher.", name = "config", type = "String",
			example = "dicomizer")
	private String config;

	@Schema(description = "Argument for the launcher", name = "arg", type = "List<String>",
			example = "$dicom:close –all")
	private List<String> arg = new ArrayList<>();

	@Schema(description = "Request should be done by using these archives in parameter", name = "archive",
			type = "LinkedHashSet<String>", example = "vnaDb, vnaDicom, pacsDcm4chee")
	private LinkedHashSet<String> archive = new LinkedHashSet<>();

	// Patient request filters
	@Schema(description = "Filter the results depending on StudyDateTime (min)", name = "lowerDateTime",
			type = "LocalDateTime", example = "2024-07-19T10:15:30")
	private LocalDateTime lowerDateTime;

	@Schema(description = "Filter the results depending on StudyDateTime (max)", name = "upperDateTime",
			type = "LocalDateTime", example = "2024-07-19T10:15:30")
	private LocalDateTime upperDateTime;

	@Schema(description = "Provide the most recent studies (compared by StudyDateTime) and limit the number of results by this parameter",
			name = "mostRecentResults", type = "Integer", example = "5")
	private Integer mostRecentResults;

	@Schema(description = "Filter the result of the request depending on the modalities in the study",
			name = "modalitiesInStudy", type = "Set<String>", example = "XC, CT")
	private Set<String> modalitiesInStudy = new HashSet<>();

	@Schema(description = "Filter the result of the request depending of the content of the Study description",
			name = "containsInDescription", type = "Set<String>", example = "abc, def")
	private Set<String> containsInDescription = new HashSet<>();

	public LocalDateTime getLowerDateTime() {
		return this.lowerDateTime;
	}

	public void setLowerDateTime(LocalDateTime lowerDateTime) {
		this.lowerDateTime = lowerDateTime;
	}

	public LocalDateTime getUpperDateTime() {
		return this.upperDateTime;
	}

	public void setUpperDateTime(LocalDateTime upperDateTime) {
		this.upperDateTime = upperDateTime;
	}

	public Integer getMostRecentResults() {
		return this.mostRecentResults;
	}

	public void setMostRecentResults(Integer mostRecentResults) {
		this.mostRecentResults = mostRecentResults;
	}

	public Set<String> getModalitiesInStudy() {
		return this.modalitiesInStudy;
	}

	public void setModalitiesInStudy(Set<String> modalitiesInStudy) {
		this.modalitiesInStudy = StringUtil.splitCommaSeparatedValuesToList(modalitiesInStudy);
	}

	public Set<String> getContainsInDescription() {
		return this.containsInDescription;
	}

	public void setContainsInDescription(Set<String> containsInDescription) {
		this.containsInDescription = StringUtil.splitCommaSeparatedValuesToList(containsInDescription)
			.stream()
			.map(d -> deAccent(d).toLowerCase())
			.collect(Collectors.toSet());
	}

	public LinkedHashSet<String> getArchive() {
		return this.archive;
	}

	public void setArchive(LinkedHashSet<String> archive) {
		this.archive = (LinkedHashSet<String>) StringUtil.splitCommaSeparatedValuesToList(archive);
	}

	public List<String> getArg() {
		return this.arg;
	}

	public void setArg(List<String> arg) {
		this.arg = arg;
	}

	@JsonGetter("ext-cfg")
	public String getExtCfg() {
		return this.extCfg;
	}

	@JsonSetter("ext-cfg")
	public void setExtCfg(String extCfg) {
		this.extCfg = extCfg;
	}

	public String getConfig() {
		return this.config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getClient() {
		return this.client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public List<String> getPro() {
		return this.pro;
	}

	public void setPro(List<String> pro) {
		this.pro = pro;
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

}
