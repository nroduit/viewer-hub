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

package org.weasis.manager.back.model.presentation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Pt implements Serializable {

	@Serial
	private static final long serialVersionUID = -832696438938670635L;

	private double x;

	private double y;

	public double getX() {
		return this.x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return this.y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Pt pt = (Pt) o;
		return Double.compare(pt.x, this.x) == 0 && Double.compare(pt.y, this.y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.y);
	}

}
