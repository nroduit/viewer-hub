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

package org.viewer.hub.back.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.model.property.ConnectorServerProperty;

public class PathUrlUtil {

	/**
	 * Ensure that the path will use / separator even when testing on Windows: used for S3
	 * paths
	 * @param path Path to transform
	 * @return Paths updated
	 */
	public static String pathWithS3Separator(String path) {
		return path != null ? path.replace("\\", "/") : null;
	}

	/**
	 * Build url from server property
	 * @param connectorServerProperty ConnectorServerProperty to evaluate
	 * @return Url built
	 */
	public static String buildUrlFromServerProperty(ConnectorServerProperty connectorServerProperty) {
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
			.fromUriString(connectorServerProperty.getUrl());
		if (StringUtils.isNotBlank(connectorServerProperty.getPort())) {
			uriComponentsBuilder.port(connectorServerProperty.getPort());
		}
		if (StringUtils.isNotBlank(connectorServerProperty.getContext())) {
			uriComponentsBuilder.path(connectorServerProperty.getContext());
		}
		return uriComponentsBuilder.build().toUriString();
	}

}
