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

package org.weasis.manager.back.model.manifest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.weasis.manager.back.util.DateTimeUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Study implements Serializable {

	@Serial
	private static final long serialVersionUID = 6556153607975946470L;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("Series")
	private Set<Serie> series = new HashSet<>();

	@JacksonXmlProperty(isAttribute = true, localName = "StudyInstanceUID")
	private String studyInstanceUID;

	@JacksonXmlProperty(isAttribute = true, localName = "StudyDescription")
	private String studyDescription;

	@JacksonXmlProperty(isAttribute = true, localName = "StudyDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
	private LocalDate studyDate;

	@JacksonXmlProperty(isAttribute = true, localName = "StudyTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HHmmss")
	private LocalTime studyTime;

	@JacksonXmlProperty(isAttribute = true, localName = "AccessionNumber")
	private String accessionNumber;

	@JacksonXmlProperty(isAttribute = true, localName = "StudyID")
	private String studyID;

	@JacksonXmlProperty(isAttribute = true, localName = "ReferringPhysicianName")
	private String referringPhysicianName;

	public Study() {
	}

	public Study(String studyInstanceUID, String studyDescription, LocalDate studyDate, LocalTime studyTime,
			String accessionNumber, String studyID, String referringPhysicianName) {
		this.studyInstanceUID = studyInstanceUID;
		this.studyDescription = studyDescription;
		this.studyDate = studyDate;
		this.studyTime = studyTime;
		this.accessionNumber = accessionNumber;
		this.studyID = studyID;
		this.referringPhysicianName = referringPhysicianName;
	}

	public Study(String studyInstanceUID, String studyDescription, LocalDate studyDate, String accessionNumber,
			String studyID, String referringPhysicianName) {
		this.studyInstanceUID = studyInstanceUID;
		this.studyDescription = studyDescription;
		this.studyDate = studyDate;
		this.accessionNumber = accessionNumber;
		this.studyID = studyID;
		this.referringPhysicianName = referringPhysicianName;
	}

	public Set<Serie> getSeries() {
		return this.series;
	}

	public void setSeries(Set<Serie> series) {
		this.series = series;
	}

	public String getStudyInstanceUID() {
		return this.studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public String getStudyDescription() {
		return this.studyDescription;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public LocalDate getStudyDate() {
		return this.studyDate;
	}

	public void setStudyDate(LocalDate studyDate) {
		this.studyDate = studyDate;
	}

	public LocalTime getStudyTime() {
		return this.studyTime;
	}

	public void setStudyTime(LocalTime studyTime) {
		this.studyTime = studyTime;
	}

	@JsonIgnore
	public LocalDateTime getStudyDateTime() {
		return DateTimeUtil.toLocalDateTime(this.studyDate, this.studyTime);
	}

	public String getAccessionNumber() {
		return this.accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getStudyID() {
		return this.studyID;
	}

	public void setStudyID(String studyID) {
		this.studyID = studyID;
	}

	public String getReferringPhysicianName() {
		return this.referringPhysicianName;
	}

	public void setReferringPhysicianName(String referringPhysicianName) {
		this.referringPhysicianName = referringPhysicianName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Study study = (Study) o;
		return Objects.equals(this.series, study.series)
				&& Objects.equals(this.studyInstanceUID, study.studyInstanceUID)
				&& Objects.equals(this.studyDescription, study.studyDescription)
				&& Objects.equals(this.studyDate, study.studyDate) && Objects.equals(this.studyTime, study.studyTime)
				&& Objects.equals(this.accessionNumber, study.accessionNumber)
				&& Objects.equals(this.studyID, study.studyID)
				&& Objects.equals(this.referringPhysicianName, study.referringPhysicianName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.series, this.studyInstanceUID, this.studyDescription, this.studyDate, this.studyTime,
				this.accessionNumber, this.studyID, this.referringPhysicianName);
	}

	@Override
	public String toString() {
		return "Study{" + "series=" + this.series + ", studyInstanceUID='" + this.studyInstanceUID + '\''
				+ ", studyDescription='" + this.studyDescription + '\'' + ", studyDate=" + this.studyDate
				+ ", studyTime=" + this.studyTime + ", accessionNumber='" + this.accessionNumber + '\'' + ", studyID='"
				+ this.studyID + '\'' + ", referringPhysicianName='" + this.referringPhysicianName + '\'' + '}';
	}

	/**
	 * Merge the current study with the study in parameter
	 * @param studyToMerge Serie to merge
	 */
	public void merge(Study studyToMerge) {
		// Retrieve series to merge that are already in the current study
		Set<Serie> seriesAlreadyInStudy = studyToMerge.getSeries()
			.stream()
			.filter(s -> this.series.stream()
				.anyMatch(serie -> Objects.equals(s.getSeriesInstanceUID(), serie.getSeriesInstanceUID())))
			.collect(Collectors.toSet());
		// Retrieve series to merge that are not already in the current study
		Set<Serie> seriesNotAlreadyInStudy = studyToMerge.getSeries()
			.stream()
			.filter(s -> this.series.stream()
				.noneMatch(serie -> Objects.equals(s.getSeriesInstanceUID(), serie.getSeriesInstanceUID())))
			.collect(Collectors.toSet());

		// Series not in current study: add directly
		this.series.addAll(seriesNotAlreadyInStudy);

		// Series in current study: retrieve the series of the current study
		// and merge them with the series to merge
		seriesAlreadyInStudy.forEach(serieToMerge -> {
			Optional<Serie> optionalSerie = this.series.stream()
				.filter(s -> Objects.equals(s.getSeriesInstanceUID(), serieToMerge.getSeriesInstanceUID()))
				.findFirst();
			if (optionalSerie.isPresent()) {
				Serie serie = optionalSerie.get();
				serie.merge(serieToMerge);
			}
		});
	}

}
