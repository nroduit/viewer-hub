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

package org.weasis.manager.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.weasis.manager.back.entity.TargetEntity;

import java.util.List;

/**
 * Model used for display in the vaadin grid
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class AssociationModel {

	@Valid
	@Schema(description = "Target: User/Host/UserGroup/HostGroup", name = "target", type = "TargetEntity")
	private TargetEntity target;

	@Valid
	@Schema(description = "Users/Hosts if target is a group or UserGroups/HostGroups if target is a user",
			name = "belongToMemberOf", type = "List<TargetEntity>")
	private List<TargetEntity> belongToMemberOf;

	public void setTarget(@Valid TargetEntity target) {
		this.target = target;
	}

	public void setBelongToMemberOf(@Valid List<TargetEntity> belongToMemberOf) {
		this.belongToMemberOf = belongToMemberOf;
	}

}
