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

package org.weasis.manager.front.views.association;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;
import org.weasis.manager.back.model.AssociationModelFilter;

import java.util.List;

/**
 * Association data provider which will handle pagination in the backend
 *
 * @param <T> AssociationModel
 */
@Component
public class AssociationDataProvider<T> extends FilterablePageableDataProvider<T, AssociationModelFilter> {

	private static final long serialVersionUID = -9033227362486757397L;

	// Services
	private final transient AssociationLogic associationLogic;

	// Default sort order
	private final List<QuerySortOrder> defaultSortOrders;

	@Autowired
	public AssociationDataProvider(AssociationLogic associationLogic) {
		this.associationLogic = associationLogic;
		// Default sort order
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		builder.thenAsc("name");
		this.defaultSortOrders = builder.build();
	}

	@Override
	protected Page<T> fetchFromBackEnd(Query<T, AssociationModelFilter> query, Pageable pageable) {
		AssociationModelFilter filter = query.getFilter().orElse(new AssociationModelFilter());
		return (Page<T>) this.associationLogic.retrieveAssociationModels(filter, pageable);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		return this.defaultSortOrders;
	}

	@Override
	protected int sizeInBackEnd(Query<T, AssociationModelFilter> query) {
		AssociationModelFilter filter = query.getFilter().orElse(new AssociationModelFilter());
		return this.associationLogic.countAssociationModels(filter);
	}

}
