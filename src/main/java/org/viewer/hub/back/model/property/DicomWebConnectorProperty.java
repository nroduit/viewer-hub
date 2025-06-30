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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DicomWebConnectorProperty {

	@Valid
	@NotNull
	@Schema(description = "Wado-rs properties")
	private ConnectorDicomWebProperty wadoRs;

	@Valid
	@NotNull
	@Schema(description = "Qido-rs properties")
	private ConnectorDicomWebProperty qidoRs;

	@JsonIgnore
	private WebClient webClientWadoRs;

	@JsonIgnore
	private WebClient webClientQidoRs;

}
