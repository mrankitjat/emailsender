package com.example.recruitment.service;

import com.example.recruitment.dto.ResumeAttachment;
import java.util.List;

/**
 * Strategy interface for loading recruitment assets (emails, subject, body, resume)
 * from various storage providers (Local Filesystem, Classpath, Google Drive, AWS S3, HTTP).
 */
public interface StorageService {

    /**
     * Retrieves recipient emails list.
     */
    List<String> getEmails();

    /**
     * Retrieves email subject.
     */
    String getSubject();

    /**
     * Retrieves email body content.
     */
    String getBody();

    /**
     * Retrieves resume attachment file.
     */
    ResumeAttachment getResume();
}
