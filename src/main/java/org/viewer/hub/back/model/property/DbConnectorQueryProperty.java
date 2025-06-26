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

package org.viewer.hub.back.model.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DbConnectorQueryProperty {

	@Schema(description = "Represents the select query to execute ")
	private String select;

	@Schema(description = "Name of the accession number column")
	private String accessionNumberColumn;

	@Schema(description = "Name of the accession number column")
	private String patientIdColumn;

	@Schema(description = "Name of the study instance uid column")
	private String studyInstanceUidColumn;

	@Schema(description = "Name of the series instance uid column")
	private String serieInstanceUidColumn;

	@Schema(description = "Name of the sop instance uid column")
	private String sopInstanceUidColumn;

}
