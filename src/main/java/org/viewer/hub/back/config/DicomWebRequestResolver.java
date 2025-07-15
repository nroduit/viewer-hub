package org.viewer.hub.back.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.viewer.hub.back.service.LocalCacheService;

public class DicomWebRequestResolver extends StandardServletMultipartResolver {

    @Autowired
    private LocalCacheService localCacheService;

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        return new DicomWebRequest(request, localCacheService);
    }

}