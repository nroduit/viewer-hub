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

package org.viewer.hub.back.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.entity.converter.ViewerTypeConverter;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity for the table viewer_association.
 */
@Entity
@Table(name = "viewer_selection")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ViewerSelectionEntity {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JacksonXmlProperty(localName = "Id")
	@Schema(description = "Id of viewer selection rule")
	private Long id;

	@Basic
	@Column(name = "priority", nullable = false, unique = true)
	@JacksonXmlProperty(localName = "Priority")
	@Schema(description = "Priority of the viewer selection rule")
	@NotNull
	public Integer priority;

	@Basic
	@Column(name = "archive", length = 100)
	@JacksonXmlProperty(localName = "Archive")
	@Schema(description = "Archive name")
	private String archive;

	@ElementCollection(targetClass = ModalityType.class, fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "viewer_selection_modalities", joinColumns = @JoinColumn(name = "viewer_selection_id"))
	@Column(name = "modality")
	@JacksonXmlProperty(localName = "Modality")
	@Schema(description = "List of modalities")
	private List<ModalityType> modalities = new ArrayList<>();

	@Basic
	@Column(name = "viewer", nullable = false, length = 100)
	@Convert(converter = ViewerTypeConverter.class)
	@JacksonXmlProperty(localName = "Viewer")
	@Schema(description = "ViewerType")
	@NotNull
	public ViewerType viewer;

}
