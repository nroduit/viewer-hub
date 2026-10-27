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

package org.viewer.hub.back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for the table display_request_monitoring: one row per viewer launch request
 * (monitoring).
 */
@Entity
@Table(name = "display_request_monitoring")
@Getter
@Setter
public class DisplayRequestMonitoringEntity {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "display_request_monitoring_seq")
	@SequenceGenerator(name = "display_request_monitoring_seq", sequenceName = "display_request_monitoring_seq",
			allocationSize = 50)
	private Long id;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "viewer", length = 100)
	private String viewer;

	@Column(name = "request_type", length = 20)
	private String requestType;

	@Column(name = "query_level", length = 20)
	private String queryLevel;

	@Column(name = "http_method", length = 10)
	private String httpMethod;

	@Column(name = "authenticated", nullable = false)
	private boolean authenticated;

	@Column(name = "jwt_user")
	private String jwtUser;

	@Column(name = "request_user")
	private String requestUser;

	@Column(name = "host")
	private String host;

	@Column(name = "client")
	private String client;

	@Column(name = "selection_rule", length = 500)
	private String selectionRule;

	@Column(name = "archive", length = 1000)
	private String archive;

	@Column(name = "patient_id", length = 1000)
	private String patientId;

	@Column(name = "study_uid", length = 2000)
	private String studyUid;

	@Column(name = "series_uid", length = 2000)
	private String seriesUid;

	@Column(name = "object_uid", length = 2000)
	private String objectUid;

	@Column(name = "accession_number", length = 1000)
	private String accessionNumber;

	@Column(name = "duration_ms", nullable = false)
	private long durationMs;

	@Column(name = "success", nullable = false)
	private boolean success;

	@Column(name = "error_message", length = 1000)
	private String errorMessage;

}
