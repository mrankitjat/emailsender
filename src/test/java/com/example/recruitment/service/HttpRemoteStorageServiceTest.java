package com.example.recruitment.service;

import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.service.impl.HttpRemoteStorageServiceImpl;
import com.example.recruitment.util.MimeTypeDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpRemoteStorageServiceTest {

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        MimeTypeDetector mimeTypeDetector = new MimeTypeDetector();
        storageService = new HttpRemoteStorageServiceImpl(mimeTypeDetector);
    }

    @Test
    @DisplayName("Should return emails list fallback when remote call is unconfigured")
    void testGetEmails() {
        List<String> emails = storageService.getEmails();
        assertNotNull(emails);
        assertFalse(emails.isEmpty());
    }

    @Test
    @DisplayName("Should return subject fallback when remote call is unconfigured")
    void testGetSubject() {
        String subject = storageService.getSubject();
        assertNotNull(subject);
        assertFalse(subject.isBlank());
    }

    @Test
    @DisplayName("Should return body fallback when remote call is unconfigured")
    void testGetBody() {
        String body = storageService.getBody();
        assertNotNull(body);
        assertFalse(body.isBlank());
    }

    @Test
    @DisplayName("Should return resume attachment fallback when remote call is unconfigured")
    void testGetResume() {
        ResumeAttachment resume = storageService.getResume();
        assertNotNull(resume);
        assertNotNull(resume.filename());
        assertNotNull(resume.data());
    }
}
