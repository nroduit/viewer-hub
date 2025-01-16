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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Helper for date time formats
 */
public class DateTimeUtil {

	private DateTimeUtil() {
		// Private constructor to hide implicit one
	}

	/**
	 * Date -> LocalDate
	 * @param dateToTransform Date to transform
	 * @return LocalDate
	 */
	public static LocalDate toLocalDate(Date dateToTransform) {
		return dateToTransform != null ? dateToTransform.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
				: null;
	}

	/**
	 * Date -> LocalTime
	 * @param dateToTransform Date to transform
	 * @return LocalTime
	 */
	public static LocalTime toLocalTime(Date dateToTransform) {
		return dateToTransform != null
				? LocalDateTime.ofInstant(dateToTransform.toInstant(), ZoneId.systemDefault()).toLocalTime() : null;
	}

	/**
	 * LocalDate + LocalTime -> LocalDateTime
	 * @param localDate LocalDate to transform
	 * @param localTime LocalTime to transform
	 * @return LocalDateTime
	 */
	public static LocalDateTime toLocalDateTime(LocalDate localDate, LocalTime localTime) {
		return localDate != null && localTime != null ? localTime.atDate(localDate)
				: localDate != null ? localDate.atStartOfDay() : null;
	}

	/**
	 * Calculate the time between the date/time in parameter and current time
	 * @return time calculated in milliseconds
	 */
	public static long retrieveDurationFromDateTimeInMs(LocalDateTime localDateTime) {
		return ChronoUnit.MILLIS.between(localDateTime, LocalDateTime.now());
	}

}
