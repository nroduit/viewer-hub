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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "weasisProfile")
@XmlAccessorType(XmlAccessType.FIELD)
public class WeasisProfile implements Serializable {

	@Serial
	private static final long serialVersionUID = -1104901232263820551L;

	@XmlElement(name = "id")
	@Schema(description = "Weasis profile id", name = "id", type = "Long", example = "1")
	private Long id;

	@XmlElement(name = "name")
	@Schema(description = "Weasis profile name", name = "name", type = "String", example = "dicomizer")
	private String name;

}
