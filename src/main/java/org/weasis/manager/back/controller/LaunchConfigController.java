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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.weasis.manager.back.constant.EndPoint;
import org.weasis.manager.back.constant.MDCConstants;
import org.weasis.manager.back.constant.ParamName;
import org.weasis.manager.back.model.PerformanceModel;
import org.weasis.manager.back.service.LaunchPreferenceService;
import org.weasis.manager.back.util.DateTimeUtil;
import org.weasis.manager.back.util.InetUtil;
import org.weasis.manager.back.util.MDCUtil;
import org.weasis.manager.back.util.MultiValueMapUtil;
import org.weasis.manager.back.util.PackageUtil;
import org.weasis.manager.back.util.PropertiesLoader;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Resource class for Weasis Launch configuration manifest
 */
@Controller
@RequestMapping(EndPoint.LAUNCH_CONFIG_PATH)
@Tag(name = "Weasis Config Preference", description = "API Endpoints for Weasis Config Preference")
@Validated
@Slf4j
public class LaunchConfigController {

	public static final String PARAM_LAUNCH_VERSION = "ver";

	static final String HOST_PARAM_BYPASS_VALUE = "unknown";

	static final String DEFAULT_PROFILE = "default";

	//////////////////////////////////////////////////////////////////////////////

	private static final UnaryOperator<String> hostWithoutPrefix = s -> s.replaceFirst("^(?i)host_", "");

	private static final Function<String, Optional<String>> optionalValidParam = str -> Optional.ofNullable(str)
		.map(String::trim)
		.filter(s -> !(s.isEmpty()))
		.map(String::toLowerCase);

	static final Pattern weasisVersionFromUserAgentPattern = Pattern.compile("(?i)weasis\\s*/\\s*(\\d+\\.\\d+\\.\\d+)",
			Pattern.UNICODE_CHARACTER_CLASS);

	// REGEX to find {version} from Weasis User-Agent ignoring case and surrounding
	// whitespace like :
	// "Weasis/{version} (Linux;..." OR " WEASIS / {version}(windows;..."

	//////////////////////////////////////////////////////////////////////////////

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

	// Services
	private final LaunchPreferenceService launchPreferenceService;

	/**
	 * Autowired constructor
	 * @param launchPreferenceService Launch Preference Service
	 */
	@Autowired
	public LaunchConfigController(final LaunchPreferenceService launchPreferenceService) {
		this.launchPreferenceService = launchPreferenceService;
	}

	@Operation(summary = "Retrieve launch preferences for the user/host in parameter and generate a config xml",
			description = "Retrieve launch preferences for the user/host in parameter and generate a config xml",
			tags = "Weasis Config Preference")
	/**
	 * Retrieve launch preferences for the user/host in parameter and generate a config
	 * file
	 * @param requestParams Request params
	 * @param request Http request
	 * @param model Model for freemarker
	 * @return the generated launch preference config
	 */
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE })
	public String retrieveLaunchConfigPreferences(@RequestParam MultiValueMap<String, String> requestParams,
			HttpServletRequest request, Model model) {
		LocalDateTime startTimeRetrieveLaunchConfig = LocalDateTime.now();

		LOG.debug("retrieveLaunchConfigPreferences");

		// HANDLE launchConfig.properties PARAMETERS
		MultiValueMap<String, String> launchPropertiesMap = PropertiesLoader.getNewLaunchPropertyMap();

		// HANDLE QUERY REQUEST PARAMETERS
		requestParams.forEach((key, valueList) -> valueList
			.forEach(value -> MultiValueMapUtil.multiValueMapAddFirst(launchPropertiesMap, key.trim(),
					removeEnglobingQuotes(Objects.toString(value, "").trim()))));
		// TODO handle properties valueList to handle only one property key in the
		// launchPropertiesMap
		// >> get inspired from WeasisConfig.handleRequestPropertyParameter(..)

		// --------------------------------------------------------------------------------------------------------------
		// OVERLOADS PREFERENCES FOR USER/HOST

		final String userParam = launchPropertiesMap.getFirst(ParamName.USER);
		final String hostParam = launchPropertiesMap.getFirst(ParamName.HOST);
		final String configParam = StringUtils.isNotBlank(launchPropertiesMap.getFirst(ParamName.CONFIG))
				? launchPropertiesMap.getFirst(ParamName.CONFIG) : launchPropertiesMap.getFirst(ParamName.EXT_CFG);
		final String versionParam = launchPropertiesMap.getFirst(PARAM_LAUNCH_VERSION);

		// GET_USER from parameters
		String user = optionalValidParam.apply(userParam).orElse(null);

		// GET_HOST from IP/DNS (X-FORWARDED FOR attributes)
		String host = InetUtil.getClientHost(request, optionalValidParam.apply(hostParam).map(hostWithoutPrefix),
				HOST_PARAM_BYPASS_VALUE);

		// GET_CONFIG from parameter (if not given DEFAULT is used)
		String config = optionalValidParam.apply(configParam).orElse(DEFAULT_PROFILE);

		// GET_VERSION from USER-AGENT or parameter
		String version = optionalValidParam.apply(versionParam).orElse(null);

		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null) {
			Matcher m = weasisVersionFromUserAgentPattern.matcher(userAgent);
			if (m.find())
				version = m.group(1);
		}

		// Build Launch Configuration
		MultiValueMap<String, String> launchProperties = this.launchPreferenceService
			.buildLaunchConfiguration(launchPropertiesMap, user, host, config, version);

		// Map to the freemarker model
		this.launchPreferenceService.freeMarkerModelMapping(model, launchProperties);

		MultiValueMap<String, String> finalLaunchPropertiesFilledWithDBValues = launchProperties;
		LOG.info(
				"[LAUNCH CONFIG PREFERENCES]\n" + launchProperties.keySet()
					.stream()
					.map(k -> k + "=" + finalLaunchPropertiesFilledWithDBValues.get(k))
					.collect(Collectors.joining(",\n")),
				kv("launch-config.build.time",
						DateTimeUtil.retrieveDurationFromDateTimeInMs(startTimeRetrieveLaunchConfig)),
				kv("weasis.package.version",
						finalLaunchPropertiesFilledWithDBValues.get(PackageUtil.PROPERTIES_PACKAGE_VERSION_NAME)),
				kv("weasis.package.config",
						finalLaunchPropertiesFilledWithDBValues.get(PackageUtil.PROPERTIES_LAUNCH_CONFIG_NAME)),
				kv("weasis.package.group",
						finalLaunchPropertiesFilledWithDBValues.get(PackageUtil.PROPERTIES_GROUP_NAME)));

		return "launchConfigTemplate";
	}

	private static String removeEnglobingQuotes(String value) {
		return value.replaceAll("(^\")|(\"$)", "");
	}

	/**
	 * Log performances of weasis in Kibana
	 * @param performanceModel Performance model
	 */
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
