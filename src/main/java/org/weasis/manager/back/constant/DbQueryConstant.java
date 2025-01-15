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

package org.weasis.manager.back.constant;

/**
 * Constants used by the connector db requests
 */
public class DbQueryConstant {

	public static final String AND = " and ";

	public static final String PARAM_ACCESSION_NUMBERS = "accessionNumbers";

	public static final String PARAM_PATIENT_IDS = "patientIds";

	public static final String PARAM_STUDY_INSTANCE_UIDS = "studyInstanceUids";

	public static final String PARAM_SERIE_INSTANCE_UIDS = "serieInstanceUids";

	public static final String PARAM_SOP_INSTANCE_UIDS = "sopInstanceUids";

	public static final String IN_CLAUSE = " in (:%s)";

	public static final String IN_ACCESSION_NUMBER = IN_CLAUSE.formatted(PARAM_ACCESSION_NUMBERS);

	public static final String IN_PATIENT_ID = IN_CLAUSE.formatted(PARAM_PATIENT_IDS);

	public static final String IN_STUDY_INSTANCE_UID = IN_CLAUSE.formatted(PARAM_STUDY_INSTANCE_UIDS);

	public static final String IN_SERIE_INSTANCE_UID = IN_CLAUSE.formatted(PARAM_SERIE_INSTANCE_UIDS);

	public static final String IN_SOP_INSTANCE_UID = IN_CLAUSE.formatted(PARAM_SOP_INSTANCE_UIDS);

}
