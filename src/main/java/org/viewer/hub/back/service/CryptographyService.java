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

import jakarta.validation.Valid;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;

/**
 * Service dealing with cryptography
 */
public interface CryptographyService {

	/**
	 * Encode a string depending on the salt and password provided
	 * @param toEncode String to encode
	 * @return encoded String
	 */
	String encode(String toEncode);

	/**
	 * Decode a string depending on the salt and password provided
	 * @param toDecode String to decode
	 * @return decoded String
	 */
	String decode(String toDecode);

	/**
	 * Encode ArchiveSearchCriteria values
	 * @param archiveSearchCriteria values to encode
	 */
	void encode(@Valid ArchiveSearchCriteria archiveSearchCriteria);

	/**
	 * Decode ArchiveSearchCriteria values
	 * @param archiveSearchCriteria values to decode
	 */
	void decode(ArchiveSearchCriteria archiveSearchCriteria);

	/**
	 * Encode IHESearchCriteria values
	 * @param IHESearchCriteria values to encode
	 */
	void encode(@Valid IHESearchCriteria IHESearchCriteria);

	/**
	 * Decode IHESearchCriteria values
	 * @param IHESearchCriteria values to decode
	 */
	void decode(IHESearchCriteria IHESearchCriteria);

}
