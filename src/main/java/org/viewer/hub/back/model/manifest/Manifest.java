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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.viewer.hub.back.model.SearchCriteria;
import org.viewer.hub.back.model.presentation.Presentation;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.model.selection.Selections;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manifest: used by Weasis to determine which studies, series or sop instances uids to
 * retrieve Corresponds to this validation file:
 * https://github.com/nroduit/Weasis/blob/master/weasis-dicom/weasis-dicom-explorer/src/main/resources/config/manifest.xsd
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@JacksonXmlRootElement(localName = "manifest")
public class Manifest implements Serializable {

	@Serial
	private static final long serialVersionUID = 5188895105209680366L;

	@JacksonXmlProperty(isAttribute = true)
	private final String xmlns = "http://www.weasis.org/xsd/2.5";

	@JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
	private final String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";

	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("arcQuery")
	private List<ArcQuery> arcQueries = new ArrayList<>();

	// TODO later: to validate with xml and implement
	private List<Presentation> presentations;

	// TODO later: to validate with xml and implement
	private List<Selections> selections;

	@JsonInclude(Include.NON_EMPTY)
	private String uid;

	@JsonIgnore
	private boolean isRequestAuthenticated;

	@JsonIgnore
	private LocalDateTime startManifestRequest;

	@JsonIgnore
	private long buildDuration;

	@JsonIgnore
	private SearchCriteria searchCriteria;

	@JsonIgnore
	private boolean buildInProgress;

	@JsonIgnore
	private String accessToken;

	public Manifest(boolean isRequestAuthenticated, SearchCriteria searchCriteria) {
		this.isRequestAuthenticated = isRequestAuthenticated;
		this.startManifestRequest = LocalDateTime.now();
		this.searchCriteria = searchCriteria;
	}

	/**
	 * Check if the manifest contains patient id in the archive id in parameter
	 * @param patientId Patient Id to evaluate
	 * @param archiveId Archive Id to evaluate
	 * @return true if the manifest contains the patient id
	 */
	public boolean containsPatient(String patientId, String archiveId) {
		return this.arcQueries.stream()
			.anyMatch(aq -> Objects.equals(aq.getArcId(), archiveId) && aq.containsPatient(patientId));
	}

	/**
	 * Retrieve in the manifest the patient corresponding to the patient id and the
	 * archive id in parameter
	 * @param patientId Patient Id to evaluate
	 * @param archiveId Archive Id to evaluate
	 * @return Patient found
	 */
	public Patient retrievePatient(String patientId, String archiveId) {
		return this.arcQueries.stream()
			.filter(aq -> Objects.equals(aq.getArcId(), archiveId) && aq.containsPatient(patientId))
			.findFirst()
			.map(aqc -> aqc.retrievePatientFromPatientId(patientId))
			.orElse(null);
	}

	/**
	 * Update the manifest with the list of patients in parameter
	 * @param patients patients to add
	 * @param connector Connector property
	 */
	public void update(Set<Patient> patients, @Valid ConnectorProperty connector) {
		if (!patients.isEmpty()) {
			if (this.getArcQueries().isEmpty() || this.getArcQueries()
				.stream()
				.noneMatch(aq -> Objects.equals(aq.getArcId(), connector.getId()))) {
				this.createAndAddNewArcQuery(connector);
			}

			this.getArcQueries()
				.stream()
				.filter(aq -> Objects.equals(aq.getArcId(), connector.getId()))
				.findFirst()
				.ifPresent(arcQuery -> patients.forEach(patient -> {
					// Merge existing patient
					if (arcQuery.containsPatient(patient.getPatientID())) {
						arcQuery.getPatients()
							.stream()
							.filter(p -> Objects.equals(p.getPatientID(), patient.getPatientID()))
							.findFirst()
							.ifPresent(p -> p.merge(patient));
					}
					// Create new one
					else {
						arcQuery.getPatients().add(patient);
					}
				}));
		}
	}

	/**
	 * Create and fill new arc query
	 * @param connector Connector properties
	 */
	private void createAndAddNewArcQuery(ConnectorProperty connector) {
		ArcQuery arcQuery = new ArcQuery();
		// Arc id
		arcQuery.setArcId(connector.getId());
		// Require Only SOP InstanceUID
		arcQuery.setRequireOnlySOPInstanceUID(connector.getWeasis().getManifest().getRequireOnlySOPInstanceUID() != null
				&& connector.getWeasis().getManifest().getRequireOnlySOPInstanceUID());
		// Additional Parameters
		arcQuery.setAdditionnalParameters(connector.getWeasis().getManifest().getAdditionnalParameters());
		// Override Dicom Tags List
		arcQuery.setOverrideDicomTags(connector.getWeasis().getManifest().getOverrideDicomTags());
		// Http Tags
		if (connector.getWeasis().getManifest().getHttpTags() != null) {
			arcQuery.getHttpTags()
				.addAll(connector.getWeasis()
					.getManifest()
					.getHttpTags()
					.keySet()
					.stream()
					.map(k -> new HttpTag(k, connector.getWeasis().getManifest().getHttpTags().get(k)))
					.collect(Collectors.toSet()));
		}

		// Set the query mode in order for Weasis to user the corresponding requests to
		// retrieve the images
		arcQuery.setQueryMode(connector.getType());

		this.getArcQueries().add(arcQuery);
	}

}
