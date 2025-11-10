package org.viewer.hub.back.config;

import org.viewer.hub.back.enums.Archive;
import org.viewer.hub.back.enums.Viewer;

import java.util.Map;

public class ArchiveViewerMapper {

    public static boolean shouldOpenViewerInNewTab(String archive, String viewer) {
        Archive targetArchive = Archive.fromString(archive);
        Viewer targetViewer = Viewer.fromString(viewer);
        return switch (targetArchive) {
            case Archive.DCM4CHEE -> openViewerInNewTabFromDcm4chee(targetViewer);
            case Archive.ORTHANC -> true;
        };
    }

    private static boolean openViewerInNewTabFromDcm4chee(Viewer viewer) {
        return false;
    }

}
