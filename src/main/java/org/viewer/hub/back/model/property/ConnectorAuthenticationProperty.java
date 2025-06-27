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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.enums.ConnectorAuthType;

@NotNull
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Validated
@Schema(description = "Model used to represent the authentication connector properties")
public class ConnectorAuthenticationProperty {

	@NotNull
	@Schema(description = "Type of authentication")
	private ConnectorAuthType type;

	@Schema(description = "Properties for oauth2 authentication")
	private ConnectorOauth2AuthProperty oauth2;

	@Schema(description = "Properties for basic authentication")
	private ConnectorBasicAuthProperty basic;

}