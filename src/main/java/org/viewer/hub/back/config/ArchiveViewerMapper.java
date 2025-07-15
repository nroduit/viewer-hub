package org.viewer.hub.back.config;

import java.util.Map;

public class ArchiveViewerMapper {

    private static final Map<String, String> mapper = Map.of(
        "dcm4chee", "weasis",
        "orthanc", "weasis"
    );

    public static String getViewer(String archive) {
        return mapper.get(archive);
    }

}
