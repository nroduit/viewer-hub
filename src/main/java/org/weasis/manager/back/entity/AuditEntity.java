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

package org.weasis.manager.back.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audit entity used to follow changes in the database
 */
@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class })
@Getter
@Setter
public class AuditEntity {

	@CreatedDate
	@Column(name = "created_date", updatable = false, columnDefinition = "TIMESTAMP")
	@JsonIgnore
	private LocalDateTime createdDate;

	@LastModifiedDate
	@Column(name = "last_update", columnDefinition = "TIMESTAMP")
	@JsonIgnore
	private LocalDateTime lastUpdate;

	@CreatedBy
	@Column(name = "created_by", updatable = false)
	@JsonIgnore
	private String createdBy;

	@LastModifiedBy
	@Column(name = "modified_by")
	@JsonIgnore
	private String modifiedBy;

}
