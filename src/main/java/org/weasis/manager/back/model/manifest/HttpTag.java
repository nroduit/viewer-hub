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

package org.weasis.manager.back.model.manifest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class HttpTag implements Serializable {

	@Serial
	private static final long serialVersionUID = -183104978124255949L;

	@JacksonXmlProperty(isAttribute = true)
	private String key;

	@JacksonXmlProperty(isAttribute = true)
	private String value;

	public HttpTag(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		HttpTag httpTag = (HttpTag) o;
		return Objects.equals(this.key, httpTag.key) && Objects.equals(this.value, httpTag.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.key, this.value);
	}

	@Override
	public String toString() {
		return "HttpTag{" + "key='" + this.key + '\'' + ", value='" + this.value + '\'' + '}';
	}

}
