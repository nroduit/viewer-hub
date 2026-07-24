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

package org.viewer.hub.back.entity.serializer;

import org.viewer.hub.back.entity.WeasisPropertyEntity;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.io.Serial;

public class WeasisPropertyEntitySerializer extends StdSerializer<WeasisPropertyEntity> {

	@Serial
	private static final long serialVersionUID = -3144678306399087431L;

	public WeasisPropertyEntitySerializer(Class<WeasisPropertyEntity> t) {
		super(t);
	}

	@Override
	public void serialize(WeasisPropertyEntity value, JsonGenerator jgen, SerializationContext context) {
		if (value != null && value.getCode() != null) {
			jgen.writeStartObject();
			jgen.writeStringProperty(value.getCode(), value.getValue());
			jgen.writeEndObject();
		}
	}

}
