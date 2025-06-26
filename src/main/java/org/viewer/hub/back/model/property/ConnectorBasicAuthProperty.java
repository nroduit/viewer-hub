package org.viewer.hub.back.model.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Schema(description = "Properties for basic authentication")
public class ConnectorBasicAuthProperty {

	@Schema(description = "Login")
	private String login;

	@Schema(description = "Password")
	private String password;

	@Schema(description = "Server")
	private ConnectorServerProperty server;

}