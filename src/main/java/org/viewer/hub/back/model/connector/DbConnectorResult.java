/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.model.connector;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model which will contains db results
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DbConnectorResult {

	// Patient
	private String patientName;

	private String patientId;

	private LocalDate patientBirthDate;

	private String patientSex;

	// Study
	private String studyInstanceUid;

	private String studyId;

	private LocalDateTime studyDateTime;

	private String accessionNumber;

	private String referringPhysicianName;

	private String studyDescription;

	// Serie
	private String seriesInstanceUid;

	private String modality;

	private LocalDateTime seriesDateTime;

	private String seriesDescription;

	private Integer seriesNumber;

	// Instance
	private String sopInstanceUid;

	private String sopClassUid;

	private Integer instanceNumber;

}
