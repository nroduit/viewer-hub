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

package org.viewer.hub.back.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionUtilTest {

	@Test
	void should_retrieveOnlyVersion_when_versionWithQualifier() {
		String versionToTest = "4.2.0-MGR";
		String toTest = VersionUtil.retrieveVersionWithoutQualifier(versionToTest);
		assertThat(toTest).isEqualTo("4.2.0");
	}

	@Test
	void should_retrieveOnlyQualifier_when_versionWithQualifier() {
		String versionToTest = "4.2.0-MGR";
		String toTest = VersionUtil.retrieveQualifierWithoutVersion(versionToTest);
		assertThat(toTest).isEqualTo("-MGR");
	}

	@Test
	void should_retrieveVersionRequested_when_versionWithoutQualifier() {
		String versionToTest = "4.2.0";
		String toTest = VersionUtil.retrieveVersionWithoutQualifier(versionToTest);
		assertThat(toTest).isEqualTo("4.2.0");
	}

	@Test
	void should_retrieveBlank_when_versionWithoutQualifier() {
		String versionToTest = "4.2.0";
		String toTest = VersionUtil.retrieveQualifierWithoutVersion(versionToTest);
		assertThat(toTest).isBlank();
	}

}
