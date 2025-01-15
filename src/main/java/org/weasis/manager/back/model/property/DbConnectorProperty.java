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

package org.weasis.manager.back.model.property;

import java.util.Objects;

public class DbConnectorProperty {

	private String user;

	private String password;

	private String uri;

	private String driver;

	private DbConnectorQueryProperty query;

	public DbConnectorProperty(String user, String password, String uri, String driver,
			DbConnectorQueryProperty query) {
		this.user = user;
		this.password = password;
		this.uri = uri;
		this.driver = driver;
		this.query = query;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDriver() {
		return this.driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public DbConnectorQueryProperty getQuery() {
		return this.query;
	}

	public void setQuery(DbConnectorQueryProperty query) {
		this.query = query;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		DbConnectorProperty that = (DbConnectorProperty) o;
		return Objects.equals(this.user, that.user) && Objects.equals(this.password, that.password)
				&& Objects.equals(this.uri, that.uri) && Objects.equals(this.driver, that.driver)
				&& Objects.equals(this.query, that.query);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.user, this.password, this.uri, this.driver, this.query);
	}

}
