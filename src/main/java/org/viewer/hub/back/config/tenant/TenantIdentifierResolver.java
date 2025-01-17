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

package org.viewer.hub.back.config.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Use to identify the tenant to use.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

	// Current tenant: ThreadLocal is used to prevent modifications of other requests'
	// tenants
	private final ThreadLocal<String> currentTenant;

	public TenantIdentifierResolver() {
		this.currentTenant = new ThreadLocal<>();
	}

	public void setCurrentTenant(String tenantId) {
		this.currentTenant.set(tenantId);
	}

	@Override
	public String resolveCurrentTenantIdentifier() {
		return this.currentTenant.get();
	}

	public void clear() {
		this.currentTenant.remove();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}

}