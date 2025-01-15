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

package org.weasis.manager.back.model.selection;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Selections implements Serializable {

	@Serial
	private static final long serialVersionUID = -1462512656138305645L;

	private List<Selection> selections;

	public List<Selection> getSelections() {
		return this.selections;
	}

	public void setSelections(List<Selection> selections) {
		this.selections = selections;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Selections that = (Selections) o;
		return Objects.equals(this.selections, that.selections);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.selections);
	}

}
