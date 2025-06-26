package org.viewer.hub.back.model.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Validated
@Schema(description = "Model used to represent the dicom web properties")
public class ConnectorDicomWebProperty {

	@Schema(description = "Authentication connector properties")
	private ConnectorAuthenticationProperty authentication;

}
