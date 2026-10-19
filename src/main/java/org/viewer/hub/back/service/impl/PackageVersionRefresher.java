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

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes the refresh of the package versions behind a circuit breaker and remembers if
 * the last attempt failed, so that {@link PackageVersionRefreshRetryJob} can retry it.
 * <p>
 * It is a dedicated bean because the {@link CircuitBreaker} annotation is only applied on
 * calls going through the Spring proxy.
 */
@Component
@Slf4j
public class PackageVersionRefresher {

	public static final String PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER = "packageVersionRefresh";

	// True when the last refresh failed (S3/DB/network issue)
	private final AtomicBoolean pending = new AtomicBoolean(false);

	/**
	 * Run the refresh protected by the circuit breaker
	 * @param refresh refresh to execute
	 */
	@CircuitBreaker(name = PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER, fallbackMethod = "refreshFallback")
	public void refresh(Runnable refresh) {
		refresh.run();
		this.pending.set(false);
	}

	/**
	 * Fallback called on any failure of the refresh, including when the circuit is open
	 * @param refresh refresh that was requested
	 * @param throwable cause
	 */
	@SuppressWarnings("unused")
	private void refreshFallback(Runnable refresh, Throwable throwable) {
		this.pending.set(true);
		LOG.warn("Refresh of the package versions failed, it will be retried automatically: {}",
				throwable != null ? throwable.toString() : null);
		LOG.debug("Refresh of the package versions failure details", throwable);
	}

	/**
	 * @return true if the last refresh failed and should be retried
	 */
	public boolean isPending() {
		return this.pending.get();
	}

}
