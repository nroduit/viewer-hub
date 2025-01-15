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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity for the table Launch_Preferred.
 */
@Entity
@Table(name = "launch_preferred")
public class LaunchPreferredEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = -7558561962605601785L;

	private Long id;

	@NotBlank
	private String name;

	@NotBlank
	private String type;

	@Id
	@Column(name = "id", nullable = false, precision = 0)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JacksonXmlProperty(localName = "Id")
	@Schema(description = "Launch Preferred Id")
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "name", nullable = false, length = 100)
	@JacksonXmlProperty(localName = "Name")
	@Schema(description = "Launch Preferred Name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Basic
	@Column(name = "type", nullable = false, length = 100)
	@JacksonXmlProperty(localName = "Type")
	@Schema(description = "Launch Preferred Type")
	public String getType() {
		return this.type;
	}

	public void setType(String category) {
		this.type = category;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		LaunchPreferredEntity that = (LaunchPreferredEntity) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.type, that.type)
				&& Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.name, this.type);
	}

}
