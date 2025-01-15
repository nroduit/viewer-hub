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

package org.viewer.hub.back.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.model.validator.ValidQualifierProperty;

/**
 * Configuration properties model of default packaging version
 */
@Validated
@ConfigurationProperties(prefix = "weasis.package.version.default")
@ValidQualifierProperty
public class WeasisPackageDefaultConfigurationProperties {

	// Number of the model
	// example: xx.xx.xx
	@NotBlank
	@Pattern(regexp = "^[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")
	private String number;

	// If not null or blank, check that contains "-" as first character
	// example: -TEST
	private String qualifier;

	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getQualifier() {
		return this.qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier != null && qualifier.trim().isBlank() ? null : qualifier;
	}

}
