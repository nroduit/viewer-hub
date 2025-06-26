package org.viewer.hub.back.model.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ConnectorOauth2AuthProperty {

	@Schema(description = "Id of the oidc configuration (defined in application-oidc.yml)")
	private String oidcId;

	@Schema(description = "Server properties")
	private ConnectorServerProperty server;

}
