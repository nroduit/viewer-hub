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

package org.viewer.hub.back.service;

import com.vaadin.flow.data.provider.QuerySortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.front.views.bundle.repository.component.WeasisAssetFilter;

import java.io.InputStream;
import java.util.List;

/**
 * Service managing the Weasis Nexus Repository
 */
public interface WeasisRepositoryService {

	/**
	 * Retrieve the Weasis assets from the Nexus repository corresponding to the package
	 * to deploy
	 * @param filter Filter to evaluate
	 * @param sortOrders Use to sort the result found
	 * @param pageable Pageable to evaluate
	 * @return List of assets found
	 */
	Page<WeasisAssetModel> retrieveWeasisAssets(WeasisAssetFilter filter, List<QuerySortOrder> sortOrders,
			Pageable pageable);

	/**
	 * Count Weasis Assets depending on the filter
	 * @param filter Filter applied
	 * @return number of Weasis assets found depending on filter
	 */
	int countWeasisAssets(WeasisAssetFilter filter);

	/**
	 * Download from Weasis Nexus repository the asset in parameter
	 * @param weasisAssetModel Asset to retrieve
	 * @return InputStream of the asset
	 */
	InputStream downloadWeasisAsset(WeasisAssetModel weasisAssetModel);

	/**
	 * Remove all the package versions corresponding to the asset in parameter
	 * @param weasisAssetModel Asset to remove
	 */
	void removeResourcePackageVersion(WeasisAssetModel weasisAssetModel);

}
