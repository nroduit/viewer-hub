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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.validator.ValidConnectorProperty;

@NotNull
@ValidConnectorProperty
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ConnectorProperty {

	private String id;

	@NotNull
	private ConnectorType type;

	// -------- Wado --------
	@Valid
	@NotNull
	private SearchCriteriaProperty searchCriteria;

	// -------- Wado --------
	@Valid
	@NotNull
	private WadoConnectorProperty wado;

	// -------- For database --------

	private DbConnectorProperty dbConnector;

	// -------- For dicom ----------

	private DicomConnectorProperty dicomConnector;

}
