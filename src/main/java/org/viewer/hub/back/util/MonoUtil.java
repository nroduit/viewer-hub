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

package org.viewer.hub.back.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public final class MonoUtil {

	/**
	 * Private constructor to hide the implicit one
	 */
	private MonoUtil() {
		// Private constructor to hide the implicit one
	}

	/**
	 * Build Error response
	 * @param message Message
	 * @return Error response
	 */
	public static Function<ClientResponse, Mono<? extends Throwable>> buildMonoError(String message) {
		return clientResponse -> Mono.error(new ResponseStatusException(clientResponse.statusCode(), String
			.format("%s : %s", HttpStatus.valueOf(clientResponse.statusCode().value()).getReasonPhrase(), message)));
	}

}
