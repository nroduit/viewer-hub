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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Embeddable key the table groups.
 */
@Embeddable
public class GroupEntityPK implements Serializable {

	@Serial
	private static final long serialVersionUID = 5375828745972704512L;

	@Schema(description = "Id of the group target")
	private Long groupId;

	@Schema(description = "Id of the member target")
	private Long memberId;

	public Long getGroupId() {
		return this.groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getMemberId() {
		return this.memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		GroupEntityPK that = (GroupEntityPK) o;
		return Objects.equals(this.groupId, that.groupId) && Objects.equals(this.memberId, that.memberId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.groupId, this.memberId);
	}

}
