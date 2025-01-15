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

package org.weasis.manager.back.model.weasis;

import lombok.extern.slf4j.Slf4j;
import org.weasis.manager.back.enums.WeasisLevelMessageType;

@Slf4j
public class WeasisMessage {

	private String title;

	private String message;

	private WeasisLevelMessageType level;

	public WeasisMessage(String title, String message, WeasisLevelMessageType level) {
		this.title = title;
		this.message = message;
		this.level = level;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public WeasisLevelMessageType getLevel() {
		return this.level;
	}

	public void setLevel(WeasisLevelMessageType level) {
		this.level = level;
	}

}
