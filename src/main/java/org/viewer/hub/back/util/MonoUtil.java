/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
