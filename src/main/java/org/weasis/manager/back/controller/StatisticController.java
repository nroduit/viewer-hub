/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.constant.MDCConstants;
import org.weasis.manager.back.model.PerformanceModel;
import org.weasis.manager.back.util.MDCUtil;

import java.time.Instant;

@RestController
@RequestMapping(EndPoint.STATISTIC_PATH)
@Tag(name = "Statistics", description = "API Endpoints for statistics")
@Validated
@Slf4j
public class StatisticController {

	// Mapping generic parameter
	private static final String MDC_WEASIS_USER_ID = MDCConstants.MDC_PARAM1;

	private static final String MDC_WEASIS_HOST = MDCConstants.MDC_PARAM2;

	private static final String MDC_WEASIS_TYPE = MDCConstants.MDC_PARAM3;

	private static final String MDC_WEASIS_SERIE_UID = MDCConstants.MDC_PARAM4;

	private static final String MDC_WEASIS_MODALITY = MDCConstants.MDC_PARAM5;

	private static final String MDC_WEASIS_IMAGE_NUMBER = MDCConstants.MDC_PARAM6;

	private static final String MDC_WEASIS_SIZE = MDCConstants.MDC_PARAM7;

	private static final String MDC_WEASIS_TIME = MDCConstants.MDC_PARAM8;

	private static final String MDC_WEASIS_RATE = MDCConstants.MDC_PARAM9;

	private static final String MDC_WEASIS_ERRORS = MDCConstants.MDC_PARAM10;

	/**
	 * Log performances of weasis in Kibana
	 * @param performanceModel Performance model
	 */
	@Operation(summary = "Log performances of weasis in Kibana", description = "Log performances of weasis in Kibana",
			tags = "Statistic")
	@PostMapping(value = "/perf", consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public void logWeasisPerf(@RequestBody @Valid PerformanceModel performanceModel) {
		Instant tStart = Instant.now();
		try {
			MDCUtil.startWSLogging("logWeasisPerf", null, "Weasis");
			MDC.put(MDC_WEASIS_USER_ID, performanceModel.getUserId());
			MDC.put(MDC_WEASIS_HOST, performanceModel.getHost());
			MDC.put(MDC_WEASIS_TYPE, performanceModel.getType());
			MDC.put(MDC_WEASIS_SERIE_UID, performanceModel.getSeriesUID());
			MDC.put(MDC_WEASIS_MODALITY, performanceModel.getModality());
			MDC.put(MDC_WEASIS_IMAGE_NUMBER, Integer.toString(performanceModel.getNbImages()));
			MDC.put(MDC_WEASIS_SIZE, Long.toString(performanceModel.getSize()));
			MDC.put(MDC_WEASIS_TIME, Long.toString(performanceModel.getTime()));
			MDC.put(MDC_WEASIS_RATE, performanceModel.getRate());
			MDC.put(MDC_WEASIS_ERRORS, Integer.toString(performanceModel.getErrors()));
		}
		finally {
			MDCUtil.endWSLogging(tStart, true, null, null);
		}
	}

}
