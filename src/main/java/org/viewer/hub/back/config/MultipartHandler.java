package org.viewer.hub.back.config;

import lombok.Getter;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.util.StringUtils;
import org.viewer.hub.back.service.LocalCacheService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class MultipartHandler implements MultipartParser.Handler {

    @Getter
    private Path dicomTmpDir;

    @Getter
    private Path dicomFile;

    private final LocalCacheService localCacheService;

    public MultipartHandler(LocalCacheService localCacheService) {
        this.localCacheService = localCacheService;
        this.dicomTmpDir = localCacheService.generateTmpDir();
    }

    @Override
    public void bodyPart(int partNumber, MultipartInputStream in) throws IOException {
        Map<String, List<String>> partHeaders = in.readHeaderParams();
        if (dicomTmpDir != null) {
            dicomFile = dicomTmpDir.resolve(
                    String.format("%03d", partNumber) + suffix(partHeaders.get("Content-type")));
            Files.copy(in, dicomFile);
        } else {
            in.skipAll();
        }
    }

    private String suffix(List<String> contentTypes) {
        if (contentTypes.isEmpty()) return "";
        String[] split = StringUtils.split(contentTypes.get(0).toLowerCase(), ';');
        return split[0].endsWith("dicom") ? ".dcm"
                : split[0].endsWith("xml") ? ".xml"
                : split[0].endsWith("json") ? ".json"
                : ".dcm";
    }

    public void dispose() throws IOException {
        this.localCacheService.removeTmpDir(this.dicomTmpDir);
    }

}