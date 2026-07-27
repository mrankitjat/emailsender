package com.example.recruitment.util;

import com.example.recruitment.exception.AttachmentException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility service using Apache Tika to automatically detect MIME types of resume files.
 */
@Component
public class MimeTypeDetector {

    private static final Logger log = LoggerFactory.getLogger(MimeTypeDetector.class);

    private final Tika tika;

    public MimeTypeDetector() {
        this.tika = new Tika();
    }

    /**
     * Detects MIME type from raw byte content and suggested filename.
     *
     * @param bytes    File raw byte array
     * @param filename Filename hint
     * @return Standard MIME content type string (e.g. application/pdf)
     */
    public String detectMimeType(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new AttachmentException("Cannot detect MIME type for empty file content");
        }

        try {
            String mimeType = tika.detect(bytes, filename);
            log.info("Detected MIME type for file '{}': {}", filename, mimeType);
            return mimeType;
        } catch (Exception e) {
            log.warn("Apache Tika detection failed for file '{}', falling back to filename extension matching. Error: {}", filename, e.getMessage());
            return fallbackMimeType(filename);
        }
    }

    private String fallbackMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerName.endsWith(".doc")) {
            return "application/msword";
        } else {
            return "application/octet-stream";
        }
    }
}
