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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.viewer.hub.back.util.DateTimeUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Serie implements Serializable {

	@Serial
	private static final long serialVersionUID = -3455369892278940902L;

	@Builder.Default
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("Instance")
	private Set<Instance> instances = new HashSet<>();

	@JacksonXmlProperty(isAttribute = true, localName = "SeriesInstanceUID")
	private String seriesInstanceUID;

	@JacksonXmlProperty(isAttribute = true, localName = "SeriesDescription")
	private String seriesDescription;

	@JacksonXmlProperty(isAttribute = true, localName = "SeriesNumber")
	private Integer seriesNumber;

	@JacksonXmlProperty(isAttribute = true, localName = "Modality")
	private String modality;

	@JacksonXmlProperty(isAttribute = true, localName = "SeriesDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
	private LocalDate seriesDate;

	@JacksonXmlProperty(isAttribute = true, localName = "SeriesTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HHmmss")
	private LocalTime seriesTime;

	@JacksonXmlProperty(isAttribute = true, localName = "WadoTransferSyntaxUID")
	@JsonInclude(Include.NON_EMPTY)
	private String wadoTransferSyntaxUID;

	@JacksonXmlProperty(isAttribute = true, localName = "WadoCompressionRate")
	private Integer wadoCompressionRate;

	@JacksonXmlProperty(isAttribute = true, localName = "DirectDownloadThumbnail")
	// TODO later: not implemented yet
	private String directDownloadThumbnail;

	public Serie(String seriesInstanceUID, String seriesDescription, Integer seriesNumber, String modality,
			LocalDateTime seriesDateTime, String wadoTransferSyntaxUID, Integer wadoCompressionRate) {
		this.instances = new HashSet<>();
		this.seriesInstanceUID = seriesInstanceUID;
		this.seriesDescription = seriesDescription;
		this.seriesNumber = seriesNumber;
		this.modality = modality;
		this.seriesDate = seriesDateTime != null ? seriesDateTime.toLocalDate() : null;
		this.seriesTime = seriesDateTime != null ? seriesDateTime.toLocalTime() : null;
		this.wadoTransferSyntaxUID = wadoTransferSyntaxUID;
		this.wadoCompressionRate = wadoCompressionRate;
	}

	public Serie(String seriesInstanceUID, String seriesDescription, Integer seriesNumber, String modality,
			Date seriesDate, Date seriesTime, String wadoTransferSyntaxUID, Integer wadoCompressionRate) {
		this.instances = new HashSet<>();
		this.seriesInstanceUID = seriesInstanceUID;
		this.seriesDescription = seriesDescription;
		this.seriesNumber = seriesNumber;
		this.modality = modality;
		this.seriesDate = seriesDate != null ? DateTimeUtil.toLocalDate(seriesDate) : null;
		this.seriesTime = seriesTime != null ? DateTimeUtil.toLocalTime(seriesTime) : null;
		this.wadoTransferSyntaxUID = wadoTransferSyntaxUID;
		this.wadoCompressionRate = wadoCompressionRate;
	}

	/**
	 * Merge the current serie with the serie in parameter
	 * @param serieToMerge Serie to merge
	 */
	public void merge(Serie serieToMerge) {
		// Retrieve instances to merge that are not already in the current serie
		Set<Instance> instancesNotAlreadyInSerie = serieToMerge.getInstances()
			.stream()
			.filter(i -> this.instances.stream()
				.noneMatch(instance -> Objects.equals(i.getSopInstanceUID(), instance.getSopInstanceUID())))
			.collect(Collectors.toSet());

		// Instances not in current serie: add directly
		this.instances.addAll(instancesNotAlreadyInSerie);
	}

}
