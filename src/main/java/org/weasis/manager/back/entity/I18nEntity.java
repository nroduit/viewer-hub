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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity for the table i18n.
 */
@Entity
@Table(name = "i18n")
@Getter
@Setter
@ToString
public class I18nEntity extends AuditEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = -1074952224312969585L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Schema(description = "Id of the i18n version")
	private Long id;

	private String versionNumber;

	private String qualifier;

	private String description;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		I18nEntity that = (I18nEntity) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.versionNumber, that.versionNumber)
				&& Objects.equals(this.qualifier, that.qualifier) && Objects.equals(this.description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.versionNumber, this.qualifier, this.description);
	}

}
