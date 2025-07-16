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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.model.validator.ValidConnectorProperty;

@NotNull
@ValidConnectorProperty
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ConnectorProperty {

	@Schema(description = "Id of the connector")
	private String id;

	// -------- Search criteria --------
	@NotNull
	@Schema(description = "Name of connector")
	private String name;

	// -------- SearchCriteriaProperty --------
	@Valid
	@NotNull
	@Schema(description = "Search criteria")
	private SearchCriteriaProperty searchCriteria;

	// -------- For database --------
	@Schema(description = "Db connector properties")
	@Valid
	private DbConnectorProperty dbConnector;

	// -------- For dicom ----------
	@Schema(description = "Dicom connector properties")
	@Valid
	private DicomConnectorProperty dicomConnector;

	// -------- For dicom-web ----------
	@Schema(description = "Dicom-web connector properties")
	private DicomWebConnectorProperty dicomWebConnector;

	// -------- Weasis --------
	@Valid
	@NotNull
	@Schema(description = "Properties specific to Weasis")
	private WeasisConnectorProperty weasis;


}
