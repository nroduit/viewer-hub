package org.weasis.manager.back.util;

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
