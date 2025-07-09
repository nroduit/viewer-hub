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

package org.viewer.hub.back.model.searchcriteria;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WeasisArchiveSearchCriteria extends ArchiveSearchCriteria {

	@Schema(description = "Used to modify the properties of the launcher", name = "pro", type = "List<String>",
			example = "weasis.export.dicom true")
	private List<String> pro = new ArrayList<>();

	@Schema(description = "[DEPRECATED][MARK_AS_REMOVAL] Define the context to use for the launcher. Will be replaced by 'config'",
			name = "extCfg", type = "String", example = "dicomizer")
	@JsonProperty("ext-cfg")
	private String extCfg;

	@Schema(description = "Define the context to use for the launcher.", name = "config", type = "String",
			example = "dicomizer")
	private String config;

	@Schema(description = "Argument for the launcher", name = "arg", type = "List<String>",
			example = "$dicom:close –all")
	private List<String> arg = new ArrayList<>();

}
