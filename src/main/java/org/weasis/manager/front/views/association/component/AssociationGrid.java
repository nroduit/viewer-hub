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

package org.weasis.manager.front.views.association.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.model.AssociationModel;
import org.weasis.manager.back.model.AssociationModelFilter;
import org.weasis.manager.front.views.association.AssociationDataProvider;

/**
 * Grid for the association view
 */
public class AssociationGrid extends Grid<AssociationModel> {

	// Filter grid rows
	private transient AssociationModelFilter associationModelFilter;

	// Original data provider not filtered by the Paginated grid: currently the method
	// getDataProvider() of the PaginatedGrid returns a temporary filtered data provider:
	// https://github.com/Klaudeta/grid-pagination/issues/9
	// Used to refresh the grid
	private AssociationDataProvider<AssociationModel> originalDataProvider;

	// Value provider for column BelongToMemberOf
	private final ValueProvider<AssociationModel, MultiSelectComboBox<TargetEntity>> multiSelectComboBoxBelongToMemberOfValueProvider;

	/**
	 * Constructor
	 * @param originalDataProvider Original data provider
	 * @param multiSelectComboBoxBelongToMemberOfValueProvider Value provider for column
	 * BelongToMemberOf
	 */
	public AssociationGrid(AssociationDataProvider<AssociationModel> originalDataProvider,
			ValueProvider<AssociationModel, MultiSelectComboBox<TargetEntity>> multiSelectComboBoxBelongToMemberOfValueProvider) {

		this.originalDataProvider = originalDataProvider;
		this.multiSelectComboBoxBelongToMemberOfValueProvider = multiSelectComboBoxBelongToMemberOfValueProvider;

		// Set size for the grid
		this.setWidthFull();

		// Build columns
		// Target name
		Column<AssociationModel> targetNameColumn = this.addColumnTargetName();
		// Target Type
		Column<AssociationModel> targetTypeColumn = this.addColumnTargetType();
		// Belongs to/Member of
		Column<AssociationModel> belongToMemberOfColumn = this.addColumnBelongToMemberOf();

		// Create filters on rows
		this.createFiltersOnRows(targetNameColumn, targetTypeColumn, belongToMemberOfColumn);
	}

	/**
	 * Add column target name
	 * @return column built
	 */
	private Column<AssociationModel> addColumnTargetName() {
		return this.addColumn(associationModel -> associationModel.getTarget().getName())
			.setHeader("Target Name")
			.setWidth("22%")
			.setSortable(true)
			.setKey("name");
	}

	/**
	 * Add column BelongToMemberOf
	 * @return column built
	 */
	private Column<AssociationModel> addColumnBelongToMemberOf() {
		return this.addComponentColumn(this.multiSelectComboBoxBelongToMemberOfValueProvider)
			.setHeader("Belongs to / Member of")
			.setWidth("60%")
			.setSortable(false); // If sortable, define a comparator
	}

	/**
	 * Add column Target type
	 * @return column built
	 */
	private Column<AssociationModel> addColumnTargetType() {
		return this.addColumn(associationModel -> associationModel.getTarget().getType().getDescription())
			.setHeader("Target Type")
			.setWidth("8%")
			.setTextAlign(ColumnTextAlign.CENTER)
			.setSortable(true)
			.setKey("type");
	}

	/**
	 * Create filters on rows
	 * @param targetNameColumn Target Name Column
	 * @param targetTypeColumn Target Type Column
	 * @param belongToMemberOfColumn BelongToMemberOf Column
	 */
	private void createFiltersOnRows(Column<AssociationModel> targetNameColumn,
			Column<AssociationModel> targetTypeColumn, Column<AssociationModel> belongToMemberOfColumn) {
		// Filters
		HeaderRow filterRow = this.appendHeaderRow();
		this.associationModelFilter = new AssociationModelFilter();

		// Filters
		// Target filter
		this.createTargetNameFilter(targetNameColumn, filterRow);

		// Target Type
		this.createTargetTypeFilter(targetTypeColumn, filterRow);

		// Belong To / Member Of
		this.createBelongToMemberOfFilter(belongToMemberOfColumn, filterRow);
	}

	/**
	 * Create filter for belongToMemberOf Column
	 * @param belongToMemberOfColumn belongToMemberOf Column
	 * @param filterRow Filter row
	 */
	private void createBelongToMemberOfFilter(Column<AssociationModel> belongToMemberOfColumn, HeaderRow filterRow) {
		TextField belongToMemberOfField = new TextField();
		belongToMemberOfField.addValueChangeListener(event -> {
			this.associationModelFilter.setBelongToMemberOf(event.getValue());
			this.originalDataProvider.refreshAll();
		});

		belongToMemberOfField.setValueChangeMode(ValueChangeMode.EAGER);

		filterRow.getCell(belongToMemberOfColumn).setComponent(belongToMemberOfField);
		belongToMemberOfField.setSizeFull();
		belongToMemberOfField.setPlaceholder("Filter Belong To / Member Of");
	}

	/**
	 * Create filter for targetType Column
	 * @param targetTypeColumn targetType Column
	 * @param filterRow Filter row
	 */
	private void createTargetTypeFilter(Column<AssociationModel> targetTypeColumn, HeaderRow filterRow) {
		ComboBox<TargetType> targetTypeCombo = new ComboBox<>();
		targetTypeCombo.setPlaceholder("Filter Target Type");
		targetTypeCombo.setClearButtonVisible(true);
		targetTypeCombo.setItems(TargetType.values());
		targetTypeCombo.addValueChangeListener(event -> {
			this.associationModelFilter.setTargetType(targetTypeCombo.getValue());
			this.originalDataProvider.refreshAll();
		});
		filterRow.getCell(targetTypeColumn).setComponent(targetTypeCombo);
		targetTypeCombo.setSizeFull();
	}

	/**
	 * Create filter for targetName Column
	 * @param targetNameColumn targetName Column
	 * @param filterRow Filter row
	 */
	private void createTargetNameFilter(Column<AssociationModel> targetNameColumn, HeaderRow filterRow) {
		TextField targetNameField = new TextField();
		targetNameField.addValueChangeListener(event -> {
			this.associationModelFilter.setTargetName(event.getValue());
			this.originalDataProvider.refreshAll();
		});

		targetNameField.setValueChangeMode(ValueChangeMode.EAGER);

		filterRow.getCell(targetNameColumn).setComponent(targetNameField);
		targetNameField.setSizeFull();
		targetNameField.setPlaceholder("Filter Target Name");
	}

	public AssociationModelFilter getAssociationModelFilter() {
		return this.associationModelFilter;
	}

	public void setAssociationModelFilter(AssociationModelFilter associationModelFilter) {
		this.associationModelFilter = associationModelFilter;
	}

	public AssociationDataProvider<AssociationModel> getOriginalDataProvider() {
		return this.originalDataProvider;
	}

	public void setOriginalDataProvider(AssociationDataProvider<AssociationModel> originalDataProvider) {
		this.originalDataProvider = originalDataProvider;
	}

}
