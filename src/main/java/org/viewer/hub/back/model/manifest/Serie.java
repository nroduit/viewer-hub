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
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

	public Set<Instance> getInstances() {
		return this.instances;
	}

	public void setInstances(Set<Instance> instances) {
		this.instances = instances;
	}

	public String getSeriesInstanceUID() {
		return this.seriesInstanceUID;
	}

	public void setSeriesInstanceUID(String seriesInstanceUID) {
		this.seriesInstanceUID = seriesInstanceUID;
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

	public String getModality() {
		return this.modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String getWadoTransferSyntaxUID() {
		return this.wadoTransferSyntaxUID;
	}

	public void setWadoTransferSyntaxUID(String wadoTransferSyntaxUID) {
		this.wadoTransferSyntaxUID = wadoTransferSyntaxUID;
	}

	public Integer getWadoCompressionRate() {
		return this.wadoCompressionRate;
	}

	public void setWadoCompressionRate(Integer wadoCompressionRate) {
		this.wadoCompressionRate = wadoCompressionRate;
	}

	public String getDirectDownloadThumbnail() {
		return this.directDownloadThumbnail;
	}

	public void setDirectDownloadThumbnail(String directDownloadThumbnail) {
		this.directDownloadThumbnail = directDownloadThumbnail;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Serie serie = (Serie) o;
		return Objects.equals(this.instances, serie.instances)
				&& Objects.equals(this.seriesInstanceUID, serie.seriesInstanceUID)
				&& Objects.equals(this.seriesDescription, serie.seriesDescription)
				&& Objects.equals(this.seriesNumber, serie.seriesNumber)
				&& Objects.equals(this.modality, serie.modality)
				&& Objects.equals(this.wadoTransferSyntaxUID, serie.wadoTransferSyntaxUID)
				&& Objects.equals(this.wadoCompressionRate, serie.wadoCompressionRate)
				&& Objects.equals(this.directDownloadThumbnail, serie.directDownloadThumbnail);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.instances, this.seriesInstanceUID, this.seriesDescription, this.seriesNumber,
				this.modality, this.wadoTransferSyntaxUID, this.wadoCompressionRate, this.directDownloadThumbnail);
	}

	@Override
	public String toString() {
		return "Serie{" + "instances=" + this.instances + ", seriesInstanceUID='" + this.seriesInstanceUID + '\''
				+ ", seriesDescription='" + this.seriesDescription + '\'' + ", seriesNumber=" + this.seriesNumber
				+ ", modality='" + this.modality + '\'' + ", wadoTransferSyntaxUID='" + this.wadoTransferSyntaxUID
				+ '\'' + ", wadoCompressionRate=" + this.wadoCompressionRate + ", directDownloadThumbnail='"
				+ this.directDownloadThumbnail + '\'' + '}';
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
