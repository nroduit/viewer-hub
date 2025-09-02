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

package org.viewer.hub.back.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;
import org.viewer.hub.back.model.searchcriteria.SearchCriteria;
import org.viewer.hub.back.service.CryptographyService;

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
				LOG.debug("Decrypting %s not done:%s".formatted(toDecode, e));
			}
		}
		return decoded;
	}

	@Override
	public void encode(@Valid ArchiveSearchCriteria archiveSearchCriteria) {
		if (this.enabled) {
			// Patient Ids
			archiveSearchCriteria.setPatientID(archiveSearchCriteria.getPatientID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Accession numbers
			archiveSearchCriteria.setAccessionNumber(archiveSearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Study UIDs
			archiveSearchCriteria.setStudyUID(archiveSearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Series UIDs
			archiveSearchCriteria.setSeriesUID(archiveSearchCriteria.getSeriesUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Objects UIDs
			archiveSearchCriteria.setObjectUID(archiveSearchCriteria.getObjectUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
		}
	}

	@Override
	public void decode(@Valid SearchCriteria searchCriteria) {
		if (this.enabled) {
			if (searchCriteria instanceof ArchiveSearchCriteria) {
				this.decode((ArchiveSearchCriteria) searchCriteria);
			}
			else if (searchCriteria instanceof IHESearchCriteria) {
				this.decode((IHESearchCriteria) searchCriteria);
			}
		}
	}

	@Override
	public void decode(@Valid ArchiveSearchCriteria archiveSearchCriteria) {
		if (this.enabled) {
			// Patient Ids
			archiveSearchCriteria.setPatientID(archiveSearchCriteria.getPatientID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Accession numbers
			archiveSearchCriteria.setAccessionNumber(archiveSearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Study UIDs
			archiveSearchCriteria.setStudyUID(archiveSearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Series UIDs
			archiveSearchCriteria.setSeriesUID(archiveSearchCriteria.getSeriesUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Objects UIDs
			archiveSearchCriteria.setObjectUID(archiveSearchCriteria.getObjectUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
		}
	}

	@Override
	public void encode(@Valid IHESearchCriteria iheSearchCriteria) {
		if (this.enabled) {
			// Patient Id
			if (StringUtils.isNotBlank(iheSearchCriteria.getPatientID())) {
				iheSearchCriteria.setPatientID(this.encode(iheSearchCriteria.getPatientID()));
			}
			// Accession numbers
			iheSearchCriteria.setAccessionNumber(iheSearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
			// Study UIDs
			iheSearchCriteria.setStudyUID(iheSearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::encode)
				.collect(Collectors.toSet()));
		}
	}

	@Override
	public void decode(@Valid IHESearchCriteria iheSearchCriteria) {
		if (this.enabled) {
			// Patient Id
			if (StringUtils.isNotBlank(iheSearchCriteria.getPatientID())) {
				iheSearchCriteria.setPatientID(this.decode(iheSearchCriteria.getPatientID()));
			}
			// Accession numbers
			iheSearchCriteria.setAccessionNumber(iheSearchCriteria.getAccessionNumber()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
			// Study UIDs
			iheSearchCriteria.setStudyUID(iheSearchCriteria.getStudyUID()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(this::decode)
				.collect(Collectors.toSet()));
		}
	}

}
