package org.viewer.hub.back.model.property;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.enums.ConnectorAuthType;

@NotNull
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Validated
@Schema(description = "Model used to represent the authentication connector properties")
public class ConnectorAuthenticationProperty {

	@NotNull
	@Schema(description = "Type of authentication")
	private ConnectorAuthType type;

	@Schema(description = "Properties for oauth2 authentication")
	private ConnectorOauth2AuthProperty oauth2;

	@Schema(description = "Properties for basic authentication")
	private ConnectorBasicAuthProperty basic;

}