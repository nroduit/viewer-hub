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

package org.weasis.manager.front.views.override.component;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class PackageVersionUpload extends HorizontalLayout {

	// Components
	private PackageVersionFileUpload packageVersionFileUpload;

	public PackageVersionUpload() {
		// File upload
		this.packageVersionFileUpload = new PackageVersionFileUpload();
		this.packageVersionFileUpload.setWidth("100%");

		// Add components
		this.setAlignItems(FlexComponent.Alignment.CENTER);
		this.add(this.packageVersionFileUpload);
	}

	public PackageVersionFileUpload getPackageVersionFileUpload() {
		return this.packageVersionFileUpload;
	}

	public void setPackageVersionFileUpload(PackageVersionFileUpload packageVersionFileUpload) {
		this.packageVersionFileUpload = packageVersionFileUpload;
	}

}
