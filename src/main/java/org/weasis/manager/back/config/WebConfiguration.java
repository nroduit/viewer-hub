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

package org.weasis.manager.back.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.weasis.manager.back.config.s3.S3ClientConfigurationProperties;

import java.io.IOException;
import java.util.List;

/**
 * Configuration for the Spring MVC part, serialization/deserialization Jackson, resources
 * packages
 */
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

	@Value("${weasis-manager.resources-packages.weasis.path}")
	private String weasisManagerResourcesPackagesWeasisPath;

	private final S3ClientConfigurationProperties s3config;

	private final ResourceLoader resourceLoader;

	@Autowired
	public WebConfiguration(S3ClientConfigurationProperties s3config, ResourceLoader resourceLoader) {
		this.s3config = s3config;
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(this.mappingJackson2XmlHttpMessageConverter(new Jackson2ObjectMapperBuilder()));
		converters.add(new StringHttpMessageConverter());
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new MappingJackson2HttpMessageConverter());
	}

	/**
	 * Setup of the xml jackson mapper
	 * @param builder Builder
	 * @return Converter built
	 */
	@Bean
	public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(
			Jackson2ObjectMapperBuilder builder) {
		ObjectMapper mapper = builder.createXmlMapper(true).build();
		// Set the xml tag to each xml serialization
		((XmlMapper) mapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
		return new MappingJackson2XmlHttpMessageConverter(mapper);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// resource handler for images, icons, packages
		registry.addResourceHandler("/img/**").addResourceLocations("classpath:META-INF/resources/img/");
		registry.addResourceHandler("/icons/**").addResourceLocations("classpath:META-INF/resources/icons/");
		registry.addResourceHandler("/logo/**").addResourceLocations("classpath:META-INF/resources/logo/");
		registry.addResourceHandler("/weasis/**")
			.addResourceLocations("s3://%s/".formatted(this.s3config.getBucket()))
			.resourceChain(true)
			.addResolver(this.s3WeasisResourceResolver());
	}

	/**
	 * Retrieve the resource in S3 for the resource handler /weasis
	 * @return PathResourceResolver created
	 */
	@NotNull
	private PathResourceResolver s3WeasisResourceResolver() {
		return new PathResourceResolver() {
			@Override
			protected Resource getResource(@NotNull String resourcePath, @NotNull Resource location)
					throws IOException {
				// Retrieve the s3 resource
				Resource s3Resource = WebConfiguration.this.resourceLoader
					.getResource("s3://%s%s/%s".formatted(WebConfiguration.this.s3config.getBucket(),
							WebConfiguration.this.weasisManagerResourcesPackagesWeasisPath, resourcePath));
				if (s3Resource.exists() && s3Resource.isReadable()) {
					return s3Resource;
				}
				return super.getResource(resourcePath, location);
			}
		};
	}

}
