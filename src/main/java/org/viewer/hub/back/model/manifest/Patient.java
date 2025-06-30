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

package org.viewer.hub.back.model.manifest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({ "patientID", "issuerOfPatientID", "patientName", "patientBirthDate", "patientBirthTime",
		"patientSex", "studies" })
public class Patient implements Serializable {

	@Serial
	private static final long serialVersionUID = 7845868523287527235L;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("Study")
	private Set<Study> studies = new HashSet<>();

	@JacksonXmlProperty(isAttribute = true, localName = "PatientID")
	private String patientID;

	@JacksonXmlProperty(isAttribute = true, localName = "PatientName")
	private String patientName;

	@JacksonXmlProperty(isAttribute = true, localName = "IssuerOfPatientID")
	private String issuerOfPatientID;

	@JacksonXmlProperty(isAttribute = true, localName = "PatientBirthDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
	private LocalDate patientBirthDate;

	@JacksonXmlProperty(isAttribute = true, localName = "PatientBirthTime")
	private LocalTime patientBirthTime;

	@JacksonXmlProperty(isAttribute = true, localName = "PatientSex")
	private DicomPatientSex patientSex;

	public Patient() {
		this.studies = new HashSet<>();
	}

	public Patient(String patientID, String patientName, LocalDate patientBirthDate, DicomPatientSex patientSex) {
		this.patientID = patientID;
		this.patientName = patientName;
		this.patientBirthDate = patientBirthDate;
		this.patientSex = patientSex;
	}

	public Patient(String patientID, String patientName, String issuerOfPatientID, LocalDate patientBirthDate,
			LocalTime patientBirthTime, DicomPatientSex patientSex) {
		this.patientID = patientID;
		this.patientName = patientName;
		this.issuerOfPatientID = issuerOfPatientID;
		this.patientBirthDate = patientBirthDate;
		this.patientBirthTime = patientBirthTime;
		this.patientSex = patientSex;
	}

	/**
	 * Merge the current patient studies with the studies of the patient in parameter
	 * @param patientToMerge Patient to merge
	 */
	public void merge(Patient patientToMerge) {
		// Retrieve studies to merge that are already in the current patient
		Set<Study> studiesAlreadyInPatient = patientToMerge.getStudies()
			.stream()
			.filter(s -> this.studies.stream()
				.anyMatch(study -> Objects.equals(s.getStudyInstanceUID(), study.getStudyInstanceUID())))
			.collect(Collectors.toSet());
		// Retrieve studies to merge that are not already in the current patient
		Set<Study> studiesNotAlreadyInPatient = patientToMerge.getStudies()
			.stream()
			.filter(s -> this.studies.stream()
				.noneMatch(study -> Objects.equals(s.getStudyInstanceUID(), study.getStudyInstanceUID())))
			.collect(Collectors.toSet());

		// Studies not in current patient: add directly the studies
		this.studies.addAll(studiesNotAlreadyInPatient);

		// Studies in current patient: retrieve the studies of the current patient
		// and merge them with the studies to merge
		studiesAlreadyInPatient.forEach(studyAlreadyInPatient -> {
			Optional<Study> optionalStudy = this.studies.stream()
				.filter(s -> Objects.equals(s.getStudyInstanceUID(), studyAlreadyInPatient.getStudyInstanceUID()))
				.findFirst();
			if (optionalStudy.isPresent()) {
				Study study = optionalStudy.get();
				study.merge(studyAlreadyInPatient);
			}
		});
	}

}
