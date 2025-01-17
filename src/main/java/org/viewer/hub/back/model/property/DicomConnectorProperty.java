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

package org.viewer.hub.back.model.property;

import org.dcm4che3.net.QueryOption;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.TlsOptions;

import java.util.Objects;

public class DicomConnectorProperty {

	private String callingAet;

	private String aet;

	private String host;

	private Integer port;

	private DicomConnectorTlsProperty tls;

	private DicomConnectorKeyStoreProperty keyStore;

	private DicomConnectorTrustStoreProperty truststore;

	public DicomConnectorProperty() {
	}

	public DicomConnectorProperty(String callingAet, String aet, String host, Integer port,
			DicomConnectorTlsProperty tls, DicomConnectorKeyStoreProperty keyStore,
			DicomConnectorTrustStoreProperty truststore) {
		this.callingAet = callingAet;
		this.aet = aet;
		this.host = host;
		this.port = port;
		this.tls = tls;
		this.keyStore = keyStore;
		this.truststore = truststore;
	}

	public String getAet() {
		return this.aet;
	}

	public void setAet(String aet) {
		this.aet = aet;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public DicomConnectorTlsProperty getTls() {
		return this.tls;
	}

	public void setTls(DicomConnectorTlsProperty tls) {
		this.tls = tls;
	}

	public DicomConnectorKeyStoreProperty getKeyStore() {
		return this.keyStore;
	}

	public void setKeyStore(DicomConnectorKeyStoreProperty keyStore) {
		this.keyStore = keyStore;
	}

	public DicomConnectorTrustStoreProperty getTruststore() {
		return this.truststore;
	}

	public void setTruststore(DicomConnectorTrustStoreProperty truststore) {
		this.truststore = truststore;
	}

	public String getCallingAet() {
		return this.callingAet;
	}

	public void setCallingAet(String callingAet) {
		this.callingAet = callingAet;
	}

	public DicomNode retrieveDicomNodeFromProperties() {
		return new DicomNode(this.aet, this.host, this.port);

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		DicomConnectorProperty that = (DicomConnectorProperty) o;
		return Objects.equals(this.callingAet, that.callingAet) && Objects.equals(this.aet, that.aet)
				&& Objects.equals(this.host, that.host) && Objects.equals(this.port, that.port)
				&& Objects.equals(this.tls, that.tls) && Objects.equals(this.keyStore, that.keyStore)
				&& Objects.equals(this.truststore, that.truststore);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.callingAet, this.aet, this.host, this.port, this.tls, this.keyStore, this.truststore);
	}

	public AdvancedParams retrieveAdvancedParamsFromProperties(boolean useQueryRelational) {
		AdvancedParams params = new AdvancedParams();
		if (this.tls != null && this.tls.getMode()) {
			TlsOptions tlsOptions = new TlsOptions(this.tls.isNeedClientAuthentication(),
					this.keyStore != null ? this.keyStore.getUrl() : StringUtil.EMPTY_STRING,
					this.keyStore != null ? this.keyStore.getType() : StringUtil.EMPTY_STRING,
					this.keyStore != null ? this.keyStore.getPassword() : StringUtil.EMPTY_STRING,
					this.keyStore != null ? this.keyStore.getKeyStorePassword() : StringUtil.EMPTY_STRING,
					this.truststore != null ? this.truststore.getUrl() : StringUtil.EMPTY_STRING,
					this.truststore != null ? this.truststore.getType() : StringUtil.EMPTY_STRING,
					this.truststore != null ? this.truststore.getPassword() : StringUtil.EMPTY_STRING);
			// params = new AdvancedParams();
			params.setTlsOptions(tlsOptions);
		}

		if (useQueryRelational) {
			params.getQueryOptions().add(QueryOption.RELATIONAL);
		}

		return params;
	}

}
