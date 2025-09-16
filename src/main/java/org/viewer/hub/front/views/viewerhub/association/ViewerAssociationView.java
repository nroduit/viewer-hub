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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.back.enums.Viewer;
import org.viewer.hub.back.model.*;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.viewerhub.association.component.ViewerAssociationAddDialog;
import org.viewer.hub.front.views.viewerhub.association.component.ViewerAssociationGrid;

import java.util.HashSet;

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

	private Accordion viewerAccordion;

	@Autowired
	public ViewerAssociationView(ViewerAssociationLogic viewerAssociationLogic,
								 ViewerAssociationDataProvider<ViewerAssociationModel> viewerAssociationDataProvider) {
		this.viewerAssociationLogic = viewerAssociationLogic;
		this.viewerAssociationDataProvider = viewerAssociationDataProvider;

		// Set the view in the service
		this.viewerAssociationLogic.setAssociationView(this);

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
		this.viewerAssociationGrid = new ViewerAssociationGrid(this.viewerAssociationDataProvider,
				this.createComboBoxBelongToMemberOfValueProvider());
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
		Button buttonAddTarget = new Button("Add Archive", new Icon(VaadinIcon.PLUS));
		buttonAddTarget.addClickListener(event -> this.addArchiveButtonListener());
		buttonAddTarget.setWidthFull();
		this.viewerAssociationGrid.asSingleSelect().addValueChangeListener(this::selectedRowAssociationGridListener);
		gridLayout.add(buttonAddTarget, this.viewerAssociationGrid);
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
	private ValueProvider<ViewerAssociationModel, ComboBox<Viewer>> createComboBoxBelongToMemberOfValueProvider() {

		return viewerAssociationModel -> {
			ComboBox<Viewer> comboBox = new ComboBox<>();
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
	private void addArchiveButtonListener() {
		// Create and open dialog
		ViewerAssociationAddDialog viewerAssociationAddDialog = new ViewerAssociationAddDialog();
		viewerAssociationAddDialog.open();

		// Listener on create button
		viewerAssociationAddDialog.getCreateButton().addClickListener(buttonClickEvent -> {
			// Validate inputs
			BinderValidationStatus<ViewerAssociationModel> validate = viewerAssociationAddDialog.getBinder().validate();

			if (validate.isOk()) {
				// Retrieve target to create
				ViewerAssociationModel targetToCreate = viewerAssociationAddDialog.getBinder().getBean();

				// Create target
				boolean hasBeenCreated = this.viewerAssociationLogic.addViewerAssociationModel(targetToCreate);

				if (hasBeenCreated) {
					// Archive association has been created
					this.displayMessage(
							new Message(MessageLevel.INFO, MessageFormat.TEXT,
									String.format("Archive %s has been associated", targetToCreate.getArchive())),
							MessageType.NOTIFICATION_MESSAGE);
					this.viewerAssociationGrid.getOriginalDataProvider().refreshAll();
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


	/**
	 * Listener on a row selected in the grid association
	 * @param event Event
	 */
	private void selectedRowAssociationGridListener(
			AbstractField.ComponentValueChangeEvent<Grid<ViewerAssociationModel>, ViewerAssociationModel> event) {
		// Association model selected
		ViewerAssociationModel viewerAssociationModelSelected = event.getValue();

		// Remove previous components
		this.viewerAccordion.getChildren().forEach(c -> this.viewerAccordion.remove(c));

		// close by default the accordion
		this.viewerAccordion.close();
	}

}
