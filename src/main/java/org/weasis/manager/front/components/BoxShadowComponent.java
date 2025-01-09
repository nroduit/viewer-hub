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

package org.weasis.manager.front.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

public class BoxShadowComponent extends Div {

	public BoxShadowComponent(Component... component) {
		this.getElement().getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
		this.getElement().getStyle().set("border-radius", "var(--lumo-border-radius-m)");
		this.getElement().getStyle().set("background-color", "hsla(245, 100%, 100%, 0.03)");
		this.add(component);
	}

}
