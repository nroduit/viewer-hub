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

package org.weasis.manager.back.entity.comparator;

import org.weasis.manager.back.entity.LaunchEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator used to order launch entities by targets types Order define in the enum
 * TargetType
 */
public class LaunchByTargetOrderComparator implements Comparator<LaunchEntity>, Serializable {

	@Serial
	private static final long serialVersionUID = -2948077579876115569L;

	@Override
	public int compare(LaunchEntity l1, LaunchEntity l2) {
		return Integer.compare(l1.getAssociatedTarget().getType().getOrder(),
				l2.getAssociatedTarget().getType().getOrder());
	}

}
