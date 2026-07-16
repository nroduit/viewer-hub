/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.model.patient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.viewer.hub.back.util.DateTimeUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
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

	public Study(String studyInstanceUID, String studyDescription, Date studyDate, Date studyTime,
			String accessionNumber, String studyID, String referringPhysicianName) {
		this.studyInstanceUID = studyInstanceUID;
		this.studyDescription = studyDescription;
		this.studyDate = studyDate != null ? DateTimeUtil.toLocalDate(studyDate) : null;
		this.studyTime = studyTime != null ? DateTimeUtil.toLocalTime(studyTime) : null;
		this.accessionNumber = accessionNumber;
		this.studyID = studyID;
		this.referringPhysicianName = referringPhysicianName;
	}

	public Study(String studyInstanceUID, String studyDescription, LocalDateTime studyDateTime, String accessionNumber,
			String studyID, String referringPhysicianName) {
		this.studyInstanceUID = studyInstanceUID;
		this.studyDescription = studyDescription;
		this.studyDate = studyDateTime != null ? studyDateTime.toLocalDate() : null;
		this.studyTime = studyDateTime != null ? studyDateTime.toLocalTime() : null;
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
			optionalSerie.ifPresent(serie -> serie.merge(serieToMerge));
		});
	}

}
