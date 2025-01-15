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

package org.weasis.manager.back.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import org.weasis.manager.back.entity.LaunchEntity;

import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "Launches")
public class Launches {

	@Schema(description = "Logging level of the message", name = "level", type = "MessageLevel", example = "INFO")
	private List<LaunchEntity> launchEntities;

	public Launches(List<LaunchEntity> launchEntities) {
		this.launchEntities = launchEntities;
	}

	@JacksonXmlProperty(localName = "Launch")
	@JacksonXmlElementWrapper(useWrapping = false)
	public List<LaunchEntity> getLaunchEntities() {
		return this.launchEntities;
	}

	public void setLaunchEntities(List<LaunchEntity> launchEntities) {
		this.launchEntities = launchEntities;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Launches launches = (Launches) o;
		return Objects.equals(this.launchEntities, launches.launchEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.launchEntities);
	}

}
