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

package org.viewer.hub.front.views.association.component;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import org.viewer.hub.back.entity.LaunchEntity;

import java.util.Collection;
import java.util.Objects;

/**
 * Grid which represents the Launch/LaunchConfig/LaunchPrefered
 */
@CssImport(value = "./styles/launch-grid.css", themeFor = "vaadin-grid")
public class LaunchGrid extends Grid<LaunchEntity> {

	private static final long serialVersionUID = -7613310574871406994L;

	/**
	 * Constructor
	 */
	public LaunchGrid() {
		// Build columns
		// Config name
		this.addColumnConfigName();
		// Prefered name
		this.addColumnPreferedName();
		// Prefered type
		this.addColumnPreferedType();
		// Value
		this.addColumnValue();
	}

	/**
	 * Add column config name
	 * @return column built
	 */
	private Column<LaunchEntity> addColumnConfigName() {
		return this.addColumn(launch -> launch.getAssociatedConfig().getName())
			.setHeader("Config Name")
			.setWidth("22%")
			.setSortable(true)
			.setKey("configNameColumn");
	}

	/**
	 * Add column prefered name
	 * @return column built
	 */
	private Column<LaunchEntity> addColumnPreferedName() {
		return this.addColumn(launch -> launch.getAssociatedPreferred().getName())
			.setHeader("Prefered Name")
			.setWidth("22%")
			.setSortable(true)
			.setKey("preferedNameColumn");
	}

	/**
	 * Add column prefered type
	 * @return column built
	 */
	private Column<LaunchEntity> addColumnPreferedType() {
		return this.addColumn(launch -> launch.getAssociatedPreferred().getType())
			.setHeader("Prefered Type")
			.setWidth("22%")
			.setSortable(true)
			.setKey("preferedTypeColumn");
	}

	/**
	 * Add column value
	 * @return column built
	 */
	private Column<LaunchEntity> addColumnValue() {
		return this.addColumn(LaunchEntity::getSelection)
			.setHeader("Value")
			.setWidth("22%")
			.setSortable(true)
			.setKey("valueColumn");
	}

	/**
	 * Apply a css style to the rows corresponding to duplicates
	 * @param launchesDuplicate Duplicate launches to compare
	 */
	public void applyDuplicateRowStyle(Collection<LaunchEntity> launchesDuplicate) {
		this.setClassNameGenerator(launchEntity -> {
			if (launchesDuplicate.stream()
				.anyMatch(l -> Objects.equals(l.getAssociatedConfig(), launchEntity.getAssociatedConfig())
						&& Objects.equals(l.getAssociatedPreferred(), launchEntity.getAssociatedPreferred()))) {
				return "duplicate";
			}
			else {
				return null;
			}
		});
	}

}
