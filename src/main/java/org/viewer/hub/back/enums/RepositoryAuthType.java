/*
 * Copyright 2014-2024 the original author or authors.
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

package org.viewer.hub.back.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Define the type of connection to use when connecting to the nexus repository
 */
@Getter
public enum RepositoryAuthType {

	// Basic authentication
	BASIC("basic"),
	// No authentication
	NONE("none");

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Constructor
	 * @param code Code of the enum
	 */
	RepositoryAuthType(final String code) {
		this.code = code;
	}

	/**
	 * Get the enum from the code in parameter
	 * @param code Code of the enum
	 * @return RepositoryAuthType found
	 */
	public static RepositoryAuthType fromCode(final String code) {
		if (code != null) {
			return Arrays.stream(RepositoryAuthType.values())
				.filter(repositoryAuthType -> code.trim().equalsIgnoreCase(repositoryAuthType.getCode()))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

}
