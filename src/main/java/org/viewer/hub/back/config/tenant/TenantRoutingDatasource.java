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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.enums.ConnectorType;
import org.viewer.hub.back.model.property.ConnectorProperty;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Objects;

/**
 * Configuration of the routing for tenants
 */
@Component
public class TenantRoutingDatasource extends AbstractRoutingDataSource {

	private final String weasisManagerDataSourceUrl;

	private final String weasisManagerDataSourceUserName;

	private final String weasisManagerDataSourcePassword;

	private final String weasisManagerDataSourceDriverClassName;

	private final String weasisManagerDataSourceHikariPoolName;

	private final long weasisManagerDataSourceHikariIdleTimeout;

	private final TenantIdentifierResolver tenantIdentifierResolver;

	private final ConnectorConfigurationProperties connectorConfigurationProperties;

	/**
	 * Constructor.
	 * @param tenantIdentifierResolver TenantIdentifierResolver
	 * @param connectorConfigurationProperties Connector Configuration Properties
	 * @param weasisManagerDataSourceUrl DataSource Url for weasis-manager
	 * @param weasisManagerDataSourceUserName DataSourceUserName for weasis-manager
	 * @param weasisManagerDataSourcePassword DataSourcePassword for weasis-manager
	 * @param weasisManagerDataSourceDriverClassName DataSourceDriverClassName for
	 * weasis-manager
	 * @param weasisManagerDataSourceHikariPoolName DataSourceHikariPoolName for
	 * weasis-manager
	 * @param weasisManagerDataSourceHikariIdleTimeout DataSourceHikariIdleTimeout for
	 * weasis-manager
	 */
	@Autowired
	TenantRoutingDatasource(TenantIdentifierResolver tenantIdentifierResolver,
			ConnectorConfigurationProperties connectorConfigurationProperties,
			@Value("${spring.datasource.url}") String weasisManagerDataSourceUrl,
			@Value("${spring.datasource.username}") String weasisManagerDataSourceUserName,
			@Value("${spring.datasource.password}") String weasisManagerDataSourcePassword,
			@Value("${spring.datasource.driver-class-name}") String weasisManagerDataSourceDriverClassName,
			@Value("${spring.datasource.hikari.pool-name}") String weasisManagerDataSourceHikariPoolName,
			@Value("${spring.datasource.hikari.idle-timeout}") long weasisManagerDataSourceHikariIdleTimeout) {
		this.tenantIdentifierResolver = tenantIdentifierResolver;
		this.connectorConfigurationProperties = connectorConfigurationProperties;

		// Weasis manager database config
		this.weasisManagerDataSourceUrl = weasisManagerDataSourceUrl;
		this.weasisManagerDataSourceUserName = weasisManagerDataSourceUserName;
		this.weasisManagerDataSourcePassword = weasisManagerDataSourcePassword;
		this.weasisManagerDataSourceDriverClassName = weasisManagerDataSourceDriverClassName;
		this.weasisManagerDataSourceHikariPoolName = weasisManagerDataSourceHikariPoolName;
		this.weasisManagerDataSourceHikariIdleTimeout = weasisManagerDataSourceHikariIdleTimeout;

		// Set the weasis-manager application database
		this.setDefaultTargetDataSource(this.createWeasisManagerDataSource());

		// Set the different db connectors to the tenants routing
		this.fillTargetDataSourcesFromProperties(connectorConfigurationProperties);
	}

	/**
	 * Fill Targets DataSources From Properties
	 * @param connectorConfigurationProperties Connector Configuration Properties
	 */
	private void fillTargetDataSourcesFromProperties(
			ConnectorConfigurationProperties connectorConfigurationProperties) {
		HashMap<Object, Object> targetDataSources = new HashMap<>();
		connectorConfigurationProperties.getConnectors()
			.keySet()
			.stream()
			.filter(k -> Objects.nonNull(connectorConfigurationProperties.getConnectors().get(k)) && Objects
				.equals(connectorConfigurationProperties.getConnectors().get(k).getType(), ConnectorType.DB))
			.forEach(kdb -> {
				ConnectorProperty connector = connectorConfigurationProperties.getConnectors().get(kdb);
				targetDataSources.put(kdb,
						this.createDataSource(connector.getDbConnector().getDriver(),
								connector.getDbConnector().getUri(), connector.getDbConnector().getUser(),
								connector.getDbConnector().getPassword(), kdb));
			});
		this.setTargetDataSources(targetDataSources);
	}

	@Override
	protected String determineCurrentLookupKey() {
		return this.tenantIdentifierResolver.resolveCurrentTenantIdentifier();
	}

	/**
	 * Create a datasource from parameters
	 * @param driverClassName Driver ClassName
	 * @param jdbcUrl JDBC url
	 * @param userName User name
	 * @param pwd Password
	 * @param poolName Pool name
	 * @return Datasource created
	 */
	private DataSource createDataSource(String driverClassName, String jdbcUrl, String userName, String pwd,
			String poolName) {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(driverClassName);
		hikariConfig.setJdbcUrl(jdbcUrl);
		hikariConfig.setUsername(userName);
		hikariConfig.setPassword(pwd);
		hikariConfig.setPoolName(poolName);
		return new HikariDataSource(hikariConfig);
	}

	/**
	 * Create Hikari datasource for weasis-manager
	 * @return Datasource created
	 */
	private DataSource createWeasisManagerDataSource() {
		HikariDataSource dataSource = (HikariDataSource) this.createDataSource(
				this.weasisManagerDataSourceDriverClassName, this.weasisManagerDataSourceUrl,
				this.weasisManagerDataSourceUserName, this.weasisManagerDataSourcePassword,
				this.weasisManagerDataSourceHikariPoolName);
		dataSource.setIdleTimeout(this.weasisManagerDataSourceHikariIdleTimeout);
		return dataSource;
	}

}