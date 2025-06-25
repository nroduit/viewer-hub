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
package org.viewer.hub.front.views.bundle.repository;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;
import org.viewer.hub.front.views.bundle.repository.component.WeasisAssetFilter;

import java.util.List;

/**
 * Data provider for the grid containing the assets in the Weasis nexus repository
 *
 * @param <T>
 */
@Component
@Getter
public class WeasisAssetDataProvider<T> extends FilterablePageableDataProvider<T, WeasisAssetFilter> {

	// Services
	private final WeasisRepositoryLogic weasisRepositoryLogic;

	@Autowired
	public WeasisAssetDataProvider(WeasisRepositoryLogic weasisRepositoryLogic) {
		super();
		this.weasisRepositoryLogic = weasisRepositoryLogic;
	}

	@Override
	protected Page<T> fetchFromBackEnd(Query<T, WeasisAssetFilter> query, Pageable pageable) {
		return (Page<T>) this.weasisRepositoryLogic
			.retrieveWeasisAssets(query.getFilter().orElse(new WeasisAssetFilter()), query.getSortOrders(), pageable);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		return builder.build();
	}

	@Override
	protected int sizeInBackEnd(Query<T, WeasisAssetFilter> query) {
		WeasisAssetFilter filter = query.getFilter().orElse(new WeasisAssetFilter());
		return this.weasisRepositoryLogic.countWeasisAssets(filter);
	}

}
