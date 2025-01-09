/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.model.manifest;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {

	@Serial
	private static final long serialVersionUID = -8487414037601387790L;

	private String title;

	private String description;

	private ErrorSeverity errorSeverity;

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ErrorSeverity getErrorSeverity() {
		return this.errorSeverity;
	}

	public void setErrorSeverity(ErrorSeverity errorSeverity) {
		this.errorSeverity = errorSeverity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Message message = (Message) o;
		return Objects.equals(this.title, message.title) && Objects.equals(this.description, message.description)
				&& this.errorSeverity == message.errorSeverity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.title, this.description, this.errorSeverity);
	}

	@Override
	public String toString() {
		return "Message{" + "title='" + this.title + '\'' + ", description='" + this.description + '\''
				+ ", errorSeverity=" + this.errorSeverity + '}';
	}

}
