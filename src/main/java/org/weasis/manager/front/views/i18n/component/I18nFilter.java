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
package org.weasis.manager.front.views.i18n.component;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Filter for I18n version
 */
@Getter
@Setter
public class I18nFilter {

	private String i18nVersion;

	public I18nFilter() {
		this.i18nVersion = "";
	}

	public boolean hasFilter() {
		return StringUtils.isNotBlank(this.i18nVersion);
	}

}
