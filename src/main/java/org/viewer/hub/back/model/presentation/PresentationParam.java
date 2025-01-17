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

public class PresentationParam implements Serializable {

	@Serial
	private static final long serialVersionUID = -4165170190334926070L;

	private References references;

	private Layers layers;

	private Graphics graphics;

	public References getReferences() {
		return this.references;
	}

	public void setReferences(References references) {
		this.references = references;
	}

	public Layers getLayers() {
		return this.layers;
	}

	public void setLayers(Layers layers) {
		this.layers = layers;
	}

	public Graphics getGraphics() {
		return this.graphics;
	}

	public void setGraphics(Graphics graphics) {
		this.graphics = graphics;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		PresentationParam that = (PresentationParam) o;
		return Objects.equals(this.references, that.references) && Objects.equals(this.layers, that.layers)
				&& Objects.equals(this.graphics, that.graphics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.references, this.layers, this.graphics);
	}

}
