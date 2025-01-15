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

package org.weasis.manager.back.entity.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.weasis.manager.back.controller.exception.TechnicalException;
import org.weasis.manager.back.entity.WeasisPropertyEntity;

import java.io.IOException;
import java.io.Serial;

public class WeasisPropertyEntitySerializer extends StdSerializer<WeasisPropertyEntity> {

	@Serial
	private static final long serialVersionUID = -3144678306399087431L;

	public WeasisPropertyEntitySerializer(Class<WeasisPropertyEntity> t) {
		super(t);
	}

	@Override
	public void serialize(WeasisPropertyEntity value, JsonGenerator jgen, SerializerProvider provider) {
		try {
			if (value != null && value.getCode() != null) {
				jgen.writeStartObject();
				jgen.writeStringField(value.getCode(), value.getValue());
				jgen.writeEndObject();
			}
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when serializing WeasisPropertyEntity in properties format%s".formatted(e.getMessage()));
		}
	}

}
