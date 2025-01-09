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

package org.weasis.manager.back.util;

import org.weasis.manager.back.constant.SearchCriteriaConstant;

/**
 * Helper for connectors
 */
public class ConnectorUtil {

	private ConnectorUtil() {
		// Private constructor to hide implicit one
	}

	/**
	 * Retrieve the patient id depending on the Hl7 syntax: patientId^^^issuerOfPatientId
	 * @param patientId patient id to evaluate
	 * @return patient id found
	 */
	public static String determinePatientIdDependingHl7Syntax(String patientId) {
		String patientIdDependingHl7Syntax = null;
		if (!patientId.contains(SearchCriteriaConstant.CIRCUMFLEX)
				&& !patientId.contains(SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX)) {
			patientIdDependingHl7Syntax = patientId;
		}
		else if (patientId.contains(SearchCriteriaConstant.CIRCUMFLEX)) {
			patientIdDependingHl7Syntax = patientId.substring(0, patientId.indexOf(SearchCriteriaConstant.CIRCUMFLEX));
		}
		else if (patientId.contains(SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX)) {
			patientIdDependingHl7Syntax = patientId.substring(0,
					patientId.indexOf(SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX));
		}
		return patientIdDependingHl7Syntax;
	}

	/**
	 * Retrieve the issuer of patient id depending on the Hl7 syntax:
	 * patientId^^^issuerOfPatientId
	 * @param patientId patient id to evaluate
	 * @return issuer of patient id found
	 */
	public static String determineIssuerPatientIdDependingHl7Syntax(String patientId) {
		String issuerPatientIdDependingHl7Syntax = null;
		if (!patientId.contains(SearchCriteriaConstant.CIRCUMFLEX)
				&& !patientId.contains(SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX)) {
			issuerPatientIdDependingHl7Syntax = null;
		}
		else if (patientId.contains(SearchCriteriaConstant.CIRCUMFLEX)) {
			issuerPatientIdDependingHl7Syntax = patientId.substring(
					patientId.indexOf(SearchCriteriaConstant.CIRCUMFLEX) + SearchCriteriaConstant.CIRCUMFLEX.length());
		}
		else if (patientId.contains(SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX)) {
			issuerPatientIdDependingHl7Syntax = patientId
				.substring(patientId.indexOf(SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX)
						+ SearchCriteriaConstant.ENCRYPTED_CIRCUMFLEX.length());
		}
		return issuerPatientIdDependingHl7Syntax;
	}

}
