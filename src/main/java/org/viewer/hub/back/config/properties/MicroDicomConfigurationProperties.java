package org.viewer.hub.back.config.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.model.property.Command;
import org.viewer.hub.back.model.property.ConnectorServerProperty;

import java.util.Map;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "microdicom")
public class MicroDicomConfigurationProperties {

    @NotNull
    private Command command;

    @NotNull
    private Map<String, ConnectorServerProperty> archives;

}
