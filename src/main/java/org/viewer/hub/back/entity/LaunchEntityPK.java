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

package org.viewer.hub.back.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Embedded key for the table Launch.
 */
@Embeddable
public class LaunchEntityPK implements Serializable {

	@Serial
	private static final long serialVersionUID = -5362144636031732832L;

	private Long targetId;

	private Long launchConfigId;

	private Long launchPreferredId;

	@JacksonXmlProperty(localName = "TargetId")
	public Long getTargetId() {
		return this.targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@JacksonXmlProperty(localName = "LaunchConfigId")
	public Long getLaunchConfigId() {
		return this.launchConfigId;
	}

	public void setLaunchConfigId(Long launchConfigId) {
		this.launchConfigId = launchConfigId;
	}

	@JacksonXmlProperty(localName = "LaunchPreferredId")
	public Long getLaunchPreferredId() {
		return this.launchPreferredId;
	}

	public void setLaunchPreferredId(Long launchPreferredId) {
		this.launchPreferredId = launchPreferredId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		LaunchEntityPK that = (LaunchEntityPK) o;
		return Objects.equals(this.targetId, that.targetId) && Objects.equals(this.launchConfigId, that.launchConfigId)
				&& Objects.equals(this.launchPreferredId, that.launchPreferredId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.targetId, this.launchConfigId, this.launchPreferredId);
	}

	@Override
	public String toString() {
		return "LaunchEntityPK{" + "targetId=" + this.targetId + ", launchConfigId=" + this.launchConfigId
				+ ", launchPreferredId=" + this.launchPreferredId + '}';
	}

}
