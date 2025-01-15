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

package org.weasis.manager.back.model.property;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.weasis.manager.back.enums.QueryLevelType;

import java.util.Set;

@Getter
@Setter
@Validated
@EqualsAndHashCode
@AllArgsConstructor
public class SearchCriteriaProperty {

	@NotNull
	// Used to deactivate some search criteria levels
	private Set<QueryLevelType> deactivated;

}
