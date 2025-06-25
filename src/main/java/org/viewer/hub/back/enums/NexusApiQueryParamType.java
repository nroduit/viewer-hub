package org.viewer.hub.back.enums;

import lombok.Getter;

/**
 * Query params used to query Nexus repository
 */
@Getter
public enum NexusApiQueryParamType {

	GROUP("group"), REPOSITORY("repository"), MAVEN_EXTENSION("maven.extension"), NAME("name"), VERSION("version"),
	ZIP_EXTENSION("zip");

	/**
	 * Code of the enum
	 */
	private final String code;

	/**
	 * Constructor
	 * @param code Code of the enum
	 */
	NexusApiQueryParamType(String code) {
		this.code = code;
	}

}
