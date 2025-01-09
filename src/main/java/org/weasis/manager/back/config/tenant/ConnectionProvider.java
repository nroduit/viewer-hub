/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.config.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class ConnectionProvider implements MultiTenantConnectionProvider {

	@Serial
	private static final long serialVersionUID = -8823226847957235585L;

	private final DataSource dataSource;

	@Autowired
	public ConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public Connection getConnection(Object o) throws SQLException {
		return this.dataSource.getConnection();
	}

	@Override
	public void releaseConnection(Object o, Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType);
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		if (MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType)) {
			return (T) this;
		}
		else {
			throw new UnknownUnwrapTypeException(unwrapType);
		}
	}

}