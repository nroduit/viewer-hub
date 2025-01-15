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

package org.weasis.manager;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.weasis.manager.back.config.properties.ConnectorConfigurationProperties;
import org.weasis.manager.back.config.properties.EnvironmentOverrideProperties;
import org.weasis.manager.back.config.properties.WeasisPackageDefaultConfigurationProperties;

@SpringBootApplication
@EnableVaadin(value = "org.weasis.manager")
@EnableAsync
@EnableConfigurationProperties({ ConnectorConfigurationProperties.class,
		WeasisPackageDefaultConfigurationProperties.class, EnvironmentOverrideProperties.class })
@EnableCaching
@EnableScheduling
public class WeasisManagerApplication {

	public static void main(String[] args) {

		SpringApplication.run(WeasisManagerApplication.class, args);
	}

}
