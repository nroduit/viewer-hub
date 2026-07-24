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

package org.viewer.hub.front.views.weasis.i18n.component;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;

import java.io.Serial;

public class I18nFileUpload extends Upload {

	@Serial
	private static final long serialVersionUID = 4685412708026661525L;

	public I18nFileUpload() {
		super();
		this.setDropLabel(new Span("Drag and drop your i18n file here (format weasis-i18n-dist-X.X.X-SNAPSHOT.zip)"));
	}

}
