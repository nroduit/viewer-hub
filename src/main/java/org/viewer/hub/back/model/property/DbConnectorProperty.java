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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@Builder
public class DbConnectorProperty {

	@Schema(description = "Database user")
	private String user;

	@Schema(description = "Database password")
	private String password;

	@Schema(description = "Database uri")
	private String uri;

	@Schema(description = "Database driver")
	private String driver;

	@Schema(description = "Query properties to execute")
	private DbConnectorQueryProperty query;

	@Schema(description = "Wado properties to retrieve images")
	private ConnectorWadoProperty wado;

}
