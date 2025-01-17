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
package org.viewer.hub.front.views.i18n;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;
import org.viewer.hub.front.views.i18n.component.I18nFilter;

import java.io.Serial;
import java.util.List;

@Component
@Getter
public class I18nDataProvider<T> extends FilterablePageableDataProvider<T, I18nFilter> {

	@Serial
	private static final long serialVersionUID = -2437747220418546572L;

	// Services
	private final I18nLogic i18nLogic;

	@Autowired
	public I18nDataProvider(I18nLogic i18nLogic) {
		super();
		this.i18nLogic = i18nLogic;
	}

	@Override
	protected Page<T> fetchFromBackEnd(Query<T, I18nFilter> query, Pageable pageable) {
		I18nFilter filter = query.getFilter().orElse(new I18nFilter());
		return (Page<T>) this.i18nLogic.retrieveI18nVersions(filter, pageable);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		builder.thenDesc("versionNumber").thenAsc("qualifier");
		return builder.build();
	}

	@Override
	protected int sizeInBackEnd(Query<T, I18nFilter> query) {
		I18nFilter filter = query.getFilter().orElse(new I18nFilter());
		return this.i18nLogic.countI18nVersions(filter);
	}

}
