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

package org.viewer.hub.front.components;

import com.vaadin.flow.component.Component;

public class UIUtil {

	private UIUtil() {
		// Private constructor to hide implicit one
	}

	/**
	 * Set tooltip on component
	 * @param component to modify
	 * @param tooltipText tool tip to add
	 */
	public static void setTooltip(Component component, String tooltipText) {
		component.getElement().setAttribute("title", tooltipText);
	}

}
