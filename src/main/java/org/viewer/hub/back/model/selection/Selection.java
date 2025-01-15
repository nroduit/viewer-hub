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

package org.viewer.hub.back.model.selection;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Selection implements Serializable {

	@Serial
	private static final long serialVersionUID = 3240859869049879904L;

	private List<Series> series;

	private String uuid;

	public List<Series> getSeries() {
		return this.series;
	}

	public void setSeries(List<Series> series) {
		this.series = series;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Selection selection = (Selection) o;
		return Objects.equals(this.series, selection.series) && Objects.equals(this.uuid, selection.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.series, this.uuid);
	}

}
