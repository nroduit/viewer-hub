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
package org.viewer.hub.front.views.override.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.front.components.UIUtil;
import org.viewer.hub.front.views.override.OverrideDataProvider;

import java.io.Serial;

/**
 * Grid for the package override view
 */
@Getter
@Setter
public class PackageOverrideGrid extends Grid<OverrideConfigEntity> {

	@Serial
	private static final long serialVersionUID = 846447925124975065L;

	// Tooltips
	public static final String TOOLTIP_FILTER_BY_PACKAGE_VERSION = "Filter by package version";

	public static final String TOOLTIP_FILTER_BY_LAUNCH_CONFIG = "Filter by launch config";

	public static final String TOOLTIP_FILTER_BY_GROUP = "Filter by group";

	// Filter grid rows
	private OverrideConfigFilter overrideConfigFilter;

	// DataProvider
	private final OverrideDataProvider<OverrideConfigEntity> overrideDataProvider;

	private OverrideConfigGridItemDetail overrideConfigGridItemDetail;

	/**
	 * Constructor
	 * @param overrideDataProvider Data provider for override configs
	 */
	public PackageOverrideGrid(OverrideDataProvider<OverrideConfigEntity> overrideDataProvider) {
		super();
		this.overrideDataProvider = overrideDataProvider;

		// Set size for the grid
		this.setWidthFull();
		this.setHeight(86, Unit.PERCENTAGE);

		// Themes of the grid
		this.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

		// Build columns
		// Package version
		Column<OverrideConfigEntity> packageVersionColumn = this.addColumnPackageVersion();
		// Launch Config
		Column<OverrideConfigEntity> launchConfigColumn = this.addColumnLaunchConfig();
		// Group
		Column<OverrideConfigEntity> groupColumn = this.addColumnGroup();

		// Item Details
		this.setItemDetailsRenderer(this.createOverrideConfigDetails());

		// Create filters on rows
		this.createFiltersOnRows(packageVersionColumn, launchConfigColumn, groupColumn);
	}

	/**
	 * Create the renderer of the override config details
	 * @return the renderer created
	 */
	private ComponentRenderer<OverrideConfigGridItemDetail, OverrideConfigEntity> createOverrideConfigDetails() {
		this.overrideConfigGridItemDetail = new OverrideConfigGridItemDetail(
				this.overrideDataProvider.getOverrideLogic().getOverrideView());
		return new ComponentRenderer<>(() -> this.overrideConfigGridItemDetail,
				OverrideConfigGridItemDetail::buildDetailsToDisplay);
	}

	/**
	 * Create filters
	 * @param packageVersionColumn Package Version Column
	 * @param launchConfigColumn launch config Column
	 * @param groupColumn group Column
	 */
	private void createFiltersOnRows(Column<OverrideConfigEntity> packageVersionColumn,
			Column<OverrideConfigEntity> launchConfigColumn, Column<OverrideConfigEntity> groupColumn) {
		// Filters
		HeaderRow filterRow = this.appendHeaderRow();
		this.overrideConfigFilter = new OverrideConfigFilter();

		// Package version filter
		this.createPackageVersionFilter(packageVersionColumn, filterRow);
		// Launch config filter
		this.createLaunchConfigFilter(launchConfigColumn, filterRow);
		// Group filter
		this.createGroupFilter(groupColumn, filterRow);
	}

	/**
	 * Creation of the filter for package version
	 * @param packageVersionColumn Column
	 * @param filterRow Row filter
	 */
	private void createPackageVersionFilter(Column<OverrideConfigEntity> packageVersionColumn, HeaderRow filterRow) {
		TextField packageVersionField = new TextField();
		packageVersionField.addValueChangeListener(event -> {
			this.overrideConfigFilter.setPackageVersion(event.getValue());
			this.overrideDataProvider.refreshAll();
		});
		packageVersionField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(packageVersionColumn).setComponent(packageVersionField);
		packageVersionField.setSizeFull();
		packageVersionField.setPlaceholder("Filter package version");
		UIUtil.setTooltip(packageVersionField, TOOLTIP_FILTER_BY_PACKAGE_VERSION);
	}

	/**
	 * Creation of the filter for launch config
	 * @param launchConfigColumn Column
	 * @param filterRow Row filter
	 */
	private void createLaunchConfigFilter(Column<OverrideConfigEntity> launchConfigColumn, HeaderRow filterRow) {
		TextField launchConfigField = new TextField();
		launchConfigField.addValueChangeListener(event -> {
			this.overrideConfigFilter.setLaunchConfig(event.getValue());
			this.overrideDataProvider.refreshAll();
		});
		launchConfigField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(launchConfigColumn).setComponent(launchConfigField);
		launchConfigField.setSizeFull();
		launchConfigField.setPlaceholder("Filter launch config");
		UIUtil.setTooltip(launchConfigField, TOOLTIP_FILTER_BY_LAUNCH_CONFIG);
	}

	/**
	 * Creation of the filter for groups
	 * @param groupColumn Column
	 * @param filterRow Row filter
	 */
	private void createGroupFilter(Column<OverrideConfigEntity> groupColumn, HeaderRow filterRow) {
		TextField groupField = new TextField();
		groupField.addValueChangeListener(event -> {
			this.overrideConfigFilter.setGroup(event.getValue());
			this.overrideDataProvider.refreshAll();
		});
		groupField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(groupColumn).setComponent(groupField);
		groupField.setSizeFull();
		groupField.setPlaceholder("Filter group");
		UIUtil.setTooltip(groupField, TOOLTIP_FILTER_BY_GROUP);
	}

	/**
	 * Add column Package version
	 * @return column built
	 */
	private Column<OverrideConfigEntity> addColumnPackageVersion() {
		return this
			.addColumn(overrideConfigEntity -> overrideConfigEntity.getPackageVersion().getQualifier() == null
					? overrideConfigEntity.getPackageVersion().getVersionNumber()
					: overrideConfigEntity.getPackageVersion().getVersionNumber()
							+ overrideConfigEntity.getPackageVersion().getQualifier())
			.setHeader("Package version")
			.setWidth("20%")
			.setSortable(false)
			.setKey("packageVersionColumn");
	}

	/**
	 * Add column launch config
	 * @return column built
	 */
	private Column<OverrideConfigEntity> addColumnLaunchConfig() {
		return this.addColumn(overrideConfigEntity -> overrideConfigEntity.getLaunchConfig().getName())
			.setHeader("Launch config")
			.setWidth("20%")
			.setSortable(false)
			.setKey("launchConfigColumn");
	}

	/**
	 * Add column group
	 * @return column built
	 */
	private Column<OverrideConfigEntity> addColumnGroup() {
		return this.addColumn(overrideConfigEntity -> overrideConfigEntity.getTarget().getName())
			.setHeader("Group")
			.setWidth("20%")
			.setSortable(false)
			.setKey("groupColumn");
	}

}
