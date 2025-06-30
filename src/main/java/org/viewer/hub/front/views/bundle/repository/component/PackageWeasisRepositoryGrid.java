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
package org.viewer.hub.front.views.bundle.repository.component;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Getter;
import lombok.Setter;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.back.util.DateTimeUtil;
import org.viewer.hub.back.util.StringUtil;
import org.viewer.hub.front.components.UIUtil;
import org.viewer.hub.front.views.bundle.repository.WeasisAssetDataProvider;

import java.time.format.DateTimeFormatter;

/**
 * Grid for the nexus Weasis repository
 */
@Getter
@Setter
public class PackageWeasisRepositoryGrid extends Grid<WeasisAssetModel> {

	// Tooltips
	public static final String TOOLTIP_FILTER_BY_ARTIFACT_NAME = "Filter by artifact name";

	public static final String TOOLTIP_FILTER_BY_ARTIFACT_VERSION = "Filter by artifact version";

	public static final String TOOLTIP_FILTER_BY_FILE_SIZE = "Filter by file size";

	public static final String TOOLTIP_FILTER_BY_UPLOADER = "Filter by uploader";

	public static final String TOOLTIP_FILTER_BY_LAST_MODIFIED = "Filter by last modified";

	public static final String TOOLTIP_FILTER_BY_LAST_DOWNLOADED = "Filter by last downloaded";

	public static final String TOOLTIP_VERSION_ALREADY_INSTALLED = "Version already installed";

	public static final String TOOLTIP_VERSION_NOT_YET_INSTALLED = "Version not yet installed";

	// Filter grid rows
	private WeasisAssetFilter weasisAssetFilter;

	// DataProvider
	private final WeasisAssetDataProvider<WeasisAssetModel> weasisAssetDataProvider;

	/**
	 * Constructor
	 * @param weasisAssetDataProvider Data provider for Weasis assets repository
	 */
	public PackageWeasisRepositoryGrid(WeasisAssetDataProvider<WeasisAssetModel> weasisAssetDataProvider) {
		super();
		this.weasisAssetDataProvider = weasisAssetDataProvider;

		// Set size for the grid
		this.setWidthFull();
		this.setHeightFull();

		// Themes of the grid
		this.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

		// Case no assets in the repository or not connected to a nexus repository
		this.setEmptyStateText(
				"Nexus not connected or issue during connection or no assets built in the provided repository");

		// Build columns
		// Status column
		Column<WeasisAssetModel> statusColumn = this.addColumnStatus();
		// Artifact name
		Column<WeasisAssetModel> artifactNameColumn = this.addColumnArtifactName();
		// Artifact version
		Column<WeasisAssetModel> artifactVersionColumn = this.addColumnArtifactVersion();
		// File size
		Column<WeasisAssetModel> fileSizeColumn = this.addColumnFileSize();
		// Uploader
		Column<WeasisAssetModel> uploaderColumn = this.addColumnUploader();
		// Last modified
		Column<WeasisAssetModel> lastModifiedColumn = this.addColumnLastModified();
		// Last downloaded
		Column<WeasisAssetModel> lastDownloadedColumn = this.addColumnLastDownloaded();
		// Action
		Column<WeasisAssetModel> actionColumn = this.addColumnAction();

		// Create filters on rows
		this.createFiltersOnRows(artifactNameColumn, artifactVersionColumn, fileSizeColumn, uploaderColumn,
				lastModifiedColumn, lastDownloadedColumn);
	}

	/**
	 * Create filters
	 * @param artifactNameColumn Artifact name Column
	 * @param artifactVersionColumn Artifact version Column
	 * @param fileSizeColumn file size Column
	 * @param uploaderColumn Uploader Column
	 * @param lastModifiedColumn Last modified Column
	 * @param lastDownloadedColumn Last downloaded Column
	 */
	private void createFiltersOnRows(Column<WeasisAssetModel> artifactNameColumn,
			Column<WeasisAssetModel> artifactVersionColumn, Column<WeasisAssetModel> fileSizeColumn,
			Column<WeasisAssetModel> uploaderColumn, Column<WeasisAssetModel> lastModifiedColumn,
			Column<WeasisAssetModel> lastDownloadedColumn) {
		// Filters
		HeaderRow filterRow = this.appendHeaderRow();
		this.weasisAssetFilter = new WeasisAssetFilter();

		// Set size for the grid
		this.setWidthFull();
		this.setHeightFull();

		// Themes of the grid
		this.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

		// Artifact name filter
		this.createArtifactNameFilter(artifactNameColumn, filterRow);
		// Artifact version filter
		this.createArtifactVersionFilter(artifactVersionColumn, filterRow);
		// File size filter
		this.createFileSizeFilter(fileSizeColumn, filterRow);
		// Uploader filter
		this.createUploaderFilter(uploaderColumn, filterRow);
		// Last modified filter
		this.createLastModifiedFilter(lastModifiedColumn, filterRow);
		// Last downloaded filter
		this.createLastDownloadedFilter(lastDownloadedColumn, filterRow);
	}

