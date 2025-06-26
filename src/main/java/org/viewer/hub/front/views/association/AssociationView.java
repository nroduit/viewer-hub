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

package org.viewer.hub.front.views.association;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.model.AssociationModel;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageFormat;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.association.component.AssociationAddUserDialog;
import org.viewer.hub.front.views.association.component.AssociationGrid;
import org.viewer.hub.front.views.association.component.LaunchGrid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * View managing associations
 */
@PageTitle(AssociationView.VIEW_NAME)
@Route(AssociationView.ROUTE)
@Menu(order = 1, icon = LineAwesomeIconUrl.LINK_SOLID)
@Secured({ "ROLE_admin" })
public class AssociationView extends AbstractView {

	private static final long serialVersionUID = 5648280472926050104L;

	public static final String ROUTE = "association";

	public static final String VIEW_NAME = "Association";

	// Logic
	private final transient AssociationLogic associationLogic;

	// Components
	private AssociationGrid associationGrid;

	private final AssociationDataProvider<AssociationModel> associationDataProvider;

	private LaunchGrid selectedLaunchGrid;

	private Accordion belongToMemberOfAccordion;

	private SplitLayout launchLayout;

	@Autowired
	public AssociationView(AssociationLogic associationLogic,
			AssociationDataProvider<AssociationModel> associationDataProvider) {
		this.associationLogic = associationLogic;
		this.associationDataProvider = associationDataProvider;

		// Set the view in the service
		this.associationLogic.setAssociationView(this);

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
		this.associationGrid = new AssociationGrid(this.associationDataProvider,
				this.createMultiSelectComboBoxBelongToMemberOfValueProvider());
		this.associationDataProvider.setFilter(this.associationGrid.getAssociationModelFilter());
		this.associationGrid.setDataProvider(this.associationDataProvider);

		// Preferences
		// Selected
		this.selectedLaunchGrid = new LaunchGrid();
		// BelongTo/MemberOf Launches
		this.belongToMemberOfAccordion = new Accordion();
		this.belongToMemberOfAccordion.setVisible(true);
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
		Button buttonAddTarget = new Button("Add Target", new Icon(VaadinIcon.PLUS));
		buttonAddTarget.addClickListener(event -> this.addTargetButtonListener());
		buttonAddTarget.setWidthFull();
		this.associationGrid.asSingleSelect().addValueChangeListener(this::selectedRowAssociationGridListener);
		gridLayout.add(buttonAddTarget, this.associationGrid);
		gridLayout.setSizeFull();

		// Launch Layout
		this.launchLayout = new SplitLayout();
		this.launchLayout.setSplitterPosition(50);

		// Selected target preferences
		this.launchLayout.addToPrimary(new NativeLabel("Select row to see preferences..."));
		// Inherited preferences
		this.launchLayout.addToSecondary(this.belongToMemberOfAccordion);
		this.launchLayout.setPrimaryStyle("min-width", "50%");
		this.launchLayout.setSecondaryStyle("min-width", "50%");
		this.launchLayout.setSizeFull();

		// Add in split layout
		layout.addToPrimary(gridLayout);
		layout.setPrimaryStyle("min-height", "750px");
		layout.addToSecondary(this.launchLayout);
		layout.setSecondaryStyle("min-height", "350px");
		this.setWidth("100%");
		this.add(layout);
	}

	/**
	 * Create a value provider for column BelongToMemberOf
	 * @return Value Provider created
	 */
	private ValueProvider<AssociationModel, MultiSelectComboBox<TargetEntity>> createMultiSelectComboBoxBelongToMemberOfValueProvider() {

		// Retrieve all targets
		List<TargetEntity> belongToMemberOfTargets = this.associationLogic.retrieveAllTargets();

		return associationModel -> {
			MultiSelectComboBox<TargetEntity> multiselectComboBox = new MultiSelectComboBox<>();
			multiselectComboBox.setWidth("100%");
			multiselectComboBox.setItemLabelGenerator(TargetEntity::getName);

			// Set filter on each column in order to display the corresponding list of
			// targets depending on the target type
			ItemFilter<TargetEntity> itemFilter = this.belongToMemberOfItemFilter(associationModel);

			// Set filter and values
			multiselectComboBox.setItems(itemFilter, belongToMemberOfTargets);

			// Set selected values of the model
			multiselectComboBox.setValue(new HashSet<>(associationModel.getBelongToMemberOf()));

			// Change listener => refresh model + update in backend
			multiselectComboBox.addValueChangeListener(event -> {
				associationModel.setBelongToMemberOf(new ArrayList<>(event.getValue()));
				this.associationLogic.updateAssociationModel(associationModel);
			});

			return multiselectComboBox;
		};
	}

	/**
	 * Create the item filter for the column belongToMemberOf
	 * @param associationModel Association Model
	 * @return the item filter created
	 */
	private ItemFilter<TargetEntity> belongToMemberOfItemFilter(AssociationModel associationModel) {
		return (item, filter) ->
		// Autocomplete depending on the input of the user
		(filter.length() <= 0 || StringUtils.containsIgnoreCase(item.getName(), filter.trim())) &&
		// Filter by target type of the row
				(Objects.equals(TargetType.USER, associationModel.getTarget().getType())
						&& Objects.equals(TargetType.USER_GROUP, item.getType())
						|| Objects.equals(TargetType.HOST, associationModel.getTarget().getType())
								&& Objects.equals(TargetType.HOST_GROUP, item.getType())
						|| Objects.equals(TargetType.USER_GROUP, associationModel.getTarget().getType())
								&& Objects.equals(TargetType.USER, item.getType())
						|| Objects.equals(TargetType.HOST_GROUP, associationModel.getTarget().getType())
								&& Objects.equals(TargetType.HOST, item.getType()));
	}

	/**
	 * Listener on add target button
	 */
	private void addTargetButtonListener() {
		// Create and open dialog
		AssociationAddUserDialog associationAddUserDialog = new AssociationAddUserDialog();
		associationAddUserDialog.open();

		// Listener on create button
		associationAddUserDialog.getCreateButton().addClickListener(buttonClickEvent -> {
			// Validate inputs
			BinderValidationStatus<TargetEntity> validate = associationAddUserDialog.getBinder().validate();

			if (validate.isOk()) {
				// Retrieve target to create
				TargetEntity targetToCreate = associationAddUserDialog.getBinder().getBean();

				// Create target
				boolean hasBeenCreated = this.associationLogic.addTarget(targetToCreate);

				if (hasBeenCreated) {
					// Target has been created
					this.displayMessage(
							new Message(MessageLevel.INFO, MessageFormat.TEXT,
									String.format("Target %s has been created", targetToCreate)),
							MessageType.NOTIFICATION_MESSAGE);
					this.associationGrid.getOriginalDataProvider().refreshAll();
					associationAddUserDialog.close();
				}
				else {
					// Target has not been created because name already existing
					this.displayMessage(
							new Message(MessageLevel.WARN, MessageFormat.TEXT,
									String.format("Target name %s already existing!", targetToCreate.getName())),
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
			AbstractField.ComponentValueChangeEvent<Grid<AssociationModel>, AssociationModel> event) {
		// Association model selected
		AssociationModel associationModelSelected = event.getValue();

		// Remove previous components
		this.belongToMemberOfAccordion.getChildren().forEach(c -> this.belongToMemberOfAccordion.remove(c));

		// Retrieve the preferences of the target selected
		List<LaunchEntity> launchesSelected = new ArrayList<>();
		if (associationModelSelected != null && associationModelSelected.getTarget() != null) {
			this.launchLayout.addToPrimary(this.selectedLaunchGrid);
			launchesSelected = this.associationLogic.retrieveLaunches(associationModelSelected.getTarget());
			this.selectedLaunchGrid.setItems(launchesSelected);
		}
		else {
			this.launchLayout.addToPrimary(new NativeLabel("Select row to see preferences..."));
			this.launchLayout.setPrimaryStyle("min-width", "50%");
		}

		// Retrieve the BelongToMemberOf preferences
		Set<LaunchEntity> launchDuplicates = this.buildLaunchesBelongToMemberOfAccordion(associationModelSelected,
				launchesSelected);

		// Apply row style to the selected preferences depending on duplicates found
		this.selectedLaunchGrid.applyDuplicateRowStyle(launchDuplicates);

		// close by default the accordion
		this.belongToMemberOfAccordion.close();
	}

	/**
	 * Build Launches BelongToMemberOf Accordion
	 * @param associationModelSelected Association Model selected
	 * @param launchesSelected Launches selected
	 * @return Duplicates found
	 */
	private Set<LaunchEntity> buildLaunchesBelongToMemberOfAccordion(AssociationModel associationModelSelected,
			List<LaunchEntity> launchesSelected) {
		// List of duplicates
		Set<LaunchEntity> globalLaunchDuplicates = new HashSet<>();

		if (associationModelSelected != null && associationModelSelected.getBelongToMemberOf() != null
				&& !associationModelSelected.getBelongToMemberOf().isEmpty()) {
			// For each targets BelongToMemberOf, retrieve the launches and add to the
			// accordion
			for (TargetEntity belongToMemberOfTarget : associationModelSelected.getBelongToMemberOf()) {
				// Retrieve launches corresponding to the target BelongToMemberOf
				List<LaunchEntity> launchesBelongToMemberOf = this.associationLogic
					.retrieveLaunches(belongToMemberOfTarget);

				// Find duplicates between launches selected and belongTo/memberOf target
				List<LaunchEntity> launchesDuplicates = this.associationLogic
					.retrieveLaunchesDuplicates(launchesSelected, launchesBelongToMemberOf);
				globalLaunchDuplicates.addAll(launchesDuplicates);

				if (launchesBelongToMemberOf != null && !launchesBelongToMemberOf.isEmpty()) {
					// Build a new accordion grid
					LaunchGrid launchGrid = new LaunchGrid();
					// Set values
					launchGrid.setItems(launchesBelongToMemberOf);
					// Apply duplicate row style
					launchGrid.applyDuplicateRowStyle(launchesDuplicates);
					// Build label of the accordion panel
					NativeLabel label = new NativeLabel(String.format("%s [%d]", belongToMemberOfTarget.getName(),
							launchesBelongToMemberOf.size()));
					// Set color to red in the accordion panel if contains a duplicate
					if (!launchesDuplicates.isEmpty()) {
						label.getElement().getStyle().set("color", "coral");
					}
					// Add accordion panel built to the accordion
					this.belongToMemberOfAccordion.add(new AccordionPanel(label, launchGrid))
						.addThemeVariants(DetailsVariant.FILLED);
				}
				else {
					this.belongToMemberOfAccordion
						.add(String.format("%s [%d]", belongToMemberOfTarget.getName(), 0),
								new NativeLabel("No preferences"))
						.addThemeVariants(DetailsVariant.FILLED);
				}
			}
		}
		// return duplicates found
		return globalLaunchDuplicates;
	}

}
