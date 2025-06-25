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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Serie implements Serializable {

	@Serial
	private static final long serialVersionUID = -3455369892278940902L;

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

	@JacksonXmlProperty(isAttribute = true, localName = "WadoTransferSyntaxUID")
	@JsonInclude(Include.NON_EMPTY)
	private String wadoTransferSyntaxUID;

	@JacksonXmlProperty(isAttribute = true, localName = "WadoCompressionRate")
	private Integer wadoCompressionRate;

	@JacksonXmlProperty(isAttribute = true, localName = "DirectDownloadThumbnail")
	// TODO later: not implemented yet
	private String directDownloadThumbnail;

	public Serie() {
	}

	public Serie(String seriesInstanceUID, String seriesDescription, Integer seriesNumber, String modality,
			String wadoTransferSyntaxUID, Integer wadoCompressionRate) {
		this.seriesInstanceUID = seriesInstanceUID;
		this.seriesDescription = seriesDescription;
		this.seriesNumber = seriesNumber;
		this.modality = modality;
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