	/**
	 * Creation of the filter for artifact name
	 * @param artifactNameColumn Column
	 * @param filterRow Row filter
	 */
	private void createArtifactNameFilter(Column<WeasisAssetModel> artifactNameColumn, HeaderRow filterRow) {
		TextField artifactNameField = new TextField();
		artifactNameField.addValueChangeListener(event -> {
			this.weasisAssetFilter.setArtifactName(event.getValue());
			this.weasisAssetDataProvider.refreshAll();
		});
		artifactNameField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(artifactNameColumn).setComponent(artifactNameField);
		artifactNameField.setSizeFull();
		artifactNameField.setPlaceholder("Filter artifact name");
		UIUtil.setTooltip(artifactNameField, TOOLTIP_FILTER_BY_ARTIFACT_NAME);
	}

	/**
	 * Creation of the filter for artifact version
	 * @param artifactVersionColumn Column
	 * @param filterRow Row filter
	 */
	private void createArtifactVersionFilter(Column<WeasisAssetModel> artifactVersionColumn, HeaderRow filterRow) {
		TextField artifactVersionField = new TextField();
		artifactVersionField.addValueChangeListener(event -> {
			this.weasisAssetFilter.setArtifactVersion(event.getValue());
			this.weasisAssetDataProvider.refreshAll();
		});
		artifactVersionField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(artifactVersionColumn).setComponent(artifactVersionField);
		artifactVersionField.setSizeFull();
		artifactVersionField.setPlaceholder("Filter artifact version");
		UIUtil.setTooltip(artifactVersionField, TOOLTIP_FILTER_BY_ARTIFACT_VERSION);
	}

	/**
	 * Creation of the filter for file size
	 * @param fileSizeColumn Column
	 * @param filterRow Row filter
	 */
	private void createFileSizeFilter(Column<WeasisAssetModel> fileSizeColumn, HeaderRow filterRow) {
		TextField fileSizeField = new TextField();
		fileSizeField.addValueChangeListener(event -> {
			this.weasisAssetFilter.setFileSize(event.getValue());
			this.weasisAssetDataProvider.refreshAll();
		});
		fileSizeField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(fileSizeColumn).setComponent(fileSizeField);
		fileSizeField.setSizeFull();
		fileSizeField.setPlaceholder("Filter file size");
		UIUtil.setTooltip(fileSizeField, TOOLTIP_FILTER_BY_FILE_SIZE);
	}

	/**
	 * Creation of the filter for uploader
	 * @param uploaderColumn Column
	 * @param filterRow Row filter
	 */
	private void createUploaderFilter(Column<WeasisAssetModel> uploaderColumn, HeaderRow filterRow) {
		TextField uploaderField = new TextField();
		uploaderField.addValueChangeListener(event -> {
			this.weasisAssetFilter.setUploader(event.getValue());
			this.weasisAssetDataProvider.refreshAll();
		});
		uploaderField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(uploaderColumn).setComponent(uploaderField);
		uploaderField.setSizeFull();
		uploaderField.setPlaceholder("Filter uploader");
		UIUtil.setTooltip(uploaderField, TOOLTIP_FILTER_BY_UPLOADER);
	}

	/**
	 * Creation of the filter for last modified
	 * @param lastModifiedColumn Column
	 * @param filterRow Row filter
	 */
	private void createLastModifiedFilter(Column<WeasisAssetModel> lastModifiedColumn, HeaderRow filterRow) {
		TextField lastModifiedField = new TextField();
		lastModifiedField.addValueChangeListener(event -> {
			this.weasisAssetFilter.setLastModified(event.getValue());
			this.weasisAssetDataProvider.refreshAll();
		});
		lastModifiedField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(lastModifiedColumn).setComponent(lastModifiedField);
		lastModifiedField.setSizeFull();
		lastModifiedField.setPlaceholder("Filter last modified");
		UIUtil.setTooltip(lastModifiedField, TOOLTIP_FILTER_BY_LAST_MODIFIED);
	}

