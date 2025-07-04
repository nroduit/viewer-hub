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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.viewer.hub.back.enums.IHERequestType;
import org.viewer.hub.back.model.validator.RequiredIHEParameter;
import org.viewer.hub.back.util.StringUtil;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Model which represents the search criteria when launching Weasis with a IHE query
 */
@Getter
@RequiredIHEParameter
@ToString
@EqualsAndHashCode(callSuper = true)
public class IHESearchCriteria extends SearchCriteria {

	@Schema(description = "IHE request type. Value can only be STUDY or PATIENT", name = "requestType",
			type = "IHERequestType", example = "STUDY")
	private IHERequestType requestType;

	@Schema(description = "Patient Id to look for", name = "patientID", type = "String", example = "123")
	private String patientID;

	@Schema(description = "Study UIDs to look for", name = "studyUID", type = "Set<String>",
			example = "1.3.6.1.4.1.237391,1.3.6.1.4.1.237392,1.3.6.1.4.1.237393")
	private Set<String> studyUID;

	@Schema(description = "Accession numbers to look for", name = "accessionNumber", type = "Set<String>",
			example = "A123,A456,A789")
	private Set<String> accessionNumber;

	// Not handle yet
	@Schema(description = "Not handle yet", name = "patientName", type = "String", example = "Not handle yet")
	private String patientName;

	// Not handle yet
	@Schema(description = "Not handle yet", name = "patientBirthDate", type = "LocalDateTime",
			example = "Not handle yet")
	private LocalDateTime patientBirthDate;

	// Not handle yet
	@Schema(description = "Not handle yet", name = "viewerType", type = "String", example = "Not handle yet")
	private String viewerType;

	// Not handle yet
	@Schema(description = "Not handle yet", name = "diagnosticQuality", type = "Boolean", example = "Not handle yet")
	private Boolean diagnosticQuality;

	// Not handle yet
	@Schema(description = "Not handle yet", name = "keyImagesOnly", type = "Boolean", example = "Not handle yet")
	private Boolean keyImagesOnly;

	/**
	 * Constructor
	 */
	public IHESearchCriteria() {
		this.studyUID = new HashSet<>();
		this.accessionNumber = new HashSet<>();
	}

	public void setRequestType(IHERequestType requestType) {
		this.requestType = requestType;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public void setPatientBirthDate(LocalDateTime patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public void setViewerType(String viewerType) {
		this.viewerType = viewerType;
	}

	public void setDiagnosticQuality(Boolean diagnosticQuality) {
		this.diagnosticQuality = diagnosticQuality;
	}

	public void setKeyImagesOnly(Boolean keyImagesOnly) {
		this.keyImagesOnly = keyImagesOnly;
	}

	public void setStudyUID(Set<String> studyUID) {
		this.studyUID = StringUtil.splitCommaSeparatedValuesToList(studyUID);
	}

	public void setAccessionNumber(Set<String> accessionNumber) {
		this.accessionNumber = StringUtil.splitCommaSeparatedValuesToList(accessionNumber);
	}

}
