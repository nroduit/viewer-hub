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

package org.viewer.hub.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * ViewerSelectionModel Filters
 */
@Setter
@Getter
public class ViewerSelectionFilter {

	@Schema(description = "Modality", name = "modality", type = "String", example = "CT,ES,MR")
	private String modality;

	@Schema(description = "Name of the archive associated to the viewer selection rule", name = "archive", type = "String",
			example = "dcm4chee")
	private String archive;

	@Schema(description = "Code of the viewer associated to the rule",
			name = "viewer", type = "String", example = "WEASIS")
	private String viewer;

}
