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

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// The circuit breaker aspect is not active here: only the pending state is tested
class PackageVersionRefresherTest {

	private final PackageVersionRefresher refresher = new PackageVersionRefresher();

	@Test
	void when_refreshSucceeds_should_runItAndNotBePending() {
		AtomicInteger calls = new AtomicInteger();

		this.refresher.refresh(calls::incrementAndGet);

		assertThat(calls.get()).isEqualTo(1);
		assertThat(this.refresher.isPending()).isFalse();
	}

	@Test
	void when_fallbackIsCalled_should_bePendingUntilARefreshSucceeds() {
		// Fallback called by the circuit breaker on a failure or when the circuit is open
		ReflectionTestUtils.invokeMethod(this.refresher, "refreshFallback", (Runnable) () -> {
		}, new IllegalStateException("S3 unreachable"));
		assertThat(this.refresher.isPending()).isTrue();

		this.refresher.refresh(() -> {
		});
		assertThat(this.refresher.isPending()).isFalse();
	}

}
