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
package org.viewer.hub.back.service.impl;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check that the {@code @CircuitBreaker} annotation of {@link PackageVersionRefresher} is
 * really applied with the Resilience4j / Spring Boot 4 auto-configuration.
 */
class PackageVersionRefresherCircuitBreakerTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(CircuitBreakerAutoConfiguration.class))
		.withUserConfiguration(TestConfiguration.class)
		.withPropertyValues(
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.sliding-window-type=COUNT_BASED",
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.sliding-window-size=2",
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.minimum-number-of-calls=2",
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.failure-rate-threshold=100",
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.wait-duration-in-open-state=300ms",
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.automatic-transition-from-open-to-half-open-enabled=true",
				"resilience4j.circuitbreaker.instances.packageVersionRefresh.permitted-number-of-calls-in-half-open-state=1");

	@Test
	void when_refreshFailsRepeatedly_should_openTheCircuitAndRecoverOnceInfraIsBack() {
		this.contextRunner.run(context -> {
			PackageVersionRefresher refresher = context.getBean(PackageVersionRefresher.class);
			CircuitBreaker circuitBreaker = context.getBean(CircuitBreakerRegistry.class)
				.circuitBreaker(PackageVersionRefresher.PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER);
			AtomicInteger executions = new AtomicInteger();
			Runnable failing = () -> {
				executions.incrementAndGet();
				throw new IllegalStateException("S3 unreachable");
			};

			// Failures are caught by the fallback: nothing propagated, retry requested
			refresher.refresh(failing);
			assertThat(refresher.isPending()).isTrue();
			assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
			refresher.refresh(failing);
			assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
			assertThat(executions.get()).isEqualTo(2);

			// Circuit open: the refresh is not executed but the fallback keeps the retry
			// pending
			refresher.refresh(failing);
			assertThat(executions.get()).isEqualTo(2);
			assertThat(refresher.isPending()).isTrue();

			// After the wait duration the circuit is half-open: the trial call succeeds
			Thread.sleep(500);
			assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
			AtomicInteger recovered = new AtomicInteger();
			refresher.refresh(recovered::incrementAndGet);
			assertThat(recovered.get()).isEqualTo(1);
			assertThat(refresher.isPending()).isFalse();
			assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
		});
	}

	@Configuration
	@EnableAspectJAutoProxy
	static class TestConfiguration {

		@Bean
		PackageVersionRefresher packageVersionRefresher() {
			return new PackageVersionRefresher();
		}

	}

}
