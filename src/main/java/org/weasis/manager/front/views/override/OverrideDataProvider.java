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
package org.weasis.manager.front.views.override;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;
import org.weasis.manager.front.views.override.component.OverrideConfigFilter;

import java.io.Serial;
import java.util.List;

@Component
public class OverrideDataProvider<T> extends FilterablePageableDataProvider<T, OverrideConfigFilter> {

	@Serial
	private static final long serialVersionUID = -8992746522768895949L;

	// Services
	private final OverrideLogic overrideLogic;

	@Autowired
	public OverrideDataProvider(OverrideLogic overrideLogic) {
		super();
		this.overrideLogic = overrideLogic;
	}

	@Override
	protected Page<T> fetchFromBackEnd(Query<T, OverrideConfigFilter> query, Pageable pageable) {
		OverrideConfigFilter filter = query.getFilter().orElse(new OverrideConfigFilter());
		return (Page<T>) this.overrideLogic.retrieveOverrideConfigs(filter, pageable);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		builder.thenDesc("packageVersionVersionNumber")
			.thenAsc("packageVersionQualifier")
			.thenAsc("launchConfigName")
			.thenAsc("targetName");
		return builder.build();
	}

	@Override
	protected int sizeInBackEnd(Query<T, OverrideConfigFilter> query) {
		OverrideConfigFilter filter = query.getFilter().orElse(new OverrideConfigFilter());
		return this.overrideLogic.countOverrideConfigs(filter);
	}

	public OverrideLogic getOverrideLogic() {
		return this.overrideLogic;
	}

}
