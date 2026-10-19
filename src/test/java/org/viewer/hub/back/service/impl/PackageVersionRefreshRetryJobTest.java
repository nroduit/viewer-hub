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

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.viewer.hub.back.service.PackageService;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class PackageVersionRefreshRetryJobTest {

	@Mock
	private PackageService packageService;

	@Mock
	private PackageVersionRefresher packageVersionRefresher;

	private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();

	private PackageVersionRefreshRetryJob retryJob;

	@BeforeEach
	void setUp() {
		this.retryJob = new PackageVersionRefreshRetryJob(this.packageService, this.packageVersionRefresher,
				this.circuitBreakerRegistry);
	}

	@Test
	void when_refreshIsPending_should_retryTheRefresh() throws Exception {
		Mockito.when(this.packageVersionRefresher.isPending()).thenReturn(true);

		this.retryJob.retryRefreshIfNeeded();

		Mockito.verify(this.packageService).refreshAvailablePackageVersion();
	}

	@Test
	void when_circuitIsHalfOpen_should_retryTheRefresh() throws Exception {
		Mockito.when(this.packageVersionRefresher.isPending()).thenReturn(true);
		this.circuitBreakerRegistry.circuitBreaker(PackageVersionRefresher.PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER)
			.transitionToOpenState();
		this.circuitBreakerRegistry.circuitBreaker(PackageVersionRefresher.PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER)
			.transitionToHalfOpenState();

		this.retryJob.retryRefreshIfNeeded();

		Mockito.verify(this.packageService).refreshAvailablePackageVersion();
	}

	@Test
	void when_circuitIsOpen_should_notRetryTheRefresh() throws Exception {
		Mockito.when(this.packageVersionRefresher.isPending()).thenReturn(true);
		this.circuitBreakerRegistry.circuitBreaker(PackageVersionRefresher.PACKAGE_VERSION_REFRESH_CIRCUIT_BREAKER)
			.transitionToOpenState();

		this.retryJob.retryRefreshIfNeeded();

		Mockito.verify(this.packageService, Mockito.never()).refreshAvailablePackageVersion();
	}

	@Test
	void when_noRefreshIsPending_should_doNothing() throws Exception {
		Mockito.when(this.packageVersionRefresher.isPending()).thenReturn(false);

		this.retryJob.retryRefreshIfNeeded();

		Mockito.verify(this.packageService, Mockito.never()).refreshAvailablePackageVersion();
	}

	@Test
	void when_retryFails_should_notPropagateTheException() throws Exception {
		Mockito.when(this.packageVersionRefresher.isPending()).thenReturn(true);
		Mockito.doThrow(new IOException("S3 unreachable")).when(this.packageService).refreshAvailablePackageVersion();

		this.retryJob.retryRefreshIfNeeded();

		Mockito.verify(this.packageService).refreshAvailablePackageVersion();
	}

}
