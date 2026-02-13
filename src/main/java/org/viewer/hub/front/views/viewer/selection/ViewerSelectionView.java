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

package org.viewer.hub.front.views.viewer.selection;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ViewerSelectionType;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.viewer.selection.component.ViewerSelectionDialog;
import org.viewer.hub.front.views.viewer.selection.component.ViewerSelectionGrid;

import java.util.ArrayList;
import java.util.Objects;

/**
 * View managing associations
 */
@PageTitle(ViewerSelectionView.VIEW_NAME)
@Route("")
@Menu(order = 1, icon = LineAwesomeIconUrl.LINK_SOLID)
@Secured({ "ROLE_admin" })
public class ViewerSelectionView extends AbstractView {

	public static final String VIEW_NAME = "Viewer selection";

	public static final String ROUTE = "/viewer-selection";

	// Logic
	private final transient ViewerSelectionLogic viewerSelectionLogic;

	// Components
	@Getter
	private ViewerSelectionGrid viewerSelectionGrid;

	private final ViewerSelectionDataProvider<ViewerSelectionEntity> viewerSelectionDataProvider;
    private final ArrayList<String> archives;

	@Autowired
	public ViewerSelectionView(ViewerSelectionLogic viewerSelectionLogic,
							   ViewerSelectionDataProvider<ViewerSelectionEntity> viewerSelectionDataProvider,
							   ConnectorConfigurationProperties connectorConfigurationProperties) {
		this.viewerSelectionLogic = viewerSelectionLogic;
		this.viewerSelectionDataProvider = viewerSelectionDataProvider;

		this.archives = new ArrayList<>(connectorConfigurationProperties.getConnectors().keySet());
		this.archives.add(ViewerSelectionType.ALL.name());

		// Set the view in the service
		this.viewerSelectionLogic.setViewerSelectionView(this);

		// Build components
		this.buildComponents();

		// Add components in the view
		this.addComponentsView();
	}

	/**
	 * Build components
	 */
	private void buildComponents() {
		// Grid + data provider
		this.viewerSelectionGrid = new ViewerSelectionGrid(this, this.viewerSelectionDataProvider,
				this.createViewerComboBoxValueProvider(), this.createArchiveComboBoxValueProvider(), viewerSelectionLogic);
		this.viewerSelectionGrid.setDataProvider(this.viewerSelectionDataProvider);
	}

	/**
	 * Add components in the view
	 */
	private void addComponentsView() {
		// Grid layout selection
		VerticalLayout gridLayout = new VerticalLayout();
		Button addRuleButton = new Button("Add Rule", new Icon(VaadinIcon.PLUS));
		addRuleButton.addClickListener(event -> this.addRuleButtonListener());
		addRuleButton.setWidthFull();
		gridLayout.add(addRuleButton, this.viewerSelectionGrid);
		gridLayout.setSizeFull();

		this.add(gridLayout);
		this.setSizeFull();
	}

	/**
	 * Create a value provider for column viewer
	 * @return Value Provider created
	 */
	private ValueProvider<ViewerSelectionEntity, Select<ViewerType>> createViewerComboBoxValueProvider() {
		return viewerSelectionEntity -> {
			Select<ViewerType> comboBox = new Select<>();
			comboBox.setWidth("100%");
			comboBox.setItems(ViewerType.values());
			comboBox.setValue(viewerSelectionEntity.getViewer());
			comboBox.setItemLabelGenerator(ViewerType::getCode);

			// Change listener => refresh model + update in backend
			comboBox.addValueChangeListener(event -> {
				viewerSelectionEntity.setViewer(event.getValue());
				this.viewerSelectionLogic.updateViewerSelection(viewerSelectionEntity);
			});

			return comboBox;
		};
	}

	/**
	 * Create a value provider for column archive
	 * @return Value Provider created
	 */
	private ValueProvider<ViewerSelectionEntity, Select<String>> createArchiveComboBoxValueProvider() {
		return viewerSelectionEntity -> {
			Select<String> select = new Select<>();
			select.setWidth("100%");
			select.setEmptySelectionAllowed(false);

			if (Objects.equals(viewerSelectionEntity.getArchive(), ViewerSelectionType.DEFAULT.name())){
				select.setItems(viewerSelectionEntity.getArchive());
				select.setEnabled(false);
			} else {
				select.setItems(archives);
			}
			select.setValue(viewerSelectionEntity.getArchive());

			// Change listener => refresh model + update in backend
			select.addValueChangeListener(event -> {
				viewerSelectionEntity.setArchive(event.getValue());
				this.viewerSelectionLogic.updateViewerSelection(viewerSelectionEntity);
			});

			return select;
		};
	}

	/**
	 * Listener on add rule button
	 */
	private void addRuleButtonListener() {
		// Create and open dialog
		ViewerSelectionDialog viewerSelectionDialog = new ViewerSelectionDialog(viewerSelectionLogic,
				this, archives);
		viewerSelectionDialog.open();
	}

}
