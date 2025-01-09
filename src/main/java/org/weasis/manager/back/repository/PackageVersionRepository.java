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

package org.weasis.manager.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weasis.manager.back.entity.PackageVersionEntity;

/**
 * Repository for the entity PackageVersion.
 */
public interface PackageVersionRepository extends JpaRepository<PackageVersionEntity, Long> {

	/**
	 * Find by version number and qualifier
	 * @param version version
	 * @param qualifier qualifier
	 * @return packageVersionEntity found
	 */
	PackageVersionEntity findByVersionNumberAndQualifier(String version, String qualifier);

}