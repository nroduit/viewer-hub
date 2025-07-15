package org.viewer.hub.back.util;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomImageReaderSpi;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.stream.DicomFileInputStream;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.util.StringUtils;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.viewer.hub.back.config.MultipartHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.catalina.filters.CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN;

public class DicomWebUtils {

    public static final String APPLICATION_DICOM_JSON = "application/dicom+json";

    public static ResponseEntity<String> generateDicomWebResponse(Attributes attributes, String redirectUrl) throws JSONException, IOException {
        String jsonResponse = generateJsonResponse(attributes);
        return generateDicomWebResponse(jsonResponse, redirectUrl);
    }

    public static ResponseEntity<String> generateDicomWebResponse(String body, String redirectUrl) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        if (redirectUrl != null) {
            responseHeaders.add(HttpHeaders.LOCATION, redirectUrl);
        }

        if (body == null) {
            return new ResponseEntity<>(body, responseHeaders, HttpStatus.NO_CONTENT);
        }

        responseHeaders.set(HttpHeaders.CONTENT_TYPE, APPLICATION_DICOM_JSON);
        responseHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.length()));
        return new ResponseEntity<>(body, responseHeaders, HttpStatus.OK);
    }

    public static String generateJsonResponse(Path dcmFile) throws IOException {
        StringWriter out = new StringWriter();
        Map<String, ?> conf = new HashMap<String, Object>(2);
        JsonGenerator jsonGen = Json.createGeneratorFactory(conf).createGenerator(out);
        JSONWriter jsonWriter = new JSONWriter(jsonGen);
        DicomInputStream dis = new DicomInputStream(dcmFile.toFile());
        dis.setDicomInputHandler(jsonWriter);
        dis.readDataset(-1, -1);
        jsonGen.flush();
        List<String> result = new ArrayList<>();
        result.add(out.toString());
        return result.toString();
    }

    public static String generateJsonResponse(Attributes attributes) throws IOException, JSONException {
        StringWriter os = new StringWriter();
        JsonGenerator generator = Json.createGenerator(os);
        JSONWriter jsonWriter = new JSONWriter(generator);
        jsonWriter.write(attributes);
        generator.close();

        List<String> result = new ArrayList<>();
        result.add(os.toString());
        return result.toString();
    }

    public static DicomMetaData getMetadata(Path dicomFile) throws IOException {
        DicomImageReaderSpi dicomImageReaderSpi = new DicomImageReaderSpi();
        DicomImageReader reader = new DicomImageReader(dicomImageReaderSpi);
        reader.setInput(new DicomFileInputStream(dicomFile));
        return reader.getStreamMetadata();
    }

    public static Path multipartToDicomFile(InputStream inputStream, String contentType, MultipartHandler handler) throws IOException {
        String boundary = getBoundary(contentType);
        try {
            try {
                new MultipartParser(boundary).parse(inputStream, handler);
                return handler.getDicomFile();
            } finally {
                inputStream.close();
            }
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getBoundary(String contentType) {
        String[] params = StringUtils.split(contentType, ';');
        for (int i = 1; i < params.length; i++) {
            String param = params[i].trim();
            if (param.length() > 12 && param.startsWith("boundary=")) {
                return param.charAt(9) == '"' && param.charAt(param.length() - 1) == '"'
                        ? param.substring(10, param.length() - 1)
                        : param.substring(9);
            }
        }
        return null;
    }
}
