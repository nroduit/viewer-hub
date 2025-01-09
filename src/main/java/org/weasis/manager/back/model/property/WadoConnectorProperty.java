/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.model.property;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@Validated
public class WadoConnectorProperty {

	@NotNull
	private AuthenticationProperty authentication;

	private String transferSyntaxUid;

	private Integer compressionRate;

	private Boolean requireOnlySOPInstanceUID;

	private String additionnalParameters;

	// example: 0x11112222
	private Set<@Pattern(
			regexp = "0x[0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F]") String> overrideDicomTags;

	private Map<String, String> httpTags = new HashMap<>();

	public WadoConnectorProperty(AuthenticationProperty authentication, String transferSyntaxUid,
			Integer compressionRate, Boolean requireOnlySOPInstanceUID, String additionnalParameters,
			Set<@Pattern(
					regexp = "0x[0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F]") String> overrideDicomTags,
			Map<String, String> httpTags) {
		this.authentication = authentication;
		this.transferSyntaxUid = transferSyntaxUid;
		this.compressionRate = compressionRate;
		this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
		this.additionnalParameters = additionnalParameters;
		this.overrideDicomTags = overrideDicomTags;
		this.httpTags = httpTags;
	}

}
