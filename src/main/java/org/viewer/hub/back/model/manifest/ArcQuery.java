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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Validated
@JsonPropertyOrder({ "httpTags", "patients" })
public class ArcQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = -5561644584316122648L;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("Patient")
	private Set<Patient> patients;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("httpTag")
	private Set<HttpTag> httpTags;

	@JacksonXmlElementWrapper(useWrapping = false)
	private Set<Message> messages;

	@JacksonXmlProperty(isAttribute = true, localName = "arcId")
	private String arcId;

	@JacksonXmlProperty(isAttribute = true, localName = "baseUrl")
	private String baseUrl = "";

	@JacksonXmlProperty(isAttribute = true, localName = "webLogin")
	private String webLogin;

	@JacksonXmlProperty(isAttribute = true, localName = "requireOnlySOPInstanceUID")
	private boolean requireOnlySOPInstanceUID;

	@JacksonXmlProperty(isAttribute = true, localName = "additionnalParameters")
	private String additionnalParameters;

	@JacksonXmlProperty(isAttribute = true, localName = "overrideDicomTagsList")
	private String overrideDicomTagsList;

	@JsonIgnore
	private Set<String> overrideDicomTags;

	public ArcQuery() {
		this.patients = new HashSet<>();
		this.httpTags = new HashSet<>();
		this.messages = new HashSet<>();
	}

	public String getArcId() {
		return this.arcId;
	}

	public void setArcId(String arcId) {
		this.arcId = arcId;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getWebLogin() {
		return this.webLogin;
	}

	public void setWebLogin(String webLogin) {
		this.webLogin = webLogin;
	}

	public boolean isRequireOnlySOPInstanceUID() {
		return this.requireOnlySOPInstanceUID;
	}

	public void setRequireOnlySOPInstanceUID(boolean requireOnlySOPInstanceUID) {
		this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
	}

	public String getAdditionnalParameters() {
		return this.additionnalParameters;
	}

	public void setAdditionnalParameters(String additionnalParameters) {
		this.additionnalParameters = additionnalParameters;
	}

	public Set<Patient> getPatients() {
		return this.patients;
	}

	public void setPatients(Set<Patient> patients) {
		this.patients = patients;
	}

	public Set<HttpTag> getHttpTags() {
		return this.httpTags;
	}

	public void setHttpTags(Set<HttpTag> httpTags) {
		this.httpTags = httpTags;
	}

	public Set<Message> getMessages() {
		return this.messages;
	}

	public void setMessages(Set<Message> messages) {
		this.messages = messages;
	}

	public String getOverrideDicomTagsList() {
		return this.overrideDicomTags != null && !this.overrideDicomTags.isEmpty()
				? String.join(StringUtil.COMMA, this.overrideDicomTags) : null;
	}

	public void setOverrideDicomTagsList(String overrideDicomTagsList) {
		this.overrideDicomTagsList = overrideDicomTagsList;
	}

	public Set<String> getOverrideDicomTags() {
		return this.overrideDicomTags;
	}

	public void setOverrideDicomTags(Set<String> overrideDicomTags) {
		this.overrideDicomTags = overrideDicomTags;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ArcQuery arcQuery = (ArcQuery) o;
		return this.requireOnlySOPInstanceUID == arcQuery.requireOnlySOPInstanceUID
				&& Objects.equals(this.patients, arcQuery.patients) && Objects.equals(this.httpTags, arcQuery.httpTags)
				&& Objects.equals(this.messages, arcQuery.messages) && Objects.equals(this.arcId, arcQuery.arcId)
				&& Objects.equals(this.baseUrl, arcQuery.baseUrl) && Objects.equals(this.webLogin, arcQuery.webLogin)
				&& Objects.equals(this.additionnalParameters, arcQuery.additionnalParameters)
				&& Objects.equals(this.overrideDicomTagsList, arcQuery.overrideDicomTagsList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.patients, this.httpTags, this.messages, this.arcId, this.baseUrl, this.webLogin,
				this.requireOnlySOPInstanceUID, this.additionnalParameters, this.overrideDicomTagsList);
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

	@Override
	public String toString() {
		return "ArcQuery{" + "patients=" + this.patients + ", httpTags=" + this.httpTags + ", messages=" + this.messages
				+ ", arcId='" + this.arcId + '\'' + ", baseUrl='" + this.baseUrl + '\'' + ", webLogin='" + this.webLogin
				+ '\'' + ", requireOnlySOPInstanceUID=" + this.requireOnlySOPInstanceUID + ", additionnalParameters='"
				+ this.additionnalParameters + '\'' + ", overrideDicomTagsList='" + this.overrideDicomTagsList + '\''
				+ ", overrideDicomTags=" + this.overrideDicomTags + '}';
	}

}
