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
package org.viewer.hub.front.views.bundle.override.component;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * Event used to launch the refresh of the package override grid
 */
public class RefreshPackageGridEvent extends ApplicationEvent {

	@Serial
	private static final long serialVersionUID = 6195412109647298848L;

	public RefreshPackageGridEvent() {
		super(true);
	}

}
