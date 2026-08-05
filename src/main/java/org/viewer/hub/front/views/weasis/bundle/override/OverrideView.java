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

package org.viewer.hub.front.views.weasis.bundle.override;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.entity.OverrideConfigEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.enums.WeasisPropertyCategory;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageFormat;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.weasis.bundle.override.component.AddGroupConfigDialog;
import org.viewer.hub.front.views.weasis.bundle.override.component.GroupComboBox;
import org.viewer.hub.front.views.weasis.bundle.override.component.OverrideConfigGridItemDetail;
import org.viewer.hub.front.views.weasis.bundle.override.component.PackageOverrideGrid;
import org.viewer.hub.front.views.weasis.bundle.override.component.PackageVersionFileUpload;
import org.viewer.hub.front.views.weasis.bundle.override.component.PackageVersionUpload;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * View managing override of package configuration
 */
@PageTitle(OverrideView.VIEW_NAME)
@Route(AbstractView.WEASIS + OverrideView.ROUTE)
@Component
@UIScope
public class OverrideView extends AbstractView {

	@Serial
	private static final long serialVersionUID = 2988446382652942038L;

	public static final String ROUTE = "/override";

	public static final String VIEW_NAME = "Override";

	// Logic
	private final transient OverrideLogic overrideLogic;

	// UI components
	private Button refreshGridButton;

	private Button createNewGroupConfigButton;

	private Button deleteMappingMinimalVersionButton;

	private PackageOverrideGrid packageOverrideGrid;

	private final OverrideDataProvider<OverrideConfigEntity> overrideDataProvider;

	@Getter
	private PackageVersionUpload packageVersionUpload;

	// private UI ui;

	@Autowired
	public OverrideView(final OverrideLogic overrideLogic,
			final OverrideDataProvider<OverrideConfigEntity> overrideDataProvider) {
		this.overrideLogic = overrideLogic;
		this.overrideDataProvider = overrideDataProvider;

		// Set the view in the service
		this.overrideLogic.setOverrideView(this);

		// Build components
		this.buildComponents();

		// Add components in the view
		this.addComponentsView();

		// Add events listeners
		this.addEventListeners();
	}

	/**
	 * Event listeners:</br>
	 * - textfields displayed in the different tabs of the grid items details: when focus
	 * is not anymore on the texField, update the value of the textField in database
	 */
	private void addEventListeners() {
		// Upload package version
		this.uploadPackageVersionListener();
	}

	/**
	 * Handle upload of package version
	 */
	private void uploadPackageVersionListener() {
		PackageVersionFileUpload packageVersionFileUpload = this.packageVersionUpload.getPackageVersionFileUpload();
		UI ui = UI.getCurrent();

		// Manage the upload of the package version to add
		packageVersionFileUpload.setUploadHandler(UploadHandler.inMemory((metadata, data) -> {
			if (metadata.fileName() != null) {
				ui.access(() -> this.overrideLogic.handleUploadWeasisNative(data));
			}
		}));
	}

	/**
	 * Textfields displayed in the different tabs of the grid items details: when focus *
	 * is not anymore on the textfield, update the value of the textfield in database
	 */
	public void textFieldsPackageOverrideGridEventsListener() {
		// Retrieve the map containing all textFields
		Map<WeasisPropertyCategory, List<TextField>> allTextFieldsByCategory = this.packageOverrideGrid
			.getOverrideConfigGridItemDetail()
			.getAllTextFieldsByCategory();

		allTextFieldsByCategory.keySet()
			.forEach(k -> allTextFieldsByCategory.get(k).forEach(textField -> textField.addBlurListener(e -> {
				OverrideConfigGridItemDetail overrideConfigGridItemDetail = null;

				if (textField.getParent().isPresent() && textField.getParent().get().getParent().isPresent()
						&& textField.getParent().get().getParent().get().getParent().isPresent()) {
					// Retrieve the item detail corresponding to the textField
					overrideConfigGridItemDetail = (OverrideConfigGridItemDetail) textField.getParent()
						.get()
						.getParent()
						.get()
						.getParent()
						.get();
				}

				if (overrideConfigGridItemDetail != null && overrideConfigGridItemDetail.getBinder() != null
						&& overrideConfigGridItemDetail.getBinder().getBean() != null) {
					// Update in DB the override config
					this.overrideLogic.createUpdate(overrideConfigGridItemDetail.getBinder().getBean());
				}

				// Set back the textfield in read only
				textField.setReadOnly(true);
			})));
	}

	/**
	 * Build components
	 */
	private void buildComponents() {
		// Upload component
		this.packageVersionUpload = new PackageVersionUpload();

		// Grid + data provider + right click context menu
		this.packageOverrideGrid = new PackageOverrideGrid(this.overrideDataProvider,
				this.createComboBoxGroupValueProvider());
		this.overrideDataProvider.setFilter(this.packageOverrideGrid.getOverrideConfigFilter());
		this.packageOverrideGrid.setDataProvider(this.overrideDataProvider);
		this.createPackageOverrideGridContextMenu();

		// Refresh button
		this.refreshGridButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
		this.refreshGridButton.addClickListener(buttonClickEvent -> this.overrideDataProvider.refreshAll());
		this.refreshGridButton.getElement().getThemeList().add("primary");

		// Create new group config
		this.createNewGroupConfigButton = new Button("Create new group config", new Icon(VaadinIcon.PLUS));
		this.createNewGroupConfigButton.addClickListener(buttonClickEvent -> this.addNewGroupConfigListener());
		this.createNewGroupConfigButton.getElement().getThemeList().add("primary");

		// Delete mapping-minimal-version.json file stored in S3
		this.deleteMappingMinimalVersionButton = new Button("Delete compatibility file", new Icon(VaadinIcon.TRASH));
		this.deleteMappingMinimalVersionButton
			.addClickListener(buttonClickEvent -> this.deleteMappingMinimalVersionListener());
		this.deleteMappingMinimalVersionButton.getElement().getThemeList().add("error primary");
	}

	private ValueProvider<OverrideConfigEntity, GroupComboBox> createComboBoxGroupValueProvider() {
		// Retrieve all groups
		Set<TargetEntity> groups = this.overrideLogic.retrieveGroups();

		// Listener
		return overrideConfigEntity -> {
			GroupComboBox groupComboBox = new GroupComboBox(groups, overrideConfigEntity.getTarget());

			// Disable base configuration of each launch config of the bundle
			if (Objects.equals(overrideConfigEntity.getTarget().getType(), TargetType.DEFAULT)) {
				groupComboBox.setEnabled(false);
			}

			// Change listener => refresh model + update in backend
			groupComboBox.getComboBox().addValueChangeListener(event -> {
				// Modify target
				OverrideConfigEntity overrideConfigModified = new OverrideConfigEntity();
				overrideConfigModified.setTarget(event.getValue());
				overrideConfigModified.setLaunchConfig(overrideConfigEntity.getLaunchConfig());
				overrideConfigModified.setPackageVersion(overrideConfigEntity.getPackageVersion());

				if (!this.overrideLogic.doesOverrideConfigAlreadyExists(overrideConfigModified)) {
					// Modify OverrideConfig
					OverrideConfigEntity overrideConfigUpdated = this.overrideLogic.modifyTarget(overrideConfigEntity,
							event.getValue());
					if (overrideConfigUpdated != null) {
						// OverrideConfig has been updated
						this.displayMessage(
								new Message(MessageLevel.INFO, MessageFormat.TEXT,
										String.format("Override config %s has been updated", overrideConfigUpdated)),
								MessageType.NOTIFICATION_MESSAGE);
					}
					else {
						// OverrideConfig has not been updated
						this.displayMessage(
								new Message(MessageLevel.WARN, MessageFormat.TEXT, String
									.format("Override config %s has not been updated", overrideConfigModified)),
								MessageType.NOTIFICATION_MESSAGE);
					}
					this.packageOverrideGrid.getOverrideDataProvider().refreshAll();
				}
				else {
					// Override config has not been updated because already existing
					this.displayMessage(
							new Message(MessageLevel.WARN, MessageFormat.TEXT,
									String.format("Override config %s already existing!", overrideConfigModified)),
							MessageType.NOTIFICATION_MESSAGE);
				}
			});
			return groupComboBox;
		};
	}