	/**
	 * Creation of the filter for last downloaded
	 * @param lastDownloadedColumn Column
	 * @param filterRow Row filter
	 */
	private void createLastDownloadedFilter(Column<WeasisAssetModel> lastDownloadedColumn, HeaderRow filterRow) {
		TextField lastDownloadedField = new TextField();
		lastDownloadedField.addValueChangeListener(event -> {
			this.weasisAssetFilter.setLastDownloaded(event.getValue());
			this.weasisAssetDataProvider.refreshAll();
		});
		lastDownloadedField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(lastDownloadedColumn).setComponent(lastDownloadedField);
		lastDownloadedField.setSizeFull();
		lastDownloadedField.setPlaceholder("Filter last modified");
		UIUtil.setTooltip(lastDownloadedField, TOOLTIP_FILTER_BY_LAST_DOWNLOADED);
	}

	/**
	 * Add column status: version already installed on server or not yet installed
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnStatus() {
		return this.addComponentColumn(weasisAssetModel -> createStatusIcon(weasisAssetModel.isAlreadyInstalled()))
			.setTooltipGenerator(weasisAssetModel -> weasisAssetModel.isAlreadyInstalled()
					? TOOLTIP_VERSION_ALREADY_INSTALLED : TOOLTIP_VERSION_NOT_YET_INSTALLED)
			.setHeader("Status")
			.setWidth("1%")
			.setTextAlign(ColumnTextAlign.CENTER)
			.setSortable(true)
			.setKey("statusColumn");
	}

	/**
	 * Build status icon
	 * @param isAlreadyInstalled: check if version is already installed on server
	 * @return Icon built
	 */
	private Icon createStatusIcon(boolean isAlreadyInstalled) {
		Icon icon;
		if (isAlreadyInstalled) {
			icon = VaadinIcon.CHECK.create();
			icon.getElement().getThemeList().add("badge success");
		}
		else {
			icon = VaadinIcon.CLOSE_SMALL.create();
			icon.getElement().getThemeList().add("badge error");
		}
		icon.getStyle().set("padding", "var(--lumo-space-xs");
		return icon;
	}

	/**
	 * Add column Artifact name
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnArtifactName() {
		return this.addColumn(WeasisAssetModel::getArtifactId)
			.setHeader("Artifact name")
			.setWidth("10%")
			.setSortable(false)
			.setKey("nameColumn");
	}

	/**
	 * Add column Artifact version
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnArtifactVersion() {
		return this.addColumn(WeasisAssetModel::getVersion)
			.setHeader("Artifact version")
			.setWidth("10%")
			.setSortable(true)
			.setKey("versionColumn");
	}

	/**
	 * Add column File size
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnFileSize() {
		return this
			.addColumn(weasisAssetModel -> StringUtil.convertSizeToReadableFormat(weasisAssetModel.getFileSize()))
			.setHeader("File size")
			.setWidth("10%")
			.setSortable(true)
			.setKey("fileSizeColumn");
	}

	/**
	 * Add column Uploader
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnUploader() {
		return this.addColumn(WeasisAssetModel::getUploader)
			.setHeader("Uploader")
			.setWidth("10%")
			.setSortable(false)
			.setKey("uploaderColumn");
	}

	/**
	 * Add column Last Modified
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnLastModified() {
		return this
			.addColumn(weasisAssetModel -> DateTimeUtil.parseIso8601DateStringToReadableFormat(
					weasisAssetModel.getLastModified(), DateTimeFormatter.ofPattern(WeasisAssetModel.DATE_FORMAT)))
			.setHeader("Last Modified")
			.setWidth("10%")
			.setSortable(true)
			.setKey("lastModifiedColumn");
	}

	/**
	 * Add column Last Downloaded
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnLastDownloaded() {
		return this
			.addColumn(weasisAssetModel -> DateTimeUtil.parseIso8601DateStringToReadableFormat(
					weasisAssetModel.getLastDownloaded(), DateTimeFormatter.ofPattern(WeasisAssetModel.DATE_FORMAT)))
			.setHeader("Last Downloaded")
			.setWidth("10%")
			.setSortable(true)
			.setKey("lastDownloadedColumn");
	}

	/**
	 * Add column action
	 * @return column built
	 */
	private Column<WeasisAssetModel> addColumnAction() {
		return this.addColumn(createActionButtons())
			.setHeader("Action")
			.setWidth("10%")
			.setSortable(false)
			.setKey("actionColumn");
	}

	/**
	 * Create the renderer of the action buttons
	 * @return the renderer created
	 */
	private ComponentRenderer<PackageWeasisRepositoryActionContent, WeasisAssetModel> createActionButtons() {
		return new ComponentRenderer<>(
				() -> new PackageWeasisRepositoryActionContent(
						this.weasisAssetDataProvider.getWeasisRepositoryLogic().getWeasisRepositoryView()),
				PackageWeasisRepositoryActionContent::buildActionContentToDisplay);
	}

}
