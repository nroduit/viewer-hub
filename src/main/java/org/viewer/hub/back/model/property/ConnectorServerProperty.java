package org.viewer.hub.back.model.property;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@NotNull
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Validated
@Schema(description = "Server properties")
public class ConnectorServerProperty {

	@NotBlank
	@Schema(description = "Url")
	private String url;

	@Schema(description = "Port")
	private String port;

	@Schema(description = "Context path of the api")
	private String context;

}