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

package org.viewer.hub.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.enums.TargetType;

/**
 * AssociationModel Filters
 */
@Setter
@Getter
public class AssociationModelFilter {

	@Schema(description = "Name of the target", name = "targetName", type = "String", example = "user-1")
	private String targetName = "";

	@Schema(description = "Type of the target define by TargetType", name = "targetType", type = "TargetType",
			example = "USER")
	private TargetType targetType;

	@Schema(description = "Define the association of the target (group: members, user/host: groups)",
			name = "belongToMemberOf", type = "String", example = "USER-GROUP")
	private String belongToMemberOf = "";

	/**
	 * Check if the row should be filtered
	 * @param associationModel Row to check
	 * @return true if the row should be kept, false otherwise
	 */
	// public boolean checkRow(AssociationModel associationModel) {
	// if (targetName.length() > 0
	// &&
	// !StringUtils.containsIgnoreCase(associationModel.getTarget().getName(),targetName))
	// {
	// return false;
	// }
	//
	// if (targetType != null && !Objects.equals(associationModel.getTarget().getType(),
	// targetType)) {
	// return false;
	// }
	//
	// return belongToMemberOf.length() <= 0 ||
	// associationModel.getBelongToMemberOf().stream().anyMatch(t -> StringUtils
	// .containsIgnoreCase(t.getName(), belongToMemberOf));
	// }

}
