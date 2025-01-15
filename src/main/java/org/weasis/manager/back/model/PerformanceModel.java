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

package org.weasis.manager.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model used to send performance kpi to kibana
 */
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceModel {

	@NotBlank(message = "User should be filled")
	@Schema(description = "User Id", name = "userId", type = "String", example = "abcd")
	private String userId;

	@NotBlank(message = "Host should be filled")
	@Schema(description = "Host", name = "host", type = "String", example = "pc-xxxx")
	private String host;

	@NotBlank(message = "Type should be filled")
	@Schema(description = "Type", name = "type", type = "String", example = "WADO")
	private String type;

	@NotBlank(message = "Series UID should be filled")
	@Schema(description = "Series UID", name = "seriesUID", type = "String", example = "1.234.567")
	private String seriesUID;

	@NotBlank(message = "Modality should be filled")
	@Schema(description = "Modality", name = "modality", type = "String", example = "XC")
	private String modality;

	@NotNull(message = "Number of images should not be null")
	@Schema(description = "Number images", name = "nbImages", type = "Integer", example = "2")
	private Integer nbImages;

	@NotNull(message = "Size should not be null")
	@Schema(description = "Size of the transfer", name = "size", type = "Long", example = "1")
	private Long size;

	@NotNull(message = "Time should not be null")
	@Schema(description = "Duration of the transfer", name = "time", type = "Long", example = "1")
	private Long time;

	@NotBlank(message = "Rate should be filled")
	@Schema(description = "rate of the transfer", name = "rate", type = "String", example = "250ko/s")
	private String rate;

	@NotNull(message = "Errors should not be null")
	@Schema(description = "Number of errors during the transfer", name = "errors", type = "Integer", example = "1")
	private Integer errors;

}
