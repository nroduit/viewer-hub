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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity for the table package version.
 */
@Entity
@Table(name = "package_version")
public class PackageVersionEntity extends AuditEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 8209781345903940843L;

	private Long id;

	private String versionNumber;

	private String qualifier;

	private String i18nVersion;

	private String description;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Schema(description = "Id of the package version")
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVersionNumber() {
		return this.versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getQualifier() {
		return this.qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public String getI18nVersion() {
		return this.i18nVersion;
	}

	public void setI18nVersion(String i18nVersion) {
		this.i18nVersion = i18nVersion;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		PackageVersionEntity that = (PackageVersionEntity) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.versionNumber, that.versionNumber)
				&& Objects.equals(this.qualifier, that.qualifier) && Objects.equals(this.i18nVersion, that.i18nVersion)
				&& Objects.equals(this.description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.versionNumber, this.qualifier, this.i18nVersion, this.description);
	}

	@Override
	public String toString() {
		return "PackageVersionEntity{" + "id=" + this.id + ", versionNumber='" + this.versionNumber + '\''
				+ ", qualifier='" + this.qualifier + '\'' + ", i18nVersion='" + this.i18nVersion + '\''
				+ ", description='" + this.description + '\'' + '}';
	}

}
