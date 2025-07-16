package org.viewer.hub.back.config;

import org.viewer.hub.back.enums.Archive;
import org.viewer.hub.back.enums.Viewer;

import java.util.Map;

public class ArchiveViewerMapper {

    private static final Map<Archive, Viewer> mapper = Map.of(
            Archive.DCM4CHEE, Viewer.OHIF,
            Archive.ORTHANC, Viewer.OHIF
    );

    public static String getViewer(String archive) {
        Archive targetArchive = Archive.fromString(archive);
        return mapper.get(targetArchive).toString();
    }

    public static boolean shouldOpenViewerInNewTab(String archive, String viewer) {
        Archive targetArchive = Archive.fromString(archive);
        Viewer targetViewer = Viewer.fromString(viewer);
        return switch (targetArchive) {
            case Archive.DCM4CHEE -> openViewerInNewTabFromDcl4chee(targetViewer);
            case Archive.ORTHANC -> true;
        };
    }

    private static boolean openViewerInNewTabFromDcl4chee(Viewer viewer) {
        return switch (viewer) {
            case OHIF -> true;
            case WEASIS -> false;
        };
    }

}
