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

package org.viewer.hub.back.constant;

public final class ParamName {

	public static final String PRO = "pro";

	public static final String USER = "user";

	public static final String HOST = "host";

	public static final String EXT_CFG = "ext-cfg";

	public static final String CONFIG = "config";

	public static final String PATIENT_ID = "patientID";

	public static final String REQUEST_TYPE = "requestType";

	public static final String ARCHIVE = "archive";

	public static final String KEY = "key";

	// Dicom-web
	public static final String DICOM_WEB_SERIES_INSTANCE_UID = "SeriesInstanceUID";

	public static final String DICOM_WEB_ACCESSION_NUMBER = "AccessionNumber";

	public static final String DICOM_WEB_PATIENT_ID = "PatientID";

	public static final String DICOM_WEB_ISSUER_OF_PATIENT_ID = "IssuerOfPatientID";

	public static final String DICOM_WEB_SOP_INSTANCE_UID = "SOPInstanceUID";

	public static final String DICOM_WEB_STUDY_INSTANCE_UID = "StudyInstanceUID";

	public static final String INCLUDE_FIELD = "includefield";

	public static final String INCLUDE_FIELD_INSTANCE_ATTRIBUTES = "StudyInstanceUID,SeriesInstanceUID,SOPInstanceUID,InstanceNumber";

	public static final String INCLUDE_FIELD_SERIE_ATTRIBUTES = "StudyInstanceUID,SeriesInstanceUID,SeriesDescription,SeriesNumber,Modality";

	public static final String INCLUDE_FIELD_STUDY_ATTRIBUTES = "StudyInstanceUID,StudyDescription,StudyDate,StudyTime,AccessionNumber,StudyID,ReferringPhysicianName,PatientID,PatientName,IssuerOfPatientID,PatientBirthDate,PatientBirthTime,PatientSex";

	// Dicom-web Pagination
	public static final String LIMIT = "limit";

	public static final String OFFSET = "offset";

	private ParamName() {
		// Private constructor to hide implicit one
	}

}
