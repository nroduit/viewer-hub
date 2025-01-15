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

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class PackageVersionFileUpload extends Upload {

	@Serial
	private static final long serialVersionUID = -6171977483444538014L;

	private MemoryBuffer memoryBuffer;

	public PackageVersionFileUpload() {
		super();
		this.memoryBuffer = new MemoryBuffer();
		this.setReceiver(this.memoryBuffer);
		this.setDropLabel(new Span("Drag and drop your package version here (format weasis-native.zip)"));
	}

}
