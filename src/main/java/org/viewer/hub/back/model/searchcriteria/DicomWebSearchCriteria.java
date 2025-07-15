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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NotNull
@ToString
public class DicomWebSearchCriteria {

	@Schema(type = "String")
	String PatientName;

	@Schema(type = "String")
	String patientID;

	@Schema(type = "String")
	String AccessionNumber;

	@Schema(type = "String")
	String StudyDescription;

	@Schema(type = "String")
	String ModalitiesInStudy;

	@Schema(type = "String")
	String limit;

	@Schema(type = "String")
	String offset;

	@Schema(type = "String")
	String fuzzymatching;

	@Schema(type = "String")
	String includefield;

	@Schema(type = "String")
	String StudyDate;

	public DicomWebSearchCriteria() {
		this.PatientName = "";
		this.patientID = "";
		this.AccessionNumber = "";
		this.StudyDescription = "";
		this.ModalitiesInStudy = "";
		this.limit = "";
		this.offset = "";
		this.fuzzymatching = "";
		this.includefield = "";
		this.StudyDate = "";
	}

//	public String getUrlArgs() {
//		List<String> args = new ArrayList<>();
//		if (!this.PatientName.isEmpty()) {
//			args.add("PatientName=" + this.PatientName);
//		}
//		if (!this.patientID.isEmpty()) {
//			args.add("patientID=" + this.patientID);
//		}
//		if (!this.AccessionNumber.isEmpty()) {
//			args.add("AccessionNumber=" + this.AccessionNumber);
//		}
//		if (!this.StudyDescription.isEmpty()) {
//			args.add("StudyDescription=" + this.StudyDescription);
//		}
//		if (!this.ModalitiesInStudy.isEmpty()) {
//			args.add("ModalitiesInStudy=" + this.ModalitiesInStudy);
//		}
//		if (!this.limit.isEmpty()) {
//			args.add("limit=" + this.limit);
//		}
//		if (!this.offset.isEmpty()) {
//			args.add("offset=" + this.offset);
//		}
//		if (!this.fuzzymatching.isEmpty()) {
//			args.add("fuzzymatching=" + this.fuzzymatching);
//		}
//		if (!this.includefield.isEmpty()) {
//			args.add("includefield=" + this.includefield);
//		}
//		if (!this.StudyDate.isEmpty()) {
//			args.add("StudyDate=" + this.StudyDate);
//		}
//		String url = "";
//		for (int i=0; i<args.size(); i++) {
//			url += i==0 ? "?" : "&";
//			url += args.get(i);
//		}
//		return url;
//	}

}
