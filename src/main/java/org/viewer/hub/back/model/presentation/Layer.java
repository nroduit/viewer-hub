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

public class Layer implements Serializable {

	@Serial
	private static final long serialVersionUID = -4989856695509727871L;

	private String uuid;

	private String type;

	private String locked;

	private String visible;

	private String level;

	private String name;

	private String selectable;

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocked() {
		return this.locked;
	}

	public void setLocked(String locked) {
		this.locked = locked;
	}

	public String getVisible() {
		return this.visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

	public String getLevel() {
		return this.level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSelectable() {
		return this.selectable;
	}

	public void setSelectable(String selectable) {
		this.selectable = selectable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Layer layer = (Layer) o;
		return Objects.equals(this.uuid, layer.uuid) && Objects.equals(this.type, layer.type)
				&& Objects.equals(this.locked, layer.locked) && Objects.equals(this.visible, layer.visible)
				&& Objects.equals(this.level, layer.level) && Objects.equals(this.name, layer.name)
				&& Objects.equals(this.selectable, layer.selectable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uuid, this.type, this.locked, this.visible, this.level, this.name, this.selectable);
	}

}
