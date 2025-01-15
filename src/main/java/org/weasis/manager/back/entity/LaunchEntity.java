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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity for the table Launch.
 */
@Entity
@Table(name = "launch")
public class LaunchEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 2062771078581164014L;

	private LaunchEntityPK launchEntityPK;

	@Valid
	private LaunchConfigEntity associatedConfig;

	@Valid
	private LaunchPreferredEntity associatedPreferred;

	@Valid
	private TargetEntity associatedTarget;

	private String selection;

	@AttributeOverrides(value = { @AttributeOverride(name = "targetId", column = @Column(name = "target_id")),
			@AttributeOverride(name = "launchConfigId", column = @Column(name = "launch_config_id")),
			@AttributeOverride(name = "launchPreferredId", column = @Column(name = "launch_preferred_id")) })
	@EmbeddedId
	// @JacksonXmlProperty(localName = "LaunchKey")
	@JsonIgnore
	public LaunchEntityPK getLaunchEntityPK() {
		return this.launchEntityPK;
	}

	public void setLaunchEntityPK(LaunchEntityPK launchEntityPK) {
		this.launchEntityPK = launchEntityPK;
	}

	@Transient
	@JacksonXmlProperty(localName = "Config")
	@JsonGetter("config")
	public LaunchConfigEntity getAssociatedConfig() {
		return this.associatedConfig;
	}

	@JsonSetter("config")
	public void setAssociatedConfig(@Valid LaunchConfigEntity associatedConfig) {
		this.associatedConfig = associatedConfig;
	}

	@Transient
	@JacksonXmlProperty(localName = "Preferred")
	@JsonGetter("preferred")
	public LaunchPreferredEntity getAssociatedPreferred() {
		return this.associatedPreferred;
	}

	@JsonSetter("preferred")
	public void setAssociatedPreferred(@Valid LaunchPreferredEntity associatedPreferred) {
		this.associatedPreferred = associatedPreferred;
	}

	@Transient
	@JacksonXmlProperty(localName = "Target")
	@JsonGetter("target")
	public TargetEntity getAssociatedTarget() {
		return this.associatedTarget;
	}

	@JsonSetter("target")
	public void setAssociatedTarget(@Valid TargetEntity associatedTarget) {
		this.associatedTarget = associatedTarget;
	}

	@Basic
	@Column(name = "selection", nullable = false, length = 600)
	@JacksonXmlProperty(localName = "Value")
	@JsonGetter("selection")
	public String getSelection() {
		return this.selection;
	}

	@JsonSetter("selection")
	public void setSelection(String selection) {
		this.selection = selection;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		LaunchEntity that = (LaunchEntity) o;
		return Objects.equals(this.launchEntityPK, that.launchEntityPK)
				&& Objects.equals(this.selection, that.selection);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.launchEntityPK, this.selection);
	}

	@Override
	public String toString() {
		return "LaunchEntity{" + "launchEntityPK=" + this.launchEntityPK + ", associatedConfig=" + this.associatedConfig
				+ ", associatedPreferred=" + this.associatedPreferred + ", associatedTarget=" + this.associatedTarget
				+ ", selection='" + this.selection + '\'' + '}';
	}

}
