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

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for the table Preference.
 */
@Entity
@Table(name = "preference")
public class PreferenceEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = -451138141524988372L;

	private Long id;

	@Valid
	private TargetEntity target;

	private String content;

	private LocalDateTime creationDate;

	private LocalDateTime updateDate;

	private ModuleEntity module;

	private ProfileEntity profile;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long weaPrefNId) {
		this.id = weaPrefNId;
	}

	@ManyToOne
	@JoinColumn(name = "target_id")
	public TargetEntity getTarget() {
		return this.target;
	}

	public void setTarget(@Valid TargetEntity target) {
		this.target = target;
	}

	@Basic
	@Column(name = "content")
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Basic
	@Column(name = "creation_date")
	public LocalDateTime getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	@Basic
	@Column(name = "update_date")
	public LocalDateTime getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@ManyToOne
	@JoinColumn(name = "module_id")
	public ModuleEntity getModule() {
		return this.module;
	}

	public void setModule(ModuleEntity module) {
		this.module = module;
	}

	@ManyToOne
	@JoinColumn(name = "profile_id")
	public ProfileEntity getProfile() {
		return this.profile;
	}

	public void setProfile(ProfileEntity profile) {
		this.profile = profile;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		PreferenceEntity that = (PreferenceEntity) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.module, that.module)
				&& Objects.equals(this.profile, that.profile) && Objects.equals(this.target, that.target)
				&& Objects.equals(this.content, that.content) && Objects.equals(this.creationDate, that.creationDate)
				&& Objects.equals(this.updateDate, that.updateDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.target, this.module, this.content, this.profile, this.creationDate,
				this.updateDate);
	}

}
