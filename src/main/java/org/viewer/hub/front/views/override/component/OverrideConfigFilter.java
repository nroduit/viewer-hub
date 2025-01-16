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
package org.viewer.hub.front.views.override.component;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Filter for Override config
 */
@Getter
@Setter
public class OverrideConfigFilter {

	private String packageVersion;

	private String launchConfig;

	private String group;

	public OverrideConfigFilter() {
		this.packageVersion = "";
		this.launchConfig = "";
		this.group = "";
	}

	public boolean hasFilter() {
		return StringUtils.isNotBlank(this.packageVersion) || StringUtils.isNotBlank(this.launchConfig)
				|| StringUtils.isNotBlank(this.group);
	}

}
