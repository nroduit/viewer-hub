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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.weasis.manager.back.constant.MDCConstants;
import org.weasis.manager.back.constant.MDCMessages;

import java.time.Duration;
import java.time.Instant;

@Slf4j
public class MDCUtil {

	private static final String MDC_LOGTYPE_APP = "APP"; // Log of type Application

	private static final Marker FATAL = MarkerFactory.getMarker("FATAL");

	private static final int MAX_PARAMETERS = 10;

	private MDCUtil() {
	}

	// APPLICATION WS LOGGING
	public static void startWSLogging(String service, String clientIP, String callingComponent, String... parameters) {
		putLogTypeInMDC(MDC_LOGTYPE_APP);
		putServiceInMDC(service);
		putClientInMDC(clientIP);
		putComponentInMDC(callingComponent);
		putParametersInMDC(parameters);
	}

	public static void endWSLogging(Instant tStart, boolean isSuccessful, String errorMsg, Throwable e) {
		putDurationInMDC(tStart);
		log(isSuccessful, MDCMessages.MSG_WS_SUCCEEDED, MDCMessages.MSG_WS_FAILED + " (" + errorMsg + ")", e, false);
		MDC.clear();
	}

	private static void putLogTypeInMDC(String logType) {
		if (logType != null) {
			MDC.put(MDCConstants.MDC_LOGTP, logType);
		}
	}

	private static void putServiceInMDC(String service) {
		if (service != null) {
			MDC.put(MDCConstants.MDC_SERVICE, service);
		}
	}

	private static void putClientInMDC(String client) {
		if (client != null) {
			MDC.put(MDCConstants.MDC_CLIENT, client);
		}
	}

	private static void putComponentInMDC(String component) {
		if (component != null) {
			MDC.put(MDCConstants.MDC_COMPONENT, component);
		}
	}

	private static void putDurationInMDC(Instant tStart) {
		MDC.put(MDCConstants.MDC_DURATION, Long.toString(Duration.between(tStart, Instant.now()).toMillis()));
	}

	private static void putParametersInMDC(String... parameters) {
		int index = 0;
		for (String parameter : parameters) {
			if (index < MAX_PARAMETERS) {
				if (parameter != null) {
					MDC.put(MDCConstants.MDC_PARAMS[index], parameter);
				}
				index++;
			}
		}
	}

	private static void log(boolean isSuccessful, String successMessage, String errorMessage, Throwable e,
			boolean isFatal) {
		if (isSuccessful) {
			LOG.info(successMessage);
		}
		else {
			if (isFatal) {
				LOG.error(FATAL, errorMessage, e);
			}
			else {
				LOG.error(errorMessage, e);
			}
		}
	}

}
