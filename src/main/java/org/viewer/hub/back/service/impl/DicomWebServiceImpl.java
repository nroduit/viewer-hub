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

package org.viewer.hub.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.service.DicomWebService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.apache.catalina.filters.CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN;

@Service
@Slf4j
public class DicomWebServiceImpl implements DicomWebService {

	@Autowired
	public DicomWebServiceImpl() {
	}

	@Override
	public ResponseEntity<?> getResponseFrom(String targetDicomUrl, String contentType) throws IOException, URISyntaxException, InterruptedException {
		HttpResponse<byte[]> response = getArchiveResponse(targetDicomUrl);
		String responseContentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).get();
		if (responseContentType.contains("multipart")) {
			return buildResponse(responseContentType, response.body());
		}
		String body = new String(response.body(), StandardCharsets.UTF_8);
		return buildResponse(contentType, body);
	}

	private HttpResponse<byte[]> getArchiveResponse(String targetDicomUrl) throws URISyntaxException, IOException, InterruptedException {
				HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(targetDicomUrl))
				.GET()
				.build();
		HttpClient client = HttpClient.newHttpClient();
		return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
	}

	private ResponseEntity<String> buildResponse(String contentType, String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		headers.set(HttpHeaders.CONTENT_TYPE, contentType);
		headers.set(HttpHeaders.CONTENT_LENGTH, body.length() + "");
		return ResponseEntity.ok()
				.headers(headers)
				.body(body);
	}

	private ResponseEntity<byte[]> buildResponse(String contentType, byte[] body) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		headers.set(HttpHeaders.CONTENT_TYPE, contentType);
		headers.set(HttpHeaders.CONTENT_LENGTH, body.length + "");
		return ResponseEntity.ok()
				.headers(headers)
				.body(body);
	}

}
