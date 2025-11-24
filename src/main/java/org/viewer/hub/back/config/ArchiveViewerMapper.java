package org.viewer.hub.back.config;

import org.viewer.hub.back.enums.Archive;
import org.viewer.hub.back.enums.Viewer;

public class ArchiveViewerMapper {

    public static boolean shouldOpenViewerInNewTab(String archive) {
        Archive targetArchive = Archive.fromString(archive);
        return switch (targetArchive) {
            case DCM4CHEE -> false;
            case ORTHANC -> true;
        };
    }

}
