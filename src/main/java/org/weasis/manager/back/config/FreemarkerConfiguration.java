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

package org.weasis.manager.back.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FreemarkerConfiguration implements BeanPostProcessor {

	@Value("${weasis-manager.server.url}")
	private String weasisManagerServerUrl;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof FreeMarkerConfigurer configurer) {
			Map<String, Object> sharedVariables = new HashMap<>();
			sharedVariables.put("weasisManagerServerUrl", this.weasisManagerServerUrl);
			configurer.setFreemarkerVariables(sharedVariables);
		}
		else if (bean instanceof FreeMarkerViewResolver resolver) {
			resolver.setContentType(MediaType.TEXT_XML_VALUE + ";charset=UTF-8");
		}
		return bean;
	}

}