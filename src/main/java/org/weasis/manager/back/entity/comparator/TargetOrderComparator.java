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

import org.weasis.manager.back.entity.TargetEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator used to order target by targets types Order define in the enum TargetType
 */
public class TargetOrderComparator implements Comparator<TargetEntity>, Serializable {

	@Serial
	private static final long serialVersionUID = 5417218534731647908L;

	@Override
	public int compare(TargetEntity t1, TargetEntity t2) {
		return Integer.compare(t1.getType().getOrder(), t2.getType().getOrder());
	}

}
