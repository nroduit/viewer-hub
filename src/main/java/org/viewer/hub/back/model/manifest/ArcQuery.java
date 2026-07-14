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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Validated
@JsonPropertyOrder({ "httpTags", "patients" })
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
public class ArcQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = -5561644584316122648L;

	@JsonProperty("Patient")
	private Set<Patient> patients;

	@JsonProperty("httpTag")
	private Set<HttpTag> httpTags;

	private Set<Message> messages;

	private String arcId;

	@Builder.Default
	private String baseUrl = "";

	private String webLogin;

	private boolean requireOnlySOPInstanceUID;

	private String additionnalParameters;

	@Getter(AccessLevel.NONE)
	private String overrideDicomTagsList;

	@JsonIgnore
	private Set<String> overrideDicomTags;

	private ConnectorType queryMode;

	public ArcQuery() {
		this.baseUrl = "";
		this.patients = new HashSet<>();
		this.httpTags = new HashSet<>();
		this.messages = new HashSet<>();
	}

	public String getOverrideDicomTagsList() {
		return this.overrideDicomTags != null && !this.overrideDicomTags.isEmpty()
				? String.join(StringUtil.COMMA, this.overrideDicomTags) : null;
	}

	/**
	 * Check if the arc query contains patient id in parameter
	 * @param patientId Patient Id to evaluate
	 * @return true if the arc query contains the patient id
	 */
	public boolean containsPatient(String patientId) {
		return this.patients.stream()
			.anyMatch(patient -> patient != null && Objects.equals(patient.getPatientID(), patientId));
	}

	/**
	 * Retrieve in the arc query the patient corresponding to the patient id in parameter
	 * @param patientId Patient Id to evaluate
	 * @return Patient found
	 */
	public Patient retrievePatientFromPatientId(String patientId) {
		return this.patients.stream()
			.filter(patient -> patient != null && Objects.equals(patient.getPatientID(), patientId))
			.findFirst()
			.orElse(null);
	}

}
