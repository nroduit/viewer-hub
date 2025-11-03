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

package org.viewer.hub.front.views.viewerhub.association;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.model.*;
import org.viewer.hub.back.service.ViewerAssociationService;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.viewerhub.association.component.ViewerAssociationAddDialog;
import org.viewer.hub.front.views.viewerhub.association.component.ViewerAssociationGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * View managing associations
 */
@PageTitle(ViewerAssociationView.VIEW_NAME)
@Route("")
@Menu(order = 1, icon = LineAwesomeIconUrl.LINK_SOLID)
@Secured({ "ROLE_admin" })
public class ViewerAssociationView extends AbstractView {

	public static final String VIEW_NAME = "Association";

	public static final String ROUTE = "association";

	// Logic
	private final transient ViewerAssociationLogic viewerAssociationLogic;

	// Components
	private ViewerAssociationGrid viewerAssociationGrid;

	private final ViewerAssociationDataProvider<ViewerAssociationModel> viewerAssociationDataProvider;
	private final ConnectorConfigurationProperties connectorConfigurationProperties;
	private final ViewerAssociationService viewerAssociationService;

	private Button addArchiveButton;
	private Accordion viewerAccordion;

	@Autowired
	public ViewerAssociationView(ViewerAssociationLogic viewerAssociationLogic,
								 ViewerAssociationDataProvider<ViewerAssociationModel> viewerAssociationDataProvider,
								 ConnectorConfigurationProperties connectorConfigurationProperties,
								 ViewerAssociationService viewerAssociationService) {
		this.viewerAssociationLogic = viewerAssociationLogic;
		this.viewerAssociationDataProvider = viewerAssociationDataProvider;
		this.connectorConfigurationProperties = connectorConfigurationProperties;
		this.viewerAssociationService = viewerAssociationService;

		// Set the view in the service
		this.viewerAssociationLogic.setAssociationView(this);

		// Build components
		this.buildComponents();

		// Add components in the view
		this.addComponentsView();

		viewerAssociationDataProvider.addDataProviderListener(dataChangeEvent -> {
			updateView();
		});
	}

	/**
	 * Build components
	 */
	private void buildComponents() {
		// Grid + data provider
		this.viewerAssociationGrid = new ViewerAssociationGrid(this.viewerAssociationDataProvider,
				this.createComboBoxBelongToMemberOfValueProvider(), viewerAssociationLogic);
		this.viewerAssociationGrid.setDataProvider(this.viewerAssociationDataProvider);

		// BelongTo/MemberOf Launches
		this.viewerAccordion = new Accordion();
		this.viewerAccordion.setVisible(true);
	}

	/**
	 * Add components in the view
	 */
	private void addComponentsView() {
		SplitLayout layout = new SplitLayout();
		layout.setOrientation(SplitLayout.Orientation.VERTICAL);
		layout.setSplitterPosition(75);
		layout.setSizeFull();

		// Grid layout association
		VerticalLayout gridLayout = new VerticalLayout();
		addArchiveButton = new Button("Add Archive", new Icon(VaadinIcon.PLUS));
		addArchiveButton.addClickListener(event -> this.archiveButtonListener());
		addArchiveButton.setWidthFull();
		addArchiveButton.setEnabled(!getRemainingArchives().isEmpty());
		this.viewerAssociationGrid.asSingleSelect().addValueChangeListener(this::selectedRowAssociationGridListener);
		gridLayout.add(addArchiveButton, this.viewerAssociationGrid);
		gridLayout.setSizeFull();

		// Add in split layout
		layout.addToPrimary(gridLayout);
		layout.setPrimaryStyle("min-height", "750px");
		this.setWidth("100%");
		this.add(layout);
	}

	/**
	 * Create a value provider for column BelongToMemberOf
	 * @return Value Provider created
	 */
	private ValueProvider<ViewerAssociationModel, Select<Viewer>> createComboBoxBelongToMemberOfValueProvider() {

		return viewerAssociationModel -> {
			Select<Viewer> comboBox = new Select<>();
			comboBox.setWidth("100%");
			comboBox.setItems(Viewer.values());
			comboBox.setValue(viewerAssociationModel.getViewer());
			comboBox.setItemLabelGenerator(Viewer::getCode);

			// Change listener => refresh model + update in backend
			comboBox.addValueChangeListener(event -> {
				viewerAssociationModel.setViewer(event.getValue());
				this.viewerAssociationLogic.updateViewerAssociationModel(viewerAssociationModel);
			});

			return comboBox;
		};
	}

	/**
	 * Listener on add archive button
	 */
	private void archiveButtonListener() {
		// Create and open dialog
		ViewerAssociationAddDialog viewerAssociationAddDialog = new ViewerAssociationAddDialog(connectorConfigurationProperties,
				getRemainingArchives());
		viewerAssociationAddDialog.open();

		// Listener on create button
		viewerAssociationAddDialog.getCreateButton().addClickListener(buttonClickEvent -> {
			// Validate inputs
			BinderValidationStatus<ViewerAssociationModel> validate = viewerAssociationAddDialog.getBinder().validate();

			if (validate.isOk()) {
				// Retrieve target to create
				ViewerAssociationModel targetToCreate = viewerAssociationAddDialog.getBinder().getBean();

				// Increment priority
				if (targetToCreate.getPriority() == null) {
					targetToCreate.setPriority(this.viewerAssociationLogic.countAssociationModels());
				}

				// Create target
				boolean hasBeenCreated = this.viewerAssociationLogic.addViewerAssociationModel(targetToCreate);

				if (hasBeenCreated) {
					// Archive association has been created
					this.displayMessage(
							new Message(MessageLevel.INFO, MessageFormat.TEXT,
									String.format("Archive %s has been associated", targetToCreate.getArchive())),
							MessageType.NOTIFICATION_MESSAGE);
					this.viewerAssociationGrid.getOriginalDataProvider().refreshAll();
					this.updateView();
					viewerAssociationAddDialog.close();
				}
				else {
					// ViewerAssociationModel has not been created because archive already existing
					this.displayMessage(
							new Message(MessageLevel.WARN, MessageFormat.TEXT,
									String.format("Archive %s already existing!", targetToCreate.getArchive())),
							MessageType.NOTIFICATION_MESSAGE);
				}
			}
		});
	}

	private void updateView() {
		addArchiveButton.setEnabled(!getRemainingArchives().isEmpty());
	}

	private ArrayList<String> getRemainingArchives() {
		List<String> associatedArchives = this.viewerAssociationService.retrieveViewerAssociationModels()
				.stream().map(ViewerAssociationModel::getArchive).toList();
		ArrayList<String> archives = new ArrayList<>(this.connectorConfigurationProperties.getConnectors().keySet());
		archives.removeAll(associatedArchives);
		return archives;
	}

	/**
	 * Listener on a row selected in the grid association
	 * @param event Event
	 */
	private void selectedRowAssociationGridListener(
			AbstractField.ComponentValueChangeEvent<Grid<ViewerAssociationModel>, ViewerAssociationModel> event) {
		// Remove previous components
		this.viewerAccordion.getChildren().forEach(c -> this.viewerAccordion.remove(c));

		// close by default the accordion
		this.viewerAccordion.close();
	}

}
