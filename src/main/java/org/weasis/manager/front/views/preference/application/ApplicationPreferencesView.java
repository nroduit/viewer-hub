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

package org.weasis.manager.front.views.preference.application;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.model.WeasisModule;
import org.weasis.manager.back.model.WeasisProfile;
import org.weasis.manager.front.layouts.MainLayout;
import org.weasis.manager.front.views.AbstractView;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Route(value = ApplicationPreferencesView.ROUTE, layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Secured({ "ROLE_admin" })
public class ApplicationPreferencesView extends AbstractView implements AfterNavigationObserver {

	@Serial
	private static final long serialVersionUID = -5300666253306864844L;

	public static final String ROUTE = "apppref";

	public static final String VIEW_NAME = "Preference";

	// CONTROLLER
	private final ApplicationPreferencesLogic logic;

	// UI COMPONENTS
	private FormLayout queryLayout;

	private ComboBox<TargetEntity> userSelector;

	private ComboBox<WeasisProfile> weasisProfileSelector;

	private ComboBox<WeasisModule> weasisModuleSelector;

	private TextArea preferencesFld;

	// DATA
	private List<TargetEntity> users;

	private ListDataProvider<TargetEntity> usersDataProvider;

	private List<WeasisProfile> weasisProfiles;

	private ListDataProvider<WeasisProfile> weasisProfilesDataProvider;

	private List<WeasisModule> weasisModules;

	private ListDataProvider<WeasisModule> weasisModulesDataProvider;

	private TargetEntity selectedUser;

	private WeasisProfile selectedWeasisProfile;

	private WeasisModule selectedWeasisModule;

	@Autowired
	public ApplicationPreferencesView(ApplicationPreferencesLogic logic) {
		// Bind the autowired service
		this.logic = logic;

		this.init();
		this.createView();

		// Set the view created in the service
		this.logic.setView(this);

		this.createMainLayout();

		this.add(this.mainLayout);
	}

	public void displayUsersField(List<TargetEntity> users) {
		this.users.clear();
		this.users.addAll(users);
		this.usersDataProvider.refreshAll();

		this.userSelector.setEnabled(true);

		if (users.isEmpty()) {
			this.userSelector.setPlaceholder("No users found!");
			// userSelector.setReadOnly(true); bug Vaadin -
			// https://github.com/vaadin/vaadin-combo-box-flow/issues/318
		}
		else {
			this.userSelector.setPlaceholder("Please select or type a User...");
			this.userSelector.setReadOnly(false);
			this.userSelector.focus();
		}
	}

	public void displayWeasisProfilesField(List<WeasisProfile> weasisProfiles) {
		this.weasisProfiles.clear();
		this.weasisProfiles.addAll(weasisProfiles);
		this.weasisProfilesDataProvider.refreshAll();

		this.weasisProfileSelector.setEnabled(true);

		if (weasisProfiles.isEmpty()) {
			this.weasisProfileSelector.setPlaceholder("No Weasis Profile found for this user!");
			// weasisProfileSelector.setReadOnly(true); bug Vaadin -
			// https://github.com/vaadin/vaadin-combo-box-flow/issues/318
		}
		else {
			this.weasisProfileSelector.setPlaceholder("Please select or type a Weasis Profile...");
			this.weasisProfileSelector.setReadOnly(false);
			this.weasisProfileSelector.focus();
		}
	}

	public void displayWeasisModulesField(List<WeasisModule> weasisModules) {
		this.weasisModules.clear();
		this.weasisModules.addAll(weasisModules);
		this.weasisModulesDataProvider.refreshAll();

		this.weasisModuleSelector.setEnabled(true);

		if (weasisModules.isEmpty()) {
			this.weasisModuleSelector.setPlaceholder("No Weasis Module found for this user and this Weasis Profile!");
			// weasisModuleSelector.setReadOnly(true); bug Vaadin -
			// https://github.com/vaadin/vaadin-combo-box-flow/issues/318
		}
		else {
			this.weasisModuleSelector.setPlaceholder("Please select or type a Weasis Module...");
			this.weasisModuleSelector.setReadOnly(false);
			this.weasisModuleSelector.focus();
		}
	}

	public void displayPreferencesField(String preferences) {
		this.preferencesFld.setEnabled(true);

		if (preferences != null && !preferences.isEmpty()) {
			this.preferencesFld.setValue(preferences);
		}
		else {
			this.preferencesFld.setValue("No preferences found for this user, Weasis Profile and Weasis Module!");
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		this.logic.viewOpened();
	}

	private void init() {
		this.users = new ArrayList<>();

		this.usersDataProvider = DataProvider.ofCollection(this.users);
		this.usersDataProvider.setSortOrder(TargetEntity::getName, SortDirection.ASCENDING);

		this.weasisProfiles = new ArrayList<>();

		this.weasisProfilesDataProvider = DataProvider.ofCollection(this.weasisProfiles);
		this.weasisProfilesDataProvider.setSortOrder(WeasisProfile::getName, SortDirection.ASCENDING);

		this.weasisModules = new ArrayList<>();

		this.weasisModulesDataProvider = DataProvider.ofCollection(this.weasisModules);
		this.weasisModulesDataProvider.setSortOrder(WeasisModule::getName, SortDirection.ASCENDING);
	}

	private void createView() {
		this.getStyle().set("min-width", "300px");
		this.setSizeFull();
	}

	private void createMainLayout() {
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setSizeFull();

		this.createQueryLayout();

		this.mainLayout.add(this.queryLayout, this.preferencesFld);
	}

	private void createQueryLayout() {
		this.queryLayout = new FormLayout();
		this.queryLayout.setWidth("100%");
		this.queryLayout.setResponsiveSteps(new ResponsiveStep("0px", 1), new ResponsiveStep("500px", 3));

		this.createUserSelector();
		this.createWeasisProfileSelector();
		this.createWeasisModuleSelector();
		this.createPreferencesField();

		this.queryLayout.add(this.userSelector, this.weasisProfileSelector, this.weasisModuleSelector);
	}

	@SuppressWarnings("serial")
	private void createUserSelector() {
		this.userSelector = new ComboBox<>();
		this.userSelector.setAllowCustomValue(false);
		this.userSelector.setLabel("User");
		this.userSelector.setItemLabelGenerator(TargetEntity::getName);
		this.userSelector.setClearButtonVisible(true);
		this.userSelector.setEnabled(false);

		this.userSelector.setItems(this.usersDataProvider);

		this.userSelector.addValueChangeListener(new ValueChangeListener<ValueChangeEvent<TargetEntity>>() {

			@Override
			public void valueChanged(ValueChangeEvent<TargetEntity> event) {
				if (event.getValue() != null) {
					ApplicationPreferencesView.this.selectedUser = event.getValue();
					ApplicationPreferencesView.this.logic.userSelection(ApplicationPreferencesView.this.selectedUser);
				}
				else {
					ApplicationPreferencesView.this
						.clearAndDisable(ApplicationPreferencesView.this.weasisProfileSelector);
					ApplicationPreferencesView.this
						.clearAndDisable(ApplicationPreferencesView.this.weasisModuleSelector);
					ApplicationPreferencesView.this.clearAndDisable(ApplicationPreferencesView.this.preferencesFld);

					ApplicationPreferencesView.this.userSelector.focus();
				}
			}
		});
	}

	@SuppressWarnings("serial")
	private void createWeasisProfileSelector() {
		this.weasisProfileSelector = new ComboBox<>();
		this.weasisProfileSelector.setItems(new ArrayList<>());
		this.weasisProfileSelector.setAllowCustomValue(false);
		this.weasisProfileSelector.setLabel("Weasis Profile");
		this.weasisProfileSelector.setItemLabelGenerator(WeasisProfile::getName);
		this.weasisProfileSelector.setClearButtonVisible(true);
		this.weasisProfileSelector.setEnabled(false);

		this.weasisProfileSelector.setItems(this.weasisProfilesDataProvider);

		this.weasisProfileSelector.addValueChangeListener(new ValueChangeListener<ValueChangeEvent<WeasisProfile>>() {

			@Override
			public void valueChanged(ValueChangeEvent<WeasisProfile> event) {
				if (event.getValue() != null) {
					ApplicationPreferencesView.this.selectedWeasisProfile = event.getValue();
					ApplicationPreferencesView.this.logic.weasisProfileSelection(
							ApplicationPreferencesView.this.selectedUser,
							ApplicationPreferencesView.this.selectedWeasisProfile);
				}
				else {
					ApplicationPreferencesView.this
						.clearAndDisable(ApplicationPreferencesView.this.weasisModuleSelector);
					ApplicationPreferencesView.this.clearAndDisable(ApplicationPreferencesView.this.preferencesFld);

					ApplicationPreferencesView.this.weasisProfileSelector.focus();
				}
			}

		});
	}

	@SuppressWarnings("serial")
	private void createWeasisModuleSelector() {
		this.weasisModuleSelector = new ComboBox<>();
		this.weasisModuleSelector.setItems(new ArrayList<>());
		this.weasisModuleSelector.setAllowCustomValue(false);
		this.weasisModuleSelector.setLabel("Weasis Module");
		this.weasisModuleSelector.setItemLabelGenerator(WeasisModule::getName);
		this.weasisModuleSelector.setClearButtonVisible(true);
		this.weasisModuleSelector.setEnabled(false);

		this.weasisModuleSelector.setItems(this.weasisModulesDataProvider);

		this.weasisModuleSelector.addValueChangeListener(new ValueChangeListener<ValueChangeEvent<WeasisModule>>() {

			@Override
			public void valueChanged(ValueChangeEvent<WeasisModule> event) {
				if (event.getValue() != null) {
					ApplicationPreferencesView.this.selectedWeasisModule = event.getValue();
					ApplicationPreferencesView.this.logic.weasisModuleSelection(
							ApplicationPreferencesView.this.selectedUser,
							ApplicationPreferencesView.this.selectedWeasisProfile,
							ApplicationPreferencesView.this.selectedWeasisModule);
				}
				else {
					ApplicationPreferencesView.this.clearAndDisable(ApplicationPreferencesView.this.preferencesFld);

					ApplicationPreferencesView.this.weasisModuleSelector.focus();
				}
			}
		});
	}

	private void createPreferencesField() {
		this.preferencesFld = new TextArea();
		this.preferencesFld.setSizeFull();
		this.preferencesFld.setLabel("Preferences");
		// preferencesFld.setReadOnly(true); //
		// https://github.com/vaadin/vaadin-text-field-flow/issues/129
		this.preferencesFld.setEnabled(false);
		this.preferencesFld.getStyle().set("overflow-y", "auto");
	}

	private void clearAndDisable(Component component) {
		if (component instanceof ComboBox<?>) {
			((ComboBox<?>) component).clear();
			((ComboBox<?>) component).setItems(new ArrayList<>());
			((ComboBox<?>) component).setEnabled(false);
		}
		else if (component instanceof TextArea) {
			((TextArea) component).clear();
			((TextArea) component).setEnabled(false);
		}
	}

}
