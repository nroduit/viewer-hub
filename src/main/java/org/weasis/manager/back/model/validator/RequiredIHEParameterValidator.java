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
import org.weasis.manager.back.enums.IHERequestType;
import org.weasis.manager.back.model.WeasisIHESearchCriteria;

import java.util.Objects;

/**
 * IHERequiredParameterValidator: used to check required search parameters depending on
 * the requestType provided
 */
public class RequiredIHEParameterValidator
		implements ConstraintValidator<RequiredIHEParameter, WeasisIHESearchCriteria> {

	@Override
	public boolean isValid(WeasisIHESearchCriteria weasisIHESearchCriteria,
			ConstraintValidatorContext constraintValidatorContext) {
		// case request Patient
		if (Objects.equals(IHERequestType.PATIENT, weasisIHESearchCriteria.getRequestType())
				&& weasisIHESearchCriteria.getPatientID() != null
				&& !weasisIHESearchCriteria.getPatientID().isBlank()) {
			return true;
		}
		// case request Study
		else {
			return Objects.equals(IHERequestType.STUDY, weasisIHESearchCriteria.getRequestType())
					&& (!weasisIHESearchCriteria.getStudyUID().isEmpty()
							&& weasisIHESearchCriteria.getAccessionNumber().isEmpty()
							|| weasisIHESearchCriteria.getStudyUID().isEmpty()
									&& !weasisIHESearchCriteria.getAccessionNumber().isEmpty());
		}
	}

}
