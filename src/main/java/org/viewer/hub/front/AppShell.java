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
package org.viewer.hub.front;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Application shell configuration.
 *
 * <p>
 * Vaadin 25 replaces the Lumo-based {@code @Theme} annotation with the Aura theme loaded
 * via {@code @StyleSheet(Aura.STYLESHEET)}. The application's own styles (formerly pulled
 * in through the {@code viewer-hub} theme folder) are now loaded explicitly with
 * {@code @CssImport}.
 */
@StyleSheet(Aura.STYLESHEET)
@CssImport("./themes/viewer-hub/styles.css")
public class AppShell implements AppShellConfigurator {

}
