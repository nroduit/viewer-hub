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

package org.weasis.manager.back.model.connector;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model which will contains db results
 */
public class DbConnectorResult {

	// Patient
	private String patientName;

	private String patientId;

	private LocalDate patientBirthDate;

	private String patientSex;

	// Study
	private String studyInstanceUid;

	private String studyId;

	private LocalDate studyDate;

	private String accessionNumber;

	private String referringPhysicianName;

	private String studyDescription;

	// Serie
	private String seriesInstanceUid;

	private String modality;

	private String seriesDescription;

	private Integer seriesNumber;

	// Instance
	private String sopInstanceUid;

	private Integer instanceNumber;

	public DbConnectorResult() {
	}

	public DbConnectorResult(String patientName, String patientId, LocalDate patientBirthDate, String patientSex,
			String studyInstanceUid, String studyId, LocalDate studyDate, String accessionNumber,
			String referringPhysicianName, String studyDescription, String seriesInstanceUid, String modality,
			String seriesDescription, Integer seriesNumber, String sopInstanceUid, Integer instanceNumber) {
		this.patientName = patientName;
		this.patientId = patientId;
		this.patientBirthDate = patientBirthDate;
		this.patientSex = patientSex;
		this.studyInstanceUid = studyInstanceUid;
		this.studyId = studyId;
		this.studyDate = studyDate;
		this.accessionNumber = accessionNumber;
		this.referringPhysicianName = referringPhysicianName;
		this.studyDescription = studyDescription;
		this.seriesInstanceUid = seriesInstanceUid;
		this.modality = modality;
		this.seriesDescription = seriesDescription;
		this.seriesNumber = seriesNumber;
		this.sopInstanceUid = sopInstanceUid;
		this.instanceNumber = instanceNumber;
	}

	public String getPatientName() {
		return this.patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPatientId() {
		return this.patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public LocalDate getPatientBirthDate() {
		return this.patientBirthDate;
	}

	public void setPatientBirthDate(LocalDate patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public String getPatientSex() {
		return this.patientSex;
	}

	public void setPatientSex(String patientSex) {
		this.patientSex = patientSex;
	}

	public String getStudyInstanceUid() {
		return this.studyInstanceUid;
	}

	public void setStudyInstanceUid(String studyInstanceUid) {
		this.studyInstanceUid = studyInstanceUid;
	}

	public String getStudyId() {
		return this.studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public LocalDate getStudyDate() {
		return this.studyDate;
	}

	public void setStudyDate(LocalDate studyDate) {
		this.studyDate = studyDate;
	}

	public String getAccessionNumber() {
		return this.accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getReferringPhysicianName() {
		return this.referringPhysicianName;
	}

	public void setReferringPhysicianName(String referringPhysicianName) {
		this.referringPhysicianName = referringPhysicianName;
	}

	public String getStudyDescription() {
		return this.studyDescription;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public String getSeriesInstanceUid() {
		return this.seriesInstanceUid;
	}

	public void setSeriesInstanceUid(String seriesInstanceUid) {
		this.seriesInstanceUid = seriesInstanceUid;
	}

	public String getModality() {
		return this.modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String getSeriesDescription() {
		return this.seriesDescription;
	}

	public void setSeriesDescription(String seriesDescription) {
		this.seriesDescription = seriesDescription;
	}

	public Integer getSeriesNumber() {
		return this.seriesNumber;
	}

	public void setSeriesNumber(Integer seriesNumber) {
		this.seriesNumber = seriesNumber;
	}

	public String getSopInstanceUid() {
		return this.sopInstanceUid;
	}

	public void setSopInstanceUid(String sopInstanceUid) {
		this.sopInstanceUid = sopInstanceUid;
	}

	public Integer getInstanceNumber() {
		return this.instanceNumber;
	}

	public void setInstanceNumber(Integer instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		DbConnectorResult that = (DbConnectorResult) o;
		return Objects.equals(this.patientName, that.patientName) && Objects.equals(this.patientId, that.patientId)
				&& Objects.equals(this.patientBirthDate, that.patientBirthDate)
				&& Objects.equals(this.patientSex, that.patientSex)
				&& Objects.equals(this.studyInstanceUid, that.studyInstanceUid)
				&& Objects.equals(this.studyId, that.studyId) && Objects.equals(this.studyDate, that.studyDate)
				&& Objects.equals(this.accessionNumber, that.accessionNumber)
				&& Objects.equals(this.referringPhysicianName, that.referringPhysicianName)
				&& Objects.equals(this.studyDescription, that.studyDescription)
				&& Objects.equals(this.seriesInstanceUid, that.seriesInstanceUid)
				&& Objects.equals(this.modality, that.modality)
				&& Objects.equals(this.seriesDescription, that.seriesDescription)
				&& Objects.equals(this.seriesNumber, that.seriesNumber)
				&& Objects.equals(this.sopInstanceUid, that.sopInstanceUid)
				&& Objects.equals(this.instanceNumber, that.instanceNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.patientName, this.patientId, this.patientBirthDate, this.patientSex,
				this.studyInstanceUid, this.studyId, this.studyDate, this.accessionNumber, this.referringPhysicianName,
				this.studyDescription, this.seriesInstanceUid, this.modality, this.seriesDescription, this.seriesNumber,
				this.sopInstanceUid, this.instanceNumber);
	}

}
