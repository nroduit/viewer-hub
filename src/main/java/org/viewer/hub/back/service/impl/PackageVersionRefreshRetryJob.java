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
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.viewer.hub.back.service.PackageService;

/**
 * Retry the refresh of the package versions as long as the last attempt failed. The
 * refresh is protected by a circuit breaker (see {@link PackageVersionRefresher}) which
 * prevents hammering the infrastructure while it is unavailable: no retry is done while
 * the circuit is open, the trial call is made once it is half-open.
 */
@Component
@Slf4j
public class PackageVersionRefreshRetryJob {

	private final PackageService packageService;

	private final PackageVersionRefresher packageVersionRefresher;

	private final CircuitBreakerRegistry circuitBreakerRegistry;

	public PackageVersionRefreshRetryJob(PackageService packageService, PackageVersionRefresher packageVersionRefresher,
			CircuitBreakerRegistry circuitBreakerRegistry) {
		this.packageService = packageService;
		this.packageVersionRefresher = packageVersionRefresher;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	@Scheduled(fixedDelayString = "${viewer-hub.package-version-refresh.retry-delay-ms:60000}",
			initialDelayString = "${viewer-hub.package-version-refresh.retry-delay-ms:60000}")
	public void retryRefreshIfNeeded() {
		if (!this.packageVersionRefresher.isPending()) {
			return;
		}
		CircuitBreaker.State circuitState = this.circuitBreakerRegistry
			.circuitBreaker(PackageVersionRefresher.PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER)
			.getState();
		if (circuitState == CircuitBreaker.State.OPEN) {
			LOG.debug("Circuit breaker open: no retry of the refresh of the package versions");
			return;
		}
		LOG.info("Retrying the refresh of the package versions");
		try {
			this.packageService.refreshAvailablePackageVersion();
		}
		catch (Exception e) {
			LOG.warn("Retry of the refresh of the package versions failed: {}", e.toString());
		}
	}

}
