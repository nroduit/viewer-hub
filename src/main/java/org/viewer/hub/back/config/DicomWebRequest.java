package org.viewer.hub.back.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomMetaData;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.viewer.hub.back.service.LocalCacheService;
import org.viewer.hub.back.util.DicomWebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class DicomWebRequest extends StandardMultipartHttpServletRequest {

    @Getter
    private Path dicomFile;

    private MultipartHandler handler;

    private final LocalCacheService localCacheService;

    public DicomWebRequest(HttpServletRequest request, LocalCacheService localCacheService) throws MultipartException {
        super(request, true);
        this.localCacheService = localCacheService;
        parseRequest(request);
    }

    private void parseRequest(HttpServletRequest request) {
        try {
            InputStream inputStream = request.getInputStream();
            String requestContentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
            this.handler = new MultipartHandler(localCacheService);
            this.dicomFile = DicomWebUtils.multipartToDicomFile(inputStream, requestContentType, handler);
        }
        catch (Throwable ex) {
            try {
                this.handler.dispose();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handleParseFailure(ex);
        }
    }

    public DicomMetaData getDicomMetaData() throws IOException {
        return DicomWebUtils.getMetadata(this.dicomFile);
    }

    public ResponseEntity<String> generateResponse(String redirectUrl) throws JSONException, IOException {
        Attributes metadata = getDicomMetaData().getDicomObject();
        this.handler.dispose();
        return DicomWebUtils.generateDicomWebResponse(metadata, redirectUrl);
    }
}
