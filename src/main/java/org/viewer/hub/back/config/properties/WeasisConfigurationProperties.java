package org.viewer.hub.back.config.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.viewer.hub.back.model.property.Command;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "weasis")
public class WeasisConfigurationProperties {

    @NotNull
    private Command command;

}
