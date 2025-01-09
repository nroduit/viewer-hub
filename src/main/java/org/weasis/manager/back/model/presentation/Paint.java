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

public class Paint implements Serializable {

	@Serial
	private static final long serialVersionUID = 7677716039556427848L;

	private String rgb;

	public String getRgb() {
		return this.rgb;
	}

	public void setRgb(String rgb) {
		this.rgb = rgb;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Paint paint = (Paint) o;
		return Objects.equals(this.rgb, paint.rgb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.rgb);
	}

}
