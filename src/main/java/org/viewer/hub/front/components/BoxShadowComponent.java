/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.front.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

public class BoxShadowComponent extends Div {

	public BoxShadowComponent(Component... component) {
		this.getElement()
			.getStyle()
			.set("box-shadow", "0 1px 4px 0 color-mix(in srgb, var(--vaadin-text-color) 15%, transparent)");
		this.getElement().getStyle().set("border-radius", "var(--vaadin-radius-m, 0.5rem)");
		this.getElement().getStyle().set("background-color", "hsla(245, 100%, 100%, 0.03)");
		this.add(component);
	}

}
