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

package org.viewer.hub.back.model.presentation;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Pts implements Serializable {

	@Serial
	private static final long serialVersionUID = -4803495697453126335L;

	private List<Pt> pts;

	public List<Pt> getPts() {
		return this.pts;
	}

	public void setPts(List<Pt> pts) {
		this.pts = pts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Pts pts1 = (Pts) o;
		return Objects.equals(this.pts, pts1.pts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.pts);
	}

}
