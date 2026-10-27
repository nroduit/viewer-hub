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

package org.viewer.hub.back.model.monitoring;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public final class ViewerAccumulator {

	private final String name;

	private long requests;

	private long errors;

	private long totalDuration;

	private long maxDuration;

	private LocalDateTime lastRequest;

	private final Set<String> users = new HashSet<>();

	/**
	 * @param name Viewer or archive the statistics are computed for
	 */
	public ViewerAccumulator(String name) {
		this.name = name;
	}

	public void add(LocalDateTime at, long duration, boolean success, String user) {
		this.requests++;
		this.errors += success ? 0 : 1;
		this.totalDuration += duration;
		this.maxDuration = Math.max(this.maxDuration, duration);
		if (this.lastRequest == null || at.isAfter(this.lastRequest)) {
			this.lastRequest = at;
		}
		if (user != null) {
			this.users.add(user);
		}
	}

	public ArchiveStatistics toArchiveStatistics() {
		return new ArchiveStatistics(this.name, this.requests, this.errors, (double) this.totalDuration / this.requests,
				this.maxDuration, this.users.size(), this.lastRequest);
	}

	public ViewerStatistics toViewerStatistics() {
		return new ViewerStatistics(this.name, this.requests, this.errors, (double) this.totalDuration / this.requests,
				this.maxDuration, this.users.size(), this.lastRequest);
	}

}
