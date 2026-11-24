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

package org.viewer.hub.front.views.weasis.association;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.Strings;
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
import org.viewer.hub.front.views.weasis.association.component.AssociationAddUserDialog;
import org.viewer.hub.front.views.weasis.association.component.AssociationGrid;
import org.viewer.hub.front.views.weasis.association.component.LaunchGrid;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * View managing associations
 */
@PageTitle(AssociationView.VIEW_NAME)
@Route(AbstractView.WEASIS + AssociationView.ROUTE)
@Menu(order = 1, icon = LineAwesomeIconUrl.LINK_SOLID)
@Secured({ "ROLE_admin" })
public class AssociationView extends AbstractView {

	@Serial
	private static final long serialVersionUID = 5648280472926050104L;

	public static final String ROUTE = "/association";

	public static final String VIEW_NAME = "Association";

	// Logic
	private final transient AssociationLogic associationLogic;

	// Components
	private AssociationGrid associationGrid;

	private final AssociationDataProvider<AssociationModel> associationDataProvider;

	private LaunchGrid selectedLaunchGrid;

	private Accordion belongToMemberOfAccordion;

	private VerticalLayout drawer;

	private H3 drawerTitle;

	// Combo boxes of the rows rendered, by target id
	private final transient Map<Long, MultiSelectComboBox<TargetEntity>> rowComboBoxes = new HashMap<>();

	private boolean syncingRows;

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
				this.createMultiSelectComboBoxBelongToMemberOfValueProvider(), this::groupActiveListener);
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
		// Grid layout association
		VerticalLayout gridLayout = new VerticalLayout();
		Button buttonAddTarget = new Button("Add Target", new Icon(VaadinIcon.PLUS));
		buttonAddTarget.addClickListener(event -> this.addTargetButtonListener());
		buttonAddTarget.setWidthFull();
		this.associationGrid.asSingleSelect().addValueChangeListener(this::selectedRowAssociationGridListener);
		gridLayout.add(buttonAddTarget, this.associationGrid);
		this.associationGrid.setSizeFull();
		gridLayout.setFlexGrow(1, this.associationGrid);
		gridLayout.setSizeFull();

		// Side drawer displayed over the grid when a row is selected
		this.buildDrawer();

		// Container: full width grid + drawer positioned over its right side
		Div container = new Div(gridLayout, this.drawer);
		container.getStyle().set("position", "relative").set("overflow", "hidden").set("min-height", "400px");
		container.setSizeFull();
		this.add(container);
		this.setSizeFull();
		this.getStyle().set("overflow-x", "hidden");
	}

	/**
	 * Build the side drawer showing the details of the selected target
	 */
	private void buildDrawer() {
		this.drawerTitle = new H3();
		this.drawerTitle.getStyle().set("margin", "0").set("flex", "1");

		Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		closeButton.setAriaLabel("Close");
		closeButton.addClickListener(event -> this.associationGrid.deselectAll());

		HorizontalLayout header = new HorizontalLayout(this.drawerTitle, closeButton);
		header.setWidthFull();
		header.setAlignItems(FlexComponent.Alignment.CENTER);

		this.selectedLaunchGrid.setWidthFull();
		this.selectedLaunchGrid.setHeight("280px");
		this.selectedLaunchGrid.setMinHeight("280px");
		this.belongToMemberOfAccordion.setWidthFull();

		VerticalLayout content = new VerticalLayout(new H4("Preferences"), this.selectedLaunchGrid,
				new H4("Inherited preferences"), this.belongToMemberOfAccordion);
		content.setPadding(false);
		content.setWidthFull();
		content.setAlignItems(FlexComponent.Alignment.STRETCH);

		Div scrollContent = new Div(content);
		scrollContent.setWidthFull();
		scrollContent.getStyle().set("overflow-y", "auto").set("flex", "1").set("min-height", "0");

		this.drawer = new VerticalLayout(header, scrollContent);
		this.drawer.setPadding(true);
		this.drawer.setSpacing(false);
		this.drawer.setAlignItems(FlexComponent.Alignment.STRETCH);
		this.drawer.setWidth("45%");
		this.drawer.setMinWidth("480px");
		this.drawer.getStyle()
			.set("position", "fixed")
			.set("top", "0")
			.set("right", "0")
			.set("bottom", "0")
			.set("z-index", "10")
			.set("box-sizing", "border-box")
			.set("background-color", "var(--aura-background-color, Canvas)")
			.set("box-shadow", "-4px 0 12px rgba(0, 0, 0, 0.25)");
		this.drawer.setVisible(false);
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
				if (this.syncingRows) {
					return;
				}
				this.associationLogic.updateAssociationModel(associationModel);
				this.syncCounterpartRows(associationModel.getTarget(), event.getOldValue(), event.getValue());
			});

			this.rowComboBoxes.put(associationModel.getTarget().getId(), multiselectComboBox);
			return multiselectComboBox;
		};
	}

	/**
	 * Reflect an association change in the rows already displayed for the counterpart
	 * targets (e.g. the user added to a group), without reloading the grid
	 * @param changedTarget Target whose associations have been edited
	 * @param oldValue Associations before the change
	 * @param newValue Associations after the change
	 */
	private void syncCounterpartRows(TargetEntity changedTarget, Set<TargetEntity> oldValue,
			Set<TargetEntity> newValue) {
		Set<TargetEntity> added = new HashSet<>(newValue);
		added.removeAll(oldValue);
		Set<TargetEntity> removed = new HashSet<>(oldValue);
		removed.removeAll(newValue);

		this.syncingRows = true;
		try {
			added.forEach(target -> this.updateCounterpartRow(target, changedTarget, true));
			removed.forEach(target -> this.updateCounterpartRow(target, changedTarget, false));
		}
		finally {
			this.syncingRows = false;
		}
	}

	private void updateCounterpartRow(TargetEntity rowTarget, TargetEntity changedTarget, boolean add) {
		MultiSelectComboBox<TargetEntity> comboBox = this.rowComboBoxes.get(rowTarget.getId());
		if (comboBox == null || !comboBox.isAttached()) {
			this.rowComboBoxes.remove(rowTarget.getId());
			return;
		}
		Set<TargetEntity> values = new HashSet<>(comboBox.getValue());
		if (add ? values.add(changedTarget) : values.remove(changedTarget)) {
			comboBox.setValue(values);
		}
	}

	/**
	 * Create the item filter for the column belongToMemberOf
	 * @param associationModel Association Model
	 * @return the item filter created
	 */
	private ItemFilter<TargetEntity> belongToMemberOfItemFilter(AssociationModel associationModel) {
		return (item, filter) ->
		// Autocomplete depending on the input of the user
		(filter.isEmpty() || Strings.CI.contains(item.getName(), filter.trim())) &&
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
	 * Listener on the activation/deactivation of a group
	 * @param associationModel Row of the group
	 * @param active New state
	 */
	private void groupActiveListener(AssociationModel associationModel, boolean active) {
		TargetEntity updated = this.associationLogic.setGroupActive(associationModel.getTarget(), active);
		associationModel.setTarget(updated);
		this.displayMessage(
				new Message(MessageLevel.SUCCESS, MessageFormat.TEXT, String.format("Group %s has been %s",
						associationModel.getTarget().getName(), active ? "activated" : "deactivated")),
				MessageType.NOTIFICATION_MESSAGE);
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
							new Message(MessageLevel.SUCCESS, MessageFormat.TEXT,
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
		boolean hasSelection = associationModelSelected != null && associationModelSelected.getTarget() != null;
		if (hasSelection) {
			launchesSelected = this.associationLogic.retrieveLaunches(associationModelSelected.getTarget());
			this.selectedLaunchGrid.setItems(launchesSelected);
			this.drawerTitle.setText(associationModelSelected.getTarget().getName());
		}
		this.drawer.setVisible(hasSelection);

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
