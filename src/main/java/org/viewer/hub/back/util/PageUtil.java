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
