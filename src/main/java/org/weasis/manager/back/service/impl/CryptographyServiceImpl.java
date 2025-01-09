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

package org.weasis.manager.back.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.weasis.manager.back.model.WeasisIHESearchCriteria;
import org.weasis.manager.back.model.WeasisSearchCriteria;
import org.weasis.manager.back.service.CryptographyService;

import java.util.stream.Collectors;

@Service
@Slf4j
public class CryptographyServiceImpl implements CryptographyService {

	private final boolean enabled;

	private final String password;

	private final String salt;

	/**
	 * Constructor.
	 * @param password password to encode/decode
	 * @param salt salt to encode/decode
	 */
	public CryptographyServiceImpl(@Value("${cryptography.enabled}") final Boolean enabled,
			@Value("${cryptography.password}") final String password,
			@Value("${cryptography.salt}") final String salt) {
		this.enabled = enabled != null && enabled;
		this.password = password;
		this.salt = salt;
	}

	@Override
	public String encode(String toEncode) {
		String encoded = null;
		if (StringUtils.isNotBlank(toEncode) && StringUtils.isNotBlank(this.password)
				&& StringUtils.isNotBlank(this.salt)) {
			TextEncryptor encryptor = Encryptors.text(this.password, this.salt);
			encoded = encryptor.encrypt(toEncode);
		}
		return encoded;
	}

	@Override
	public String decode(String toDecode) {
		String decoded = toDecode;
		if (StringUtils.isNotBlank(toDecode) && StringUtils.isNotBlank(this.password)
				&& StringUtils.isNotBlank(this.salt)) {
			try {
				TextEncryptor encryptor = Encryptors.text(this.password, this.salt);
				decoded = encryptor.decrypt(toDecode);
			}
			catch (Exception e) {
				LOG.info("Error when decrypting %s:%s".formatted(toDecode, e));
			}

		}
		return decoded;
	}

	@Override
	public void encode(@Valid WeasisSearchCriteria weasisSearchCriteria) {
		if (this.enabled) {
			// Patient Ids
			weasisSearchCriteria.setPatientID(weasisSearchCriteria.getPatientID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Accession numbers
			weasisSearchCriteria.setAccessionNumber(weasisSearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Study UIDs
			weasisSearchCriteria.setStudyUID(weasisSearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Series UIDs
			weasisSearchCriteria.setSeriesUID(weasisSearchCriteria.getSeriesUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Objects UIDs
			weasisSearchCriteria.setObjectUID(weasisSearchCriteria.getObjectUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
		}
	}

	@Override
	public void decode(@Valid WeasisSearchCriteria weasisSearchCriteria) {
		if (this.enabled) {
			// Patient Ids
			weasisSearchCriteria.setPatientID(weasisSearchCriteria.getPatientID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Accession numbers
			weasisSearchCriteria.setAccessionNumber(weasisSearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Study UIDs
			weasisSearchCriteria.setStudyUID(weasisSearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Series UIDs
			weasisSearchCriteria.setSeriesUID(weasisSearchCriteria.getSeriesUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Objects UIDs
			weasisSearchCriteria.setObjectUID(weasisSearchCriteria.getObjectUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
		}
	}

	@Override
	public void encode(@Valid WeasisIHESearchCriteria weasisIHESearchCriteria) {
		if (this.enabled) {
			// Patient Id
			if (StringUtils.isNotBlank(weasisIHESearchCriteria.getPatientID())) {
				weasisIHESearchCriteria.setPatientID(this.encode(weasisIHESearchCriteria.getPatientID()));
			}
			// Accession numbers
			weasisIHESearchCriteria.setAccessionNumber(weasisIHESearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Study UIDs
			weasisIHESearchCriteria.setStudyUID(weasisIHESearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
		}
	}

	@Override
	public void decode(@Valid WeasisIHESearchCriteria weasisIHESearchCriteria) {
		if (this.enabled) {
			// Patient Id
			if (StringUtils.isNotBlank(weasisIHESearchCriteria.getPatientID())) {
				weasisIHESearchCriteria.setPatientID(this.decode(weasisIHESearchCriteria.getPatientID()));
			}
			// Accession numbers
			weasisIHESearchCriteria.setAccessionNumber(weasisIHESearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Study UIDs
			weasisIHESearchCriteria.setStudyUID(weasisIHESearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
		}
	}

}
