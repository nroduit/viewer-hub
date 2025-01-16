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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeasisModule implements Serializable {

	@Serial
	private static final long serialVersionUID = 6406133215322044079L;

	@Schema(description = "Weasis module id", name = "id", type = "Long", example = "1")
	@JacksonXmlProperty(localName = "id")
	private Long id;

	@Schema(description = "Weasis module name", name = "name", type = "String", example = "weasis-core-ui")
	@JacksonXmlProperty(localName = "name")
	private String name;

}
