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

package org.weasis.manager.back.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.weasis.manager.back.entity.converter.TargetTypeConverter;
import org.weasis.manager.back.enums.TargetType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity for the table Target.
 */
@Entity
@Table(name = "target")
public class TargetEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = -1699303279218857036L;

	private Long id;

	private String name;

	private TargetType type;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JacksonXmlProperty(localName = "Id")
	@Schema(description = "Id of the target")
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "name", nullable = false, length = 100)
	@JacksonXmlProperty(localName = "Name")
	@Schema(description = "Name of the target")
	@NotBlank
	public String getName() {
		return this.name != null ? this.name.trim().toUpperCase() : null;
	}

	public void setName(String name) {
		this.name = name != null ? name.trim().toUpperCase() : null;
	}

	@Basic
	@Column(name = "type", nullable = false, length = 100)
	@Convert(converter = TargetTypeConverter.class)
	@JacksonXmlProperty(localName = "Type")
	@Schema(description = "Type of the target")
	@NotNull
	public TargetType getType() {
		return this.type;
	}

	public void setType(TargetType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		TargetEntity that = (TargetEntity) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name)
				&& Objects.equals(this.type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.name, this.type);
	}

	@Override
	public String toString() {
		return "Target {" + "name='" + this.name + '\'' + ", type=" + this.type + '}';
	}

}
