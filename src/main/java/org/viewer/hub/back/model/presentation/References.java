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

public class References implements Serializable {

	@Serial
	private static final long serialVersionUID = -2546559956369669488L;

	private List<Series> series;

	public List<Series> getSeries() {
		return this.series;
	}

	public void setSeries(List<Series> series) {
		this.series = series;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		References that = (References) o;
		return Objects.equals(this.series, that.series);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.series);
	}

}
