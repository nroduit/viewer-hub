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

package org.viewer.hub.front.views.viewerhub.association;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;
import org.viewer.hub.back.model.AssociationModelFilter;

import java.util.List;

/**
 * Association data provider which will handle pagination in the backend
 *
 * @param <T> AssociationModel
 */
@Component
public class ViewerAssociationDataProvider<T> extends FilterablePageableDataProvider<T, AssociationModelFilter> {

	private static final long serialVersionUID = -9033227362486757397L;

	// Services
	private final transient ViewerAssociationLogic viewerAssociationLogic;

	// Default sort order
	private final List<QuerySortOrder> defaultSortOrders;

	@Autowired
	public ViewerAssociationDataProvider(ViewerAssociationLogic viewerAssociationLogic) {
		this.viewerAssociationLogic = viewerAssociationLogic;
		// Default sort order
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		builder.thenDesc("priority");
		this.defaultSortOrders = builder.build();
	}

	@Override
	protected Page<T> fetchFromBackEnd(Query<T, AssociationModelFilter> query, Pageable pageable) {
		return (Page<T>) this.viewerAssociationLogic.retrieveAssociationModels(pageable);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		return this.defaultSortOrders;
	}

	@Override
	protected int sizeInBackEnd(Query<T, AssociationModelFilter> query) {
		return this.viewerAssociationLogic.countAssociationModels();
	}

}
