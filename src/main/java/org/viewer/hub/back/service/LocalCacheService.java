package org.viewer.hub.back.service;

import java.io.IOException;
import java.nio.file.Path;

public interface LocalCacheService {

    Path generateTmpDir();

    void removeTmpDir(Path tmpDir) throws IOException;
}
