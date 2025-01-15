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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Instance implements Serializable {

	@Serial
	private static final long serialVersionUID = 7225568734948080876L;

	@JacksonXmlProperty(isAttribute = true, localName = "SOPInstanceUID")
	private String sopInstanceUID;

	@JacksonXmlProperty(isAttribute = true, localName = "InstanceNumber")
	private Integer instanceNumber;

	@JacksonXmlProperty(isAttribute = true, localName = "DirectDownloadFile")
	// TODO later: not implemented yet
	private String directDownloadFile;

	public Instance(String sopInstanceUID, Integer instanceNumber) {
		this.sopInstanceUID = sopInstanceUID;
		this.instanceNumber = instanceNumber;
	}

	public String getSopInstanceUID() {
		return this.sopInstanceUID;
	}

	public void setSopInstanceUID(String sopInstanceUID) {
		this.sopInstanceUID = sopInstanceUID;
	}

	public Integer getInstanceNumber() {
		return this.instanceNumber;
	}

	public void setInstanceNumber(Integer instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	public String getDirectDownloadFile() {
		return this.directDownloadFile;
	}

	public void setDirectDownloadFile(String directDownloadFile) {
		this.directDownloadFile = directDownloadFile;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Instance instance = (Instance) o;
		return this.instanceNumber == instance.instanceNumber
				&& Objects.equals(this.sopInstanceUID, instance.sopInstanceUID)
				&& Objects.equals(this.directDownloadFile, instance.directDownloadFile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.sopInstanceUID, this.instanceNumber, this.directDownloadFile);
	}

	@Override
	public String toString() {
		return "Instance{" + "sopInstanceUID='" + this.sopInstanceUID + '\'' + ", instanceNumber=" + this.instanceNumber
				+ ", directDownloadFile='" + this.directDownloadFile + '\'' + '}';
	}

}
