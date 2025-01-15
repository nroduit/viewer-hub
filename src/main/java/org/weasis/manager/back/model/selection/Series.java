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

public class Series implements Serializable {

	@Serial
	private static final long serialVersionUID = -914499300597799790L;

	private List<Image> images;

	private String uuid;

	public List<Image> getImages() {
		return this.images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
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
		Series series = (Series) o;
		return Objects.equals(this.images, series.images) && Objects.equals(this.uuid, series.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.images, this.uuid);
	}

}
