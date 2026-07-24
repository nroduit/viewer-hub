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

package org.viewer.hub.front.views.weasis.bundle.override.component;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;

import java.io.Serial;

public class PackageVersionFileUpload extends Upload {

	@Serial
	private static final long serialVersionUID = -6171977483444538014L;

	public PackageVersionFileUpload() {
		super();
		this.setDropLabel(new Span("Drag and drop your package version here..."));
	}

}
