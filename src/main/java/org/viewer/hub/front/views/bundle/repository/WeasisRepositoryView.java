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

package org.viewer.hub.front.views.bundle.repository;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.bundle.repository.component.PackageWeasisRepositoryGrid;

@PageTitle(WeasisRepositoryView.VIEW_NAME)
@Route(WeasisRepositoryView.ROUTE)
@Component
@UIScope
@Slf4j
@Getter
public class WeasisRepositoryView extends AbstractView {

	public static final String ROUTE = "weasis-repository";

	public static final String VIEW_NAME = "Weasis Repository";

	// Logic
	private final WeasisRepositoryLogic weasisRepositoryLogic;

	// UI components
	private PackageWeasisRepositoryGrid packageWeasisRepositoryGrid;

	private Button refreshGridButton;

	private final WeasisAssetDataProvider<WeasisAssetModel> weasisAssetDataProvider;

	@Autowired
	public WeasisRepositoryView(final WeasisRepositoryLogic weasisRepositoryLogic,
			final WeasisAssetDataProvider<WeasisAssetModel> weasisAssetDataProvider) {
		this.weasisRepositoryLogic = weasisRepositoryLogic;
		this.weasisAssetDataProvider = weasisAssetDataProvider;

		// Set the view in the service
		this.weasisRepositoryLogic.setWeasisRepositoryView(this);

		// Build components
		this.buildComponents();

		// Add components in the view
		this.addComponentsView();

		// Add events listeners
		this.addEventListeners();
	}

	/**
	 * Build components
	 */
	private void buildComponents() {
		// Grid + data provider
		this.packageWeasisRepositoryGrid = new PackageWeasisRepositoryGrid(this.weasisAssetDataProvider);
		this.weasisAssetDataProvider.setFilter(this.packageWeasisRepositoryGrid.getWeasisAssetFilter());
		this.packageWeasisRepositoryGrid.setDataProvider(this.weasisAssetDataProvider);

		// Refresh button
		this.refreshGridButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
		this.refreshGridButton.addClickListener(buttonClickEvent -> this.weasisAssetDataProvider.refreshAll());
		this.refreshGridButton.setSizeFull();

		this.refreshGridButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
	}

	/**
	 * Add components in the view
	 */
	private void addComponentsView() {
		// Grid
		this.add(this.packageWeasisRepositoryGrid);

		// Buttons
		HorizontalLayout buttonLayout = new HorizontalLayout(this.refreshGridButton);
		buttonLayout.getStyle().set("margin-top", "70px");
		buttonLayout.setWidthFull();
		this.add(buttonLayout);

		this.setSizeFull();
		this.setWidthFull();
	}

	/**
	 * Event listeners:</br>
	 */
	private void addEventListeners() {
	}

	public void addActionButtonRemoveClickListener(Button buttonRemove, WeasisAssetModel weasisAssetModel) {
		buttonRemove.addClickListener(event -> weasisRepositoryLogic.removePackageVersion(weasisAssetModel));
	}

	public void addActionButtonImportClickListener(Button buttonDownload, WeasisAssetModel weasisAssetModel) {
		buttonDownload.addClickListener(event -> weasisRepositoryLogic.importPackageVersion(weasisAssetModel));
	}

}
