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
package org.weasis.manager.front.views.i18n.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Getter;
import lombok.Setter;
import org.weasis.manager.back.entity.I18nEntity;
import org.weasis.manager.front.components.UIUtil;
import org.weasis.manager.front.views.i18n.I18nDataProvider;

import java.io.Serial;

/**
 * Grid for the i18n view
 */
@Getter
@Setter
public class I18nGrid extends Grid<I18nEntity> {

	@Serial
	private static final long serialVersionUID = -5148755652392799696L;

	// Tooltips
	public static final String TOOLTIP_FILTER_BY_I18N_VERSION = "Filter by i18n version";

	// Filter grid rows
	private I18nFilter i18nFilter;

	// DataProvider
	private final I18nDataProvider<I18nEntity> i18nDataProvider;

	/**
	 * Constructor
	 * @param i18nDataProvider Data provider for i18n versions
	 */
	public I18nGrid(I18nDataProvider<I18nEntity> i18nDataProvider) {
		super();
		this.i18nDataProvider = i18nDataProvider;

		// Set size for the grid
		this.setWidthFull();
		this.setHeight(86, Unit.PERCENTAGE);

		// Themes of the grid
		this.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

		// Build columns
		// I18n
		Column<I18nEntity> i18nColumn = this.addColumnI18n();

		// Create filters on rows
		this.createFiltersOnRows(i18nColumn);

		this.setMaxWidth("50%");
	}

	/**
	 * Add column I18n
	 * @return column built
	 */
	private Column<I18nEntity> addColumnI18n() {
		return this
			.addColumn(i18nEntity -> i18nEntity.getQualifier() == null ? i18nEntity.getVersionNumber()
					: i18nEntity.getVersionNumber() + i18nEntity.getQualifier())
			.setHeader("I18n version")
			.setWidth("80%")
			.setSortable(false)
			.setKey("i18nColumn");
	}

	/**
	 * Create filters
	 * @param i18nColumn I18n Version Column
	 */
	private void createFiltersOnRows(Column<I18nEntity> i18nColumn) {
		// Filters
		HeaderRow filterRow = this.appendHeaderRow();
		this.i18nFilter = new I18nFilter();

		// I18n version filter
		this.createI18nVersionFilter(i18nColumn, filterRow);
	}

	/**
	 * Creation of the filter for i18n version
	 * @param i18nColumn Column
	 * @param filterRow Row filter
	 */
	private void createI18nVersionFilter(Column<I18nEntity> i18nColumn, HeaderRow filterRow) {
		TextField i18nVersionField = new TextField();
		i18nVersionField.addValueChangeListener(event -> {
			this.i18nFilter.setI18nVersion(event.getValue());
			this.i18nDataProvider.refreshAll();
		});
		i18nVersionField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(i18nColumn).setComponent(i18nVersionField);
		i18nVersionField.setSizeFull();
		i18nVersionField.setPlaceholder("Filter i18n version");
		UIUtil.setTooltip(i18nVersionField, TOOLTIP_FILTER_BY_I18N_VERSION);
	}

}
