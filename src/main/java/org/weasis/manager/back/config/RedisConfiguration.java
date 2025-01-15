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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.weasis.manager.back.constant.CacheName;
import org.weasis.manager.back.entity.PackageVersionEntity;
import org.weasis.manager.back.model.manifest.Manifest;

import java.time.Duration;

/**
 * Configuration for the cache redis
 */
@Configuration
public class RedisConfiguration {

	/**
	 * Serializer for the manifest
	 * @param connectionFactory connection factory
	 * @return RedisTemplate for the manifest
	 */
	@Bean(name = "manifestRedisTemplate")
	public RedisTemplate<String, Manifest> manifestRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Manifest> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(
				new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule()),
				Manifest.class));
		return template;
	}

	/**
	 * Serializer for the package version
	 * @param connectionFactory connection factory
	 * @return RedisTemplate for the package version
	 */
	@Bean(name = "packageVersionRedisTemplate")
	public RedisTemplate<String, PackageVersionEntity> packageVersionRedisTemplate(
			RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, PackageVersionEntity> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(
				new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule()),
				PackageVersionEntity.class));
		return template;
	}

	/**
	 * Configure cache names + ttl
	 * @return RedisCacheManagerBuilderCustomizer
	 */
	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder
			.withCacheConfiguration(CacheName.MANIFEST,
					RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(3)))
			.withCacheConfiguration(CacheName.PACKAGE_VERSION,
					RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ZERO));
	}

}
