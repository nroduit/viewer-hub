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
package org.viewer.hub.back.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.util.SecurityUtil;
import org.viewer.hub.front.authentication.NotAuthorizedScreen;
import org.viewer.hub.front.help.HelpView;
import org.viewer.hub.front.views.association.AssociationView;
import org.viewer.hub.front.views.override.OverrideView;
import org.viewer.hub.front.views.preference.application.ApplicationPreferencesView;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class UIServiceInitListener implements VaadinServiceInitListener {

	// All view classes
	private static final List<? extends Class<? extends com.vaadin.flow.component.Component>> viewClasses = Arrays
		.asList(ApplicationPreferencesView.class, AssociationView.class, OverrideView.class, HelpView.class);

	@Serial
	private static final long serialVersionUID = -1808906248435713207L;

	/**
	 * Listen for the initialization of the UI (the internal root component in Vaadin) and
	 * then add a listener before every view transition
	 * @param event ServiceInitEvent
	 */
	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {

			final UI ui = uiEvent.getUI();
			ui.addBeforeEnterListener(this::beforeEnter);
		});
	}

	/**
	 * Check authorized view to display to the user
	 *
	 * <p>
	 * If none redirect to the unauthorized view
	 * @param event BeforeEnterEvent
	 */
	private void beforeEnter(BeforeEnterEvent event) {

		// Root view
		boolean isRootView = Objects.equals(event.getNavigationTarget().getName(),
				"org.weasis.manager.front.views.preferences.application.ApplicationPreferencesView");

		if (SecurityUtil.isUserLoggedIn() && !SecurityUtil.isAccessGranted(event.getNavigationTarget())) {
			// If root requested
			if (isRootView) {
				// List all authorized views and take first one if user request root of
				// the application
				// Try to find first authorized view
				Optional<? extends Class<? extends com.vaadin.flow.component.Component>> firstAuthorizedViewFoundOpt = viewClasses
					.stream()
					.filter(SecurityUtil::isAccessGranted)
					.findFirst();

				// If an authorized view have been found
				if (firstAuthorizedViewFoundOpt.isPresent()) {
					event.rerouteTo(firstAuthorizedViewFoundOpt.get());
				}
				else {
					// No authorized view has been found
					event.rerouteTo(NotAuthorizedScreen.class);
				}
			}
			else {
				// Case direct access not authorized
				event.rerouteTo(NotAuthorizedScreen.class);
			}
		}
	}

}
