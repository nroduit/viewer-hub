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

package org.weasis.manager.back.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.weasis.manager.back.config.properties.WeasisPackageDefaultConfigurationProperties;
import org.weasis.manager.back.util.StringUtil;

import java.util.Objects;

/**
 * ValidQualifierPropertyValidator: used to check valid qualifier in properties
 */
public class ValidQualifierPropertyValidator
		implements ConstraintValidator<ValidQualifierProperty, WeasisPackageDefaultConfigurationProperties> {

	@Override
	public boolean isValid(WeasisPackageDefaultConfigurationProperties weasisPackageVersionDefaultConfigurationProperty,
			ConstraintValidatorContext constraintValidatorContext) {
		String qualifier = weasisPackageVersionDefaultConfigurationProperty.getQualifier();
		// If not null, check that qualifier contains "-" as first character and size > 2
		return qualifier == null
				|| (qualifier.length() >= 2 && Objects.equals(qualifier.substring(0, 1), StringUtil.HYPHEN));
	}

}
