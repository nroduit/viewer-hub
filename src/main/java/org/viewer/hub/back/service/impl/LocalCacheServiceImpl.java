package org.viewer.hub.back.service.impl;

import org.apache.commons.io.FileUtils;
import org.dcm4che3.util.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.service.LocalCacheService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Service
public class LocalCacheServiceImpl implements LocalCacheService {

    @Value("${viewer-hub.tmp.storage}")
    private String storageDir;

    public Path generateTmpDir() {
        Path result;
        Date now = new Date();
        for (; ; ) {
            try {
                result = Files.createDirectory(Paths.get(storageDir).resolve(DateUtils.formatDT(null, now)));
                break;
            } catch (IOException e) {
                now = new Date(now.getTime() + 1);
            }
        }
        return result;
    }

    public void removeTmpDir(Path tmpDir) throws IOException {
        if (tmpDir == null) {
            return;
        }
        File dir = tmpDir.toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
}
