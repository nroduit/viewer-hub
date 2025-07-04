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
import org.viewer.hub.back.model.ArchiveSearchCriteria;
import org.viewer.hub.back.model.IHESearchCriteria;

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
	 * Encode WeasisSearchCriteria values
	 * @param weasisSearchCriteria values to encode
	 */
	void encode(@Valid ArchiveSearchCriteria weasisSearchCriteria);

	/**
	 * Decode WeasisSearchCriteria values
	 * @param weasisSearchCriteria values to decode
	 */
	void decode(ArchiveSearchCriteria weasisSearchCriteria);

	/**
	 * Encode WeasisIHESearchCriteria values
	 * @param weasisIHESearchCriteria values to encode
	 */
	void encode(@Valid IHESearchCriteria weasisIHESearchCriteria);

	/**
	 * Decode WeasisIHESearchCriteria values
	 * @param weasisIHESearchCriteria values to decode
	 */
	void decode(IHESearchCriteria weasisIHESearchCriteria);

}
