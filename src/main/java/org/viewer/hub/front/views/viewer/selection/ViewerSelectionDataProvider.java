/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.front.views.viewer.selection;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;
import org.viewer.hub.back.model.ViewerSelectionFilter;

import java.io.Serial;
import java.util.List;

/**
 * Data provider which will handle pagination in the backend
 *
 * @param <T> Type of data provided
 */
@Component
public class ViewerSelectionDataProvider<T> extends FilterablePageableDataProvider<T, ViewerSelectionFilter> {

	@Serial
	private static final long serialVersionUID = -9033227362486757397L;

	// Services
	private final transient ViewerSelectionLogic viewerSelectionLogic;

	// Default sort order
	private final List<QuerySortOrder> defaultSortOrders;

	@Autowired
	public ViewerSelectionDataProvider(ViewerSelectionLogic viewerSelectionLogic) {
		this.viewerSelectionLogic = viewerSelectionLogic;
		// Default sort order
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		builder.thenDesc("priority");
		this.defaultSortOrders = builder.build();
	}

	@Override
	protected Page<T> fetchFromBackEnd(Query<T, ViewerSelectionFilter> query, Pageable pageable) {
		return (Page<T>) this.viewerSelectionLogic.retrieveViewerSelection(pageable);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		return this.defaultSortOrders;
	}

	@Override
	protected int sizeInBackEnd(Query<T, ViewerSelectionFilter> query) {
		return this.viewerSelectionLogic.countViewerSelection();
	}

}
