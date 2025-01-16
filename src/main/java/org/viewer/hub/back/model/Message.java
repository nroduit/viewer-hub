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

package org.viewer.hub.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
public class Message implements Serializable {

	@Serial
	private static final long serialVersionUID = -6985321657421271943L;

	@Schema(description = "Logging level of the message", name = "level", type = "MessageLevel", example = "INFO")
	private MessageLevel level;

	@Schema(description = "Format of the message (TEXT or HTML)", name = "format", type = "MessageFormat",
			example = "HTML")
	private MessageFormat format;

	@Schema(description = "Text of the message", name = "text", type = "String", example = "abcdef")
	private String text;

}
