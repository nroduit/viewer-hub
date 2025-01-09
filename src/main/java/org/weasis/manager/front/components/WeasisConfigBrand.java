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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.io.Serial;

public class WeasisConfigBrand extends Composite<Div> {

	@Serial
	private static final long serialVersionUID = -4354338543637288856L;

	private final Div div;

	private final Image logo;

	private final Span text;

	public WeasisConfigBrand() {
		this.div = this.getContent();
		this.div.getStyle().set("display", "contents");

		this.logo = new Image("logo/weasis.svg", "logo");
		this.text = new Span("WeasisConfig");
		this.div.add(this.logo, this.text);
	}

}
