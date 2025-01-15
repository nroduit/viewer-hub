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

package org.weasis.manager.front.views.i18n;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.weasis.manager.back.entity.I18nEntity;
import org.weasis.manager.back.model.Message;
import org.weasis.manager.back.model.MessageFormat;
import org.weasis.manager.back.model.MessageLevel;
import org.weasis.manager.back.model.MessageType;
import org.weasis.manager.front.layouts.MainLayout;
import org.weasis.manager.front.views.AbstractView;
import org.weasis.manager.front.views.i18n.component.I18nFileUpload;
import org.weasis.manager.front.views.i18n.component.I18nGrid;
import org.weasis.manager.front.views.i18n.component.I18nUpload;

import java.io.Serial;
import java.util.Set;

import static org.weasis.manager.back.constant.PropertiesFileName.I18N_PATTERN_NAME;

/**
 * View managing I18n versions
 */
@Route(value = I18nView.ROUTE, layout = MainLayout.class)
@Secured({ "ROLE_admin" })
@Getter
public class I18nView extends AbstractView {

	@Serial
	private static final long serialVersionUID = -2338629200185539985L;

	public static final String ROUTE = "i18n";

	public static final String VIEW_NAME = "Translation";

	// Logic
	private final transient I18nLogic i18nLogic;

	// UI components
	private Button refreshGridButton;

	private I18nGrid i18nGrid;

	private final I18nDataProvider<I18nEntity> i18nDataProvider;

	private I18nUpload i18nUpload;

	@Autowired
	public I18nView(final I18nLogic i18nLogic, final I18nDataProvider<I18nEntity> i18nDataProvider) {
		this.i18nLogic = i18nLogic;
		this.i18nDataProvider = i18nDataProvider;

		// Set the view in the service
		this.i18nLogic.setI18nView(this);

		// Build components
		this.buildComponents();

		// Add components in the view
		this.addComponentsView();

		// Add events listeners
		this.addEventListeners();
	}

	/**
	 * Event listeners:
	 */
	private void addEventListeners() {
		// Upload i18n
		this.uploadI18nVersionListener();
	}

	/**
	 * Handle upload of translation i18n
	 */
	private void uploadI18nVersionListener() {
		I18nFileUpload i18nFileUpload = this.i18nUpload.getI18nFileUpload();
		// Manage the upload of the i18n version to add
		i18nFileUpload.addSucceededListener(event -> {
			if (event.getFileName() != null && event.getFileName().contains(I18N_PATTERN_NAME)) {
				this.i18nLogic.handleUploadI18n(i18nFileUpload.getMemoryBuffer().getInputStream(), event.getFileName());
			}

		});
	}

	/**
	 * Build components
	 */
	private void buildComponents() {
		// Upload component
		this.i18nUpload = new I18nUpload();

		// Grid + data provider + right click context menu
		this.i18nGrid = new I18nGrid(this.i18nDataProvider);
		this.i18nDataProvider.setFilter(this.i18nGrid.getI18nFilter());
		this.i18nGrid.setDataProvider(this.i18nDataProvider);
		this.createI18nGridContextMenu();

		// Refresh button
		this.refreshGridButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
		this.refreshGridButton.addClickListener(buttonClickEvent -> this.i18nDataProvider.refreshAll());
		this.refreshGridButton.setMinWidth("50%");
	}

	/**
	 * Handle right click event on the grid
	 */
	private void createI18nGridContextMenu() {
		GridContextMenu<I18nEntity> ctxMenu = this.i18nGrid.addContextMenu();
		this.i18nGrid.getElement()
			.addEventListener("vaadin-context-menu-before-open", this.i18nGridContextMenuEventListener(ctxMenu));
	}

	/**
	 * Right-click on the grid
	 * @param ctxMenu Context menu
	 * @return DomEventListener
	 */
	@NotNull
	private DomEventListener i18nGridContextMenuEventListener(GridContextMenu<I18nEntity> ctxMenu) {
		return e -> {
			Set<I18nEntity> items = this.i18nGrid.getSelectedItems();
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
	private void buildDeleteItemContextMenu(GridContextMenu<I18nEntity> ctxMenu, Set<I18nEntity> items) {
		for (I18nEntity i : items) {
			String itemSelected = "%s%s".formatted(i.getVersionNumber(),
					i.getQualifier() == null ? "" : i.getQualifier());
			GridMenuItem<I18nEntity> deleteContextMenuItem = ctxMenu.addItem("Delete %s ?".formatted(itemSelected));
			deleteContextMenuItem.addComponentAsFirst(VaadinIcon.TRASH.create());
			deleteContextMenuItem.addMenuItemClickListener(ev -> ev.getItem().ifPresent(ov -> {
				try {
					// Delete version in volume
					this.i18nLogic.deleteVersion(ov);
					// Refresh grid
					this.i18nDataProvider.refreshAll();
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
	 * Add components in the view
	 */
	private void addComponentsView() {
		// Upload
		this.add(this.i18nUpload);
		// Grid
		this.add(this.i18nGrid);
		// Buttons
		HorizontalLayout buttonLayout = new HorizontalLayout(this.refreshGridButton);
		buttonLayout.setWidthFull();

		this.add(buttonLayout);
		this.setSizeFull();
		this.setWidthFull();
	}

}
