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
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wado properties used to retrieve images
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
@Validated
public class WeasisManifestConnectorProperty {

	@Schema(description = "Transfer syntax uid")
	private String transferSyntaxUid;

	@Schema(description = "Compression rate")
	private Integer compressionRate;

	@Schema(description = "Require only SOPInstanceUID")
	private Boolean requireOnlySOPInstanceUID;

	@Schema(description = "Additional parameters")
	private String additionnalParameters;

	// example: 0x11112222
	@Schema(description = "Override dicom tags")
	private Set<@Pattern(
			regexp = "0x[0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F]") String> overrideDicomTags;

	@Schema(description = "Http tags")
	private Map<String, String> httpTags = new HashMap<>();

	public WeasisManifestConnectorProperty(String transferSyntaxUid, Integer compressionRate,
			Boolean requireOnlySOPInstanceUID, String additionnalParameters,
			Set<@Pattern(
					regexp = "0x[0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F]") String> overrideDicomTags,
			Map<String, String> httpTags) {
		this.transferSyntaxUid = transferSyntaxUid;
		this.compressionRate = compressionRate;
		this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
		this.additionnalParameters = additionnalParameters;
		this.overrideDicomTags = overrideDicomTags;
		this.httpTags = httpTags;
	}

}
