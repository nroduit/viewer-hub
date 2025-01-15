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
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Embeddable key for the table override_config.
 */
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverrideConfigEntityPK implements Serializable {

	@Serial
	private static final long serialVersionUID = 7254948679073477475L;

	@Schema(description = "Id of the package version to override")
	@Column(name = "package_version_id")
	private Long packageVersionId;

	@Schema(description = "Id of the launch config to override")
	@Column(name = "launch_config_id")
	private Long launchConfigId;

	@Schema(description = "Id of the group to override")
	@Column(name = "target_id")
	private Long targetId;

	public Long getLaunchConfigId() {
		return this.launchConfigId;
	}

	public void setLaunchConfigId(Long launchConfigId) {
		this.launchConfigId = launchConfigId;
	}

	public Long getTargetId() {
		return this.targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public Long getPackageVersionId() {
		return this.packageVersionId;
	}

	public void setPackageVersionId(Long packageVersionId) {
		this.packageVersionId = packageVersionId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		OverrideConfigEntityPK that = (OverrideConfigEntityPK) o;
		return Objects.equals(this.packageVersionId, that.packageVersionId)
				&& Objects.equals(this.launchConfigId, that.launchConfigId)
				&& Objects.equals(this.targetId, that.targetId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.packageVersionId, this.launchConfigId, this.targetId);
	}

}
