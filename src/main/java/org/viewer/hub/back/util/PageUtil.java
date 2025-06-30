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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {

	/**
	 * Convert a list of objects to a Page object
	 * @param objectList Objects to transforé
	 * @param pageable Pageable
	 * @return Page<T>
	 * @param <T>
	 */
	public static <T> Page<T> convertToPage(List<T> objectList, Pageable pageable) {
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), objectList.size());
		List<T> subList = start >= end ? new ArrayList<>() : objectList.subList(start, end);
		return new PageImpl<>(subList, pageable, objectList.size());
	}

}
