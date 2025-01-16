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

public class Graphic implements Serializable {

	@Serial
	private static final long serialVersionUID = 4008442018627556826L;

	private List<GraphicParam> graphicParams;

	private String uuid;

	private int classId;

	private boolean fill;

	private boolean showLabel;

	private float thickness;

	public List<GraphicParam> getGraphicParams() {
		return this.graphicParams;
	}

	public void setGraphicParams(List<GraphicParam> graphicParams) {
		this.graphicParams = graphicParams;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getClassId() {
		return this.classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public boolean isFill() {
		return this.fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public boolean isShowLabel() {
		return this.showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}

	public float getThickness() {
		return this.thickness;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Graphic graphic = (Graphic) o;
		return this.classId == graphic.classId && this.fill == graphic.fill && this.showLabel == graphic.showLabel
				&& Float.compare(graphic.thickness, this.thickness) == 0
				&& Objects.equals(this.graphicParams, graphic.graphicParams) && Objects.equals(this.uuid, graphic.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.graphicParams, this.uuid, this.classId, this.fill, this.showLabel, this.thickness);
	}

}
