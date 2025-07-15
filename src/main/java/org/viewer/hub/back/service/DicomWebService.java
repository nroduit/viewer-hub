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

package org.viewer.hub.back.service;

import org.json.JSONException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Service used to launch the application OHIF
 */
public interface DicomWebService {

	ResponseEntity<?> getResponseFrom(String url, String contentType) throws IOException, URISyntaxException, InterruptedException, JSONException;

}
