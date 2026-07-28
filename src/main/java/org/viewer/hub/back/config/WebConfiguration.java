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

package org.viewer.hub.back.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.viewer.hub.back.config.s3.S3ClientConfigurationProperties;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import java.io.IOException;

/**
 * Configuration for the Spring MVC part, serialization/deserialization Jackson, resources
 * packages
 */
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

	@Value("${viewer-hub.resources-packages.weasis.path}")
	private String viewerHubResourcesPackagesWeasisPath;

	private final S3ClientConfigurationProperties s3config;

	private final ResourceLoader resourceLoader;

	@Autowired
	public WebConfiguration(S3ClientConfigurationProperties s3config, ResourceLoader resourceLoader) {
		this.s3config = s3config;
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
		// The builder is pre-populated with the default converters (String, byte[], JSON, ...);
		// override the XML one (to keep the XML declaration) and the JSON one (to keep the
		// Jackson 2 enum-by-name behaviour that clients and launch URLs rely on).
		builder.withXmlConverter(this.jacksonXmlHttpMessageConverter())
			.withJsonConverter(this.jacksonJsonHttpMessageConverter());
	}

	/**
	 * Setup of the json jackson mapper (Jackson 3 / tools.jackson).
	 * <p>
	 * Jackson 3 reads/writes enums using {@code toString()} by default; disabling the enum
	 * {@code *_USING_TO_STRING} features restores the Jackson 2 behaviour of using {@code name()},
	 * so values such as {@code ViewerType} "WEASIS" keep (de)serializing correctly. Other Jackson 3
	 * defaults (ISO-8601 dates, discovered modules) are left untouched.
	 * @return Converter built
	 */
	@Bean
	public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
		JsonMapper mapper = JsonMapper.builder()
			.findAndAddModules()
			.disable(EnumFeature.READ_ENUMS_USING_TO_STRING, EnumFeature.WRITE_ENUMS_USING_TO_STRING)
			.build();
		return new JacksonJsonHttpMessageConverter(mapper);
	}

	/**
	 * Setup of the xml jackson mapper (Jackson 3 / tools.jackson)
	 * @return Converter built
	 */
	@Bean
	public JacksonXmlHttpMessageConverter jacksonXmlHttpMessageConverter() {
		// configureForJackson2() keeps the Jackson 2 serialization defaults (enum-as-name,
		// dates-as-timestamps, no xsi:nil for null values, ...) so the manifest XML is unchanged.
		// enable(WRITE_XML_DECLARATION) adds the xml declaration tag to each xml serialization.
		XmlMapper mapper = XmlMapper.builder()
			.configureForJackson2()
			.enable(XmlWriteFeature.WRITE_XML_DECLARATION)
			.build();
		return new JacksonXmlHttpMessageConverter(mapper);
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
					.getResource("s3://%s/%s/%s".formatted(WebConfiguration.this.s3config.getBucket(),
							WebConfiguration.this.viewerHubResourcesPackagesWeasisPath, resourcePath));
				if (s3Resource.exists() && s3Resource.isReadable()) {
					return s3Resource;
				}
				return super.getResource(resourcePath, location);
			}
		};
	}

}
