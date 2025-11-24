package org.viewer.hub.back.config;

import org.viewer.hub.back.enums.Archive;
import org.viewer.hub.back.enums.Viewer;

public class ArchiveViewerMapper {

    public static boolean shouldOpenViewerInNewTab(String archive, Viewer viewer) {
        Archive targetArchive = Archive.fromString(archive);
        return switch (targetArchive) {
            case Archive.DCM4CHEE -> false;
            case Archive.ORTHANC -> openViewerInNewTabFromOrthanc(viewer);
        };
    }

    private static boolean openViewerInNewTabFromOrthanc(Viewer viewer) {
        return switch (viewer) {
            case Viewer.OHIF -> true;
            default -> false;
        };
    }

}
