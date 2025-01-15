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

package org.weasis.manager.back.util;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class Retryable<T> {

	private Supplier<T> action = () -> null;

	private Predicate<T> successCondition = ($) -> true;

	private int numberOfTries = 3;

	private long delay = 1000L;

	private Supplier<T> fallback = () -> null;

	public static <A> Retryable<A> of(Supplier<A> action) {
		return new Retryable<A>().run(action);
	}

	public Retryable<T> run(Supplier<T> action) {
		this.action = action;
		return this;
	}

	public Retryable<T> successIs(Predicate<T> successCondition) {
		this.successCondition = successCondition;
		return this;
	}

	public Retryable<T> retries(int numberOfTries) {
		this.numberOfTries = numberOfTries;
		return this;
	}

	public Retryable<T> delay(long delay) {
		this.delay = delay;
		return this;
	}

	public Retryable<T> orElse(Supplier<T> fallback) {
		this.fallback = fallback;
		return this;
	}

	public T execute() {
		for (int i = 0; i < this.numberOfTries; i++) {
			T t = this.action.get();
			if (this.successCondition.test(t)) {
				return t;
			}

			try {
				Thread.sleep(this.delay);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return this.fallback.get();
	}

}