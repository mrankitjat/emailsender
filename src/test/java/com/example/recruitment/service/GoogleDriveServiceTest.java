package com.example.recruitment.service;

import com.example.recruitment.config.GoogleDriveConfig;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.service.impl.GoogleDriveServiceImpl;
import com.example.recruitment.util.MimeTypeDetector;
import com.google.api.services.drive.Drive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleDriveServiceTest {

    @Mock
    private Drive driveClient;

    @Mock
    private GoogleDriveConfig googleDriveConfig;

    private MimeTypeDetector mimeTypeDetector;
    private ResourceLoader resourceLoader;
    private GoogleDriveService googleDriveService;

    @BeforeEach
    void setUp() {
        mimeTypeDetector = new MimeTypeDetector();
        resourceLoader = new DefaultResourceLoader();
        lenient().when(googleDriveConfig.getFolderId()).thenReturn("mock-folder-id");

        googleDriveService = new GoogleDriveServiceImpl(
                driveClient,
                googleDriveConfig,
                mimeTypeDetector,
                resourceLoader
        );
    }

    @Test
    @DisplayName("Should fetch emails from classpath sample when drive call falls back")
    void testGetEmails_Fallback() {
        List<String> emails = googleDriveService.getEmails();
        assertNotNull(emails);
        assertFalse(emails.isEmpty());
        assertTrue(emails.contains("abc@google.com"));
    }

    @Test
    @DisplayName("Should fetch subject from classpath sample when drive call falls back")
    void testGetSubject_Fallback() {
        String subject = googleDriveService.getSubject();
        assertNotNull(subject);
        assertFalse(subject.isBlank());
    }

    @Test
    @DisplayName("Should fetch body from classpath sample when drive call falls back")
    void testGetBody_Fallback() {
        String body = googleDriveService.getBody();
        assertNotNull(body);
        assertFalse(body.isBlank());
    }

    @Test
    @DisplayName("Should download resume attachment successfully")
    void testGetResume_Fallback() {
        ResumeAttachment resume = googleDriveService.getResume();
        assertNotNull(resume);
        assertNotNull(resume.filename());
        assertNotNull(resume.contentType());
        assertNotNull(resume.data());
        assertTrue(resume.data().length > 0);
    }
}
