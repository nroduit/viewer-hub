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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.entity.converter.TargetViewerTypeConverter;
import org.viewer.hub.back.enums.Viewer;

/**
 * Entity for the table viewer_association.
 */
@Entity
@Table(name = "viewer_association")
@Getter
@Setter
public class ViewerAssociationModel {

    @Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JacksonXmlProperty(localName = "Id")
	@Schema(description = "Id of the target")
	private Long id;

	@Basic
	@Column(name = "priority", nullable = false, unique = true)
	@JacksonXmlProperty(localName = "Priority")
	@Schema(description = "Priority of the target")
	@NotNull
	public Integer priority;

	@Basic
	@Column(name = "archive", length = 100)
	@JacksonXmlProperty(localName = "Archive")
	@Schema(description = "Archive name")
	private String archive;

	@Basic
	@Column(name = "aet", length = 100)
	@JacksonXmlProperty(localName = "AET")
	@Schema(description = "AET")
	public String aet;

	@Basic
	@Column(name = "viewer", nullable = false, length = 100)
	@Convert(converter = TargetViewerTypeConverter.class)
	@JacksonXmlProperty(localName = "Viewer")
	@Schema(description = "Target Viewer")
	@NotNull
	public Viewer viewer;

}
