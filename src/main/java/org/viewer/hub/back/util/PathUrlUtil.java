package org.viewer.hub.back.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.viewer.hub.back.model.property.ConnectorServerProperty;

public class PathUrlUtil {

    /**
     * Ensure that the path will use / separator even when testing on Windows: used for S3
     * paths
     * @param path Path to transform
     * @return Paths updated
     */
    public static String pathWithS3Separator(String path) {
        return path != null ? path.replace("\\", "/") : null;
    }

    /**
     * Build url from server property
     * @param connectorServerProperty ConnectorServerProperty to evaluate
     * @return Url built
     */
    public static String buildUrlFromServerProperty(ConnectorServerProperty connectorServerProperty) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromUriString(connectorServerProperty.getUrl());
        if (StringUtils.isNotBlank(connectorServerProperty.getPort())) {
            uriComponentsBuilder.port(connectorServerProperty.getPort());
        }
        if (StringUtils.isNotBlank(connectorServerProperty.getContext())) {
            uriComponentsBuilder.path(connectorServerProperty.getContext());
        }
        return uriComponentsBuilder.build().toUriString();
    }
}
