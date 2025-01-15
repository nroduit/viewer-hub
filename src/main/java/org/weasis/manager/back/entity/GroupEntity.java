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
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity for the table groups.
 */
@Entity
@Table(name = "groups")
public class GroupEntity implements Serializable {

	private static final long serialVersionUID = 8313794009353457388L;

	private GroupEntityPK groupEntityPK;

	@AttributeOverrides(value = { @AttributeOverride(name = "groupId", column = @Column(name = "group_id")),
			@AttributeOverride(name = "memberId", column = @Column(name = "member_id")) })
	@EmbeddedId
	@JsonGetter("association")
	public GroupEntityPK getGroupEntityPK() {
		return this.groupEntityPK;
	}

	public void setGroupEntityPK(GroupEntityPK groupEntityPK) {
		this.groupEntityPK = groupEntityPK;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		GroupEntity that = (GroupEntity) o;
		return Objects.equals(this.groupEntityPK, that.groupEntityPK);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.groupEntityPK);
	}

}
