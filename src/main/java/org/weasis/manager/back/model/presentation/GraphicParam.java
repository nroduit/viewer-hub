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

package org.weasis.manager.back.model.presentation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class GraphicParam implements Serializable {

	@Serial
	private static final long serialVersionUID = 4822275955415308137L;

	private Paint paint;

	private GraphicLabel graphicLabel;

	private Layer layer;

	private Pts pts;

	public Paint getPaint() {
		return this.paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	public GraphicLabel getGraphicLabel() {
		return this.graphicLabel;
	}

	public void setGraphicLabel(GraphicLabel graphicLabel) {
		this.graphicLabel = graphicLabel;
	}

	public Layer getLayer() {
		return this.layer;
	}

	public void setLayer(Layer layer) {
		this.layer = layer;
	}

	public Pts getPts() {
		return this.pts;
	}

	public void setPts(Pts pts) {
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
		GraphicParam that = (GraphicParam) o;
		return Objects.equals(this.paint, that.paint) && Objects.equals(this.graphicLabel, that.graphicLabel)
				&& Objects.equals(this.layer, that.layer) && Objects.equals(this.pts, that.pts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.paint, this.graphicLabel, this.layer, this.pts);
	}

}
