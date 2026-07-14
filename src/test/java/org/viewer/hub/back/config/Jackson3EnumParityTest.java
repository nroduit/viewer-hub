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

package org.viewer.hub.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression guard for the Jackson 3 migration: enums must (de)serialize by {@code name()} and not
 * by Lombok {@code toString()}, both for the query-param deduction and the MVC JSON converter.
 */
class Jackson3EnumParityTest {

	@Test
	void jacksonDeductionResolvesViewerTypeFromName() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("viewer", "WEASIS");

		SearchCriteria criteria = SearchCriteria.jacksonDeduction(params);

		assertThat(criteria.getViewer()).isEqualTo(ViewerType.WEASIS);
	}

	@Test
	void jsonConverterMapperReadsAndWritesEnumByName() {
		JsonMapper mapper = (JsonMapper) new WebConfiguration(null, null).jacksonJsonHttpMessageConverter().getMapper();

		assertThat(mapper.writeValueAsString(ViewerType.WEASIS)).isEqualTo("\"WEASIS\"");
		assertThat(mapper.readValue("\"OHIF\"", ViewerType.class)).isEqualTo(ViewerType.OHIF);
	}

}
