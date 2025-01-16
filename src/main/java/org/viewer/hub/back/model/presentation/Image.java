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
import java.util.Objects;

public class Image implements Serializable {

	@Serial
	private static final long serialVersionUID = -6920111597427444569L;

	private String uuid;

	private String frames;

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFrames() {
		return this.frames;
	}

	public void setFrames(String frames) {
		this.frames = frames;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Image image = (Image) o;
		return Objects.equals(this.uuid, image.uuid) && Objects.equals(this.frames, image.frames);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uuid, this.frames);
	}

}
