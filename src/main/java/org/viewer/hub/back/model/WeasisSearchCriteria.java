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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.viewer.hub.back.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Model which represents the search criteria when launching Weasis
 */

@Getter
@NotNull
@ToString
@EqualsAndHashCode(callSuper = true)
public class WeasisSearchCriteria extends SearchCriteria {

	@Schema(description = "Patient Ids to look for", name = "patientID", type = "Set<String>", example = "123,456,789")
	private Set<String> patientID;

	@Schema(description = "Study UIDs to look for", name = "studyUID", type = "Set<String>",
			example = "1.3.6.1.4.1.237391,1.3.6.1.4.1.237392,1.3.6.1.4.1.237393")
	private Set<String> studyUID;

	@Schema(description = "Accession numbers to look for", name = "accessionNumber", type = "Set<String>",
			example = "A123,A456,A789")
	private Set<String> accessionNumber;

	@Schema(description = "Series UIDs to look for", name = "seriesUID", type = "Set<String>",
			example = "1.3.6.1.4.1.237391,1.3.6.1.4.1.237392,1.3.6.1.4.1.237393")
	private Set<String> seriesUID;

	@Schema(description = "Object UIDs to look for", name = "objectUID", type = "Set<String>",
			example = "1.3.6.1.4.1.237391,1.3.6.1.4.1.237392,1.3.6.1.4.1.237393")
	private Set<String> objectUID;

	/**
	 * Constructor
	 */
	public WeasisSearchCriteria() {
		this.patientID = new HashSet<>();
		this.studyUID = new HashSet<>();
		this.accessionNumber = new HashSet<>();
		this.seriesUID = new HashSet<>();
		this.objectUID = new HashSet<>();
	}

	public void setPatientID(Set<String> patientID) {
		this.patientID = StringUtil.splitCommaSeparatedValuesToList(patientID);
	}

	public void setStudyUID(Set<String> studyUID) {
		this.studyUID = StringUtil.splitCommaSeparatedValuesToList(studyUID);
	}

	public void setAccessionNumber(Set<String> accessionNumber) {
		this.accessionNumber = StringUtil.splitCommaSeparatedValuesToList(accessionNumber);
	}

	public void setSeriesUID(Set<String> seriesUID) {
		this.seriesUID = StringUtil.splitCommaSeparatedValuesToList(seriesUID);
	}

	public void setObjectUID(Set<String> objectUID) {
		this.objectUID = StringUtil.splitCommaSeparatedValuesToList(objectUID);
	}

}
