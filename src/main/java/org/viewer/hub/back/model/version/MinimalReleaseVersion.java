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

package org.viewer.hub.back.model.version;

import org.viewer.hub.back.util.VersionUtil;

import java.util.Objects;

/**
 * Model which represents the different versions released with its minimal native version
 */
public class MinimalReleaseVersion {

	private String releaseVersion;

	private String minimalVersion;

	private String i18nVersion;

	/**
	 * Empty constructor
	 */
	public MinimalReleaseVersion() {
		// Empty constructor
	}

	/**
	 * Constructor with parameters
	 * @param releaseVersion Release Version
	 * @param minimalVersion Minimal Version
	 */
	public MinimalReleaseVersion(String releaseVersion, String minimalVersion, String i18nVersion) {
		this.releaseVersion = releaseVersion;
		this.minimalVersion = minimalVersion;
		this.i18nVersion = i18nVersion;
	}

	public String getReleaseVersion() {
		return this.releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public String getMinimalVersion() {
		return this.minimalVersion;
	}

	public void setMinimalVersion(String minimalVersion) {
		this.minimalVersion = minimalVersion;
	}

	public String getI18nVersion() {
		return this.i18nVersion;
	}

	public void setI18nVersion(String i18nVersion) {
		this.i18nVersion = i18nVersion;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		MinimalReleaseVersion that = (MinimalReleaseVersion) o;
		return Objects.equals(this.releaseVersion, that.releaseVersion)
				&& Objects.equals(this.minimalVersion, that.minimalVersion)
				&& Objects.equals(this.i18nVersion, that.i18nVersion);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.releaseVersion, this.minimalVersion, this.i18nVersion);
	}

	public void cleaningQualifierForReleaseAndMinimalVersion() {
		// Release version
		String releaseVersionWithoutQualifier = VersionUtil.retrieveVersionWithoutQualifier(this.releaseVersion);
		this.releaseVersion = releaseVersionWithoutQualifier != null ? releaseVersionWithoutQualifier
				: this.releaseVersion;

		// Minimal version
		String minimalVersionWithoutQualifier = VersionUtil.retrieveVersionWithoutQualifier(this.minimalVersion);
		this.minimalVersion = minimalVersionWithoutQualifier != null ? minimalVersionWithoutQualifier
				: this.minimalVersion;
	}

}