	/**
	 * Handle right click event on the grid
	 */
	private void createPackageOverrideGridContextMenu() {
		GridContextMenu<OverrideConfigEntity> ctxMenu = this.packageOverrideGrid.addContextMenu();
		this.packageOverrideGrid.getElement()
			.addEventListener("vaadin-context-menu-before-open",
					this.packageOverrideGridContextMenuEventListener(ctxMenu));
	}

	/**
	 * Right-click on the grid
	 * @param ctxMenu Context menu
	 * @return DomEventListener
	 */
	@NotNull
	private DomEventListener packageOverrideGridContextMenuEventListener(
			GridContextMenu<OverrideConfigEntity> ctxMenu) {
		return e -> {
			Set<OverrideConfigEntity> items = this.packageOverrideGrid.getSelectedItems();
			ctxMenu.removeAll();
			if (items.isEmpty()) {
				ctxMenu.addItem("Please select a version in the grid");
			}
			else {
				// Dynamically populate the ctx menu
				this.buildDeleteItemContextMenu(ctxMenu, items);
			}
		};
	}

	/**
	 * Delete context menu
	 * @param ctxMenu context menu
	 * @param items Items
	 */
	private void buildDeleteItemContextMenu(GridContextMenu<OverrideConfigEntity> ctxMenu,
			Set<OverrideConfigEntity> items) {
		for (OverrideConfigEntity o : items) {
			String itemSelected = "%s%s/%s/%s".formatted(o.getPackageVersion().getVersionNumber(),
					o.getPackageVersion().getQualifier() == null ? "" : o.getPackageVersion().getQualifier(),
					o.getLaunchConfig().getName(), o.getTarget().getName());
			GridMenuItem<OverrideConfigEntity> deleteContextMenuItem = ctxMenu
				.addItem("Delete %s ?".formatted(itemSelected));
			deleteContextMenuItem.addComponentAsFirst(VaadinIcon.TRASH.create());
			deleteContextMenuItem.addMenuItemClickListener((ev) -> ev.getItem().ifPresent((ov) -> {
				try {
					// Delete version in volume
					this.overrideLogic.deleteVersion(ov);
					// Refresh grid
					this.overrideDataProvider.refreshAll();
					// Display message
					this.displayMessage(
							new Message(MessageLevel.INFO, MessageFormat.TEXT, "%s deleted".formatted(itemSelected)),
							MessageType.NOTIFICATION_MESSAGE);
				}
				catch (Exception e) {
					this.displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
							"%s not deleted".formatted(itemSelected)), MessageType.NOTIFICATION_MESSAGE);
				}
			}));
		}
	}

	/**
	 * Create and display a dialog to add a new group config
	 */
	private void addNewGroupConfigListener() {
		// Create and open dialog
		AddGroupConfigDialog addGroupConfigDialog = new AddGroupConfigDialog();
		addGroupConfigDialog.getPackageVersionComboBox().setItems(this.overrideLogic.retrievePackageVersions());
		addGroupConfigDialog.getGroupComboBox().setItems(this.overrideLogic.retrieveGroups());

		// Open the popup
		addGroupConfigDialog.open();

		// Listener on package version: retrieve only the launch configs which correspond
		// to the package version selected
		addGroupConfigDialog.getPackageVersionComboBox()
			.addValueChangeListener(event -> addGroupConfigDialog.getLaunchConfigComboBox()
				.setItems(this.overrideLogic.retrieveLaunchConfigsByPackageVersion(event.getValue())));

		// Listener on create button
		addGroupConfigDialog.getCreateButton().addClickListener(buttonClickEvent -> {
			// Validate inputs
			BinderValidationStatus<OverrideConfigEntity> validate = addGroupConfigDialog.getBinder().validate();

			if (validate.isOk()) {
				// Retrieve target to create
				OverrideConfigEntity overrideConfigToCreate = addGroupConfigDialog.getBinder().getBean();

				// Retrieve the values of the launch config from the default group and
				// copy them in the entity to create
				this.overrideLogic.copyAllValuesFromDefaultGroupExceptId(overrideConfigToCreate);

				// Create OverrideConfig
				OverrideConfigEntity overrideConfigCreated = this.overrideLogic.doesOverrideConfigAlreadyExists(
						overrideConfigToCreate) ? null : this.overrideLogic.createUpdate(overrideConfigToCreate);

				if (overrideConfigCreated != null) {
					// OverrideConfig has been created
					this.displayMessage(
							new Message(MessageLevel.INFO, MessageFormat.TEXT,
									String.format("Override config %s has been created", overrideConfigCreated)),
							MessageType.NOTIFICATION_MESSAGE);
					this.packageOverrideGrid.getOverrideDataProvider().refreshAll();
					addGroupConfigDialog.close();
				}
				else {
					// Override config has not been created because already existing
					this.displayMessage(
							new Message(MessageLevel.WARN, MessageFormat.TEXT, String
								.format("Override config %s already existing!", overrideConfigToCreate.toString())),
							MessageType.NOTIFICATION_MESSAGE);
				}
			}
		});
	}

	/**
	 * Display a warning confirmation popup and delete the mapping-minimal-version.json
	 * file stored in S3 if confirmed
	 */
	private void deleteMappingMinimalVersionListener() {
		ConfirmDialog confirmDialog = new ConfirmDialog();
		confirmDialog.setHeader("Delete compatibility file");
		confirmDialog.setText("Are you sure you want to delete the file mapping-minimal-version.json stored in S3? "
				+ "This action is irreversible and may impact the version resolution of Weasis packages.");

		confirmDialog.setCancelable(true);
		confirmDialog.setCancelText("Cancel");

		confirmDialog.setConfirmText("Delete");
		confirmDialog.setConfirmButtonTheme("error primary");
		confirmDialog.addConfirmListener(event -> {
			try {
				this.overrideLogic.deleteMappingMinimalVersionFile();
				this.displayMessage(
						new Message(MessageLevel.INFO, MessageFormat.TEXT,
								"File mapping-minimal-version.json has been deleted"),
						MessageType.NOTIFICATION_MESSAGE);
			}
			catch (Exception e) {
				this.displayMessage(
						new Message(MessageLevel.ERROR, MessageFormat.TEXT,
								"Issue when deleting file mapping-minimal-version.json"),
						MessageType.NOTIFICATION_MESSAGE);
			}
		});

		confirmDialog.open();
	}

	/**
	 * Add components in the view
	 */
	private void addComponentsView() {
		this.getStyle().set("display", "flex");
		this.getStyle().set("flex-direction", "column");

		this.packageVersionUpload.getStyle().set("flex-shrink", "0");
		this.add(this.packageVersionUpload);

		this.packageOverrideGrid.getStyle().set("flex-grow", "1");
		this.packageOverrideGrid.getStyle().set("min-height", "0");
		this.add(this.packageOverrideGrid);

		// Buttons: fixed to their natural height, never shrunk, pinned to the bottom,
		// sharing equally the full width of the grid above
		HorizontalLayout buttonLayout = new HorizontalLayout(this.refreshGridButton, this.createNewGroupConfigButton,
				this.deleteMappingMinimalVersionButton);
		buttonLayout.getStyle().set("flex-shrink", "0");
		buttonLayout.getStyle().set("margin-top", "8px");
		buttonLayout.setWidthFull();
		buttonLayout.setSpacing(true);
		buttonLayout.setFlexGrow(1, this.refreshGridButton, this.createNewGroupConfigButton,
				this.deleteMappingMinimalVersionButton);
		this.add(buttonLayout);

		this.setSizeFull();
		this.setWidthFull();
	}

	// TODO: refresh not working
	public void clearUploadedFileAndRefresh() {
		getUI().ifPresent(ui -> ui.access(() -> {
			// TODO refresh and clear list not working if user doesn't click on refresh
			// button
			// or click to remove uploaded file:
			this.packageVersionUpload.getPackageVersionFileUpload().clearFileList();
			this.packageOverrideGrid.getOverrideDataProvider().refreshAll();
		}));
	}

}
