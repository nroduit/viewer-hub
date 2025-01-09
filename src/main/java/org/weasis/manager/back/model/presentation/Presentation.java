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
import java.util.List;
import java.util.Objects;

public class Presentation implements Serializable {

	@Serial
	private static final long serialVersionUID = 8459161770442805379L;

	private List<PresentationParam> presentationParams;

	private String uuid;

	public List<PresentationParam> getPresentationParams() {
		return this.presentationParams;
	}

	public void setPresentationParams(List<PresentationParam> presentationParams) {
		this.presentationParams = presentationParams;
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
		Presentation that = (Presentation) o;
		return Objects.equals(this.presentationParams, that.presentationParams) && Objects.equals(this.uuid, that.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.presentationParams, this.uuid);
	}

}
