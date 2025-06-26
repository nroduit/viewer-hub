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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
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

}
