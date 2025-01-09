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

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class I18nUpload extends HorizontalLayout {

	// Components
	private I18nFileUpload i18nFileUpload;

	public I18nUpload() {
		// File upload
		this.i18nFileUpload = new I18nFileUpload();
		this.i18nFileUpload.setWidth("50%");

		// Add components
		this.setAlignItems(Alignment.CENTER);
		this.add(this.i18nFileUpload);
	}

}
