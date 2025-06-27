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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dcm4che3.net.QueryOption;
import org.springframework.validation.annotation.Validated;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.TlsOptions;

@Getter
@Setter
@Validated
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DicomConnectorDimseProperty {

	@Schema(description = "Calling aet")
	private String callingAet;

	@Schema(description = "Aet")
	private String aet;

	@Schema(description = "Host")
	private String host;

	@Schema(description = "Port")
	private Integer port;

	@Schema(description = "Tls properties")
	private DicomConnectorTlsProperty tls;

	@Schema(description = "Keystore properties")
	private DicomConnectorKeyStoreProperty keyStore;

	@Schema(description = "Truststore properties")
	private DicomConnectorTrustStoreProperty truststore;

	public DicomNode retrieveDicomNodeFromProperties() {
		return new DicomNode(this.aet, this.host, this.port);
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
