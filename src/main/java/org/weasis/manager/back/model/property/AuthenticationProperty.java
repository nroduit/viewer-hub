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

package org.weasis.manager.back.model.property;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * Manage the wado authentication in order to retrieve images from manifest in Weasis
 */
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationProperty {

	// Used to force usage of basic authentication parameters to retrieve images (even if
	// request is authenticated)
	@NotNull
	private Boolean forceBasic;

	// If request is authenticated: retrieve the token from the authenticated request and
	// inject it in the manifest for Weasis to get the images
	@NotNull
	private OAuth2AuthenticationProperty oauth2;

	// If request is not authenticated use basic authentication to retrieve images with
	// credentials from this property
	@NotNull
	private BasicAuthenticationProperty basic;

}
