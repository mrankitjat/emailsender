package com.example.recruitment.service.impl;

import com.example.recruitment.config.CacheConfig;
import com.example.recruitment.config.GoogleDriveConfig;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.exception.AttachmentException;
import com.example.recruitment.exception.GoogleDriveException;
import com.example.recruitment.service.GoogleDriveService;
import com.example.recruitment.util.MimeTypeDetector;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Production implementation of GoogleDriveService interacting with Google Drive API v3
 * with Caffeine cache annotations and classpath fallback capabilities for development resilience.
 */
@Service
@ConditionalOnProperty(name = "recruitment.storage.type", havingValue = "GOOGLE_DRIVE")
public class GoogleDriveServiceImpl implements GoogleDriveService {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveServiceImpl.class);

    private final Drive driveClient;
    private final GoogleDriveConfig googleDriveConfig;
    private final MimeTypeDetector mimeTypeDetector;
    private final ResourceLoader resourceLoader;

    public GoogleDriveServiceImpl(Drive driveClient,
                                  GoogleDriveConfig googleDriveConfig,
                                  MimeTypeDetector mimeTypeDetector,
                                  ResourceLoader resourceLoader) {
        this.driveClient = driveClient;
        this.googleDriveConfig = googleDriveConfig;
        this.mimeTypeDetector = mimeTypeDetector;
        this.resourceLoader = resourceLoader;
    }

    @Override
    //@Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'emails'")
    public List<String> getEmails() {
        log.info("Fetching emails.txt from Google Drive folder ID: {}", googleDriveConfig.getFolderId());
        String content = fetchTextFileContent("emails.txt");
        List<String> emails = Arrays.stream(content.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        log.info("Successfully loaded {} email address entries from emails.txt", emails.size());
        return emails;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'subject'")
    public String getSubject() {
        log.info("Fetching subject.txt from Google Drive folder ID: {}", googleDriveConfig.getFolderId());
        String subject = fetchTextFileContent("subject.txt").trim();
        log.info("Successfully loaded subject from Google Drive: '{}'", subject);
        return subject;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'body'")
    public String getBody() {
        log.info("Fetching body.txt from Google Drive folder ID: {}", googleDriveConfig.getFolderId());
        String body = fetchTextFileContent("body.txt");
        log.info("Successfully loaded body from Google Drive (length: {} chars)", body.length());
        return body;
    }

    @Override
    public ResumeAttachment getResume() {
        log.info("Downloading resume attachment dynamically from Google Drive folder ID: {}", googleDriveConfig.getFolderId());
        try {
            Optional<File> driveFileOpt = findDriveFile("resume");

            if (driveFileOpt.isPresent()) {
                File file = driveFileOpt.get();
                byte[] data = downloadDriveFileContent(file.getId());
                String mimeType = mimeTypeDetector.detectMimeType(data, file.getName());
                log.info("Successfully downloaded resume '{}' (size: {} bytes, mime: {}) from Google Drive", file.getName(), data.length, mimeType);
                return new ResumeAttachment(file.getName(), mimeType, data);
            }

            log.warn("Resume file not found in Google Drive folder. Attempting classpath fallback sample 'sample-drive-files/resume.pdf'");
            return loadClasspathFallbackResume();
        } catch (Exception e) {
            log.error("Failed to fetch resume attachment: {}", e.getMessage(), e);
            throw new AttachmentException("Unable to retrieve resume attachment from Google Drive or classpath fallback", e);
        }
    }

    private String fetchTextFileContent(String filename) {
        try {
            Optional<File> driveFileOpt = findDriveFileByExactName(filename);
            if (driveFileOpt.isPresent()) {
                byte[] data = downloadDriveFileContent(driveFileOpt.get().getId());
                return new String(data, StandardCharsets.UTF_8);
            }

            log.warn("File '{}' not found in Google Drive folder. Attempting classpath fallback 'classpath:sample-drive-files/{}'", filename, filename);
            return loadClasspathFallbackText("classpath:sample-drive-files/" + filename);
        } catch (Exception e) {
            log.error("Error fetching file '{}' from Google Drive: {}", filename, e.getMessage(), e);
            log.warn("Falling back to sample file resource for '{}'", filename);
            return loadClasspathFallbackText("classpath:sample-drive-files/" + filename);
        }
    }

    private Optional<File> findDriveFileByExactName(String filename) {
        try {
            String query = String.format("'%s' in parents and name = '%s' and trashed = false",
                    googleDriveConfig.getFolderId(), filename);
            FileList result = driveClient.files().list()
                    .setQ(query)
                    .setFields("files(id, name, mimeType)")
                    .execute();

            if (result.getFiles() != null && !result.getFiles().isEmpty()) {
                return Optional.of(result.getFiles().get(0));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Google Drive API list call failed for exact name '{}': {}", filename, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<File> findDriveFile(String prefix) {
        try {
            String query = String.format("'%s' in parents and name contains '%s' and trashed = false",
                    googleDriveConfig.getFolderId(), prefix);
            FileList result = driveClient.files().list()
                    .setQ(query)
                    .setFields("files(id, name, mimeType)")
                    .execute();

            if (result.getFiles() != null && !result.getFiles().isEmpty()) {
                return Optional.of(result.getFiles().get(0));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Google Drive API search failed for prefix '{}': {}", prefix, e.getMessage());
            return Optional.empty();
        }
    }

    private byte[] downloadDriveFileContent(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveClient.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toByteArray();
    }

    private String loadClasspathFallbackText(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            throw new GoogleDriveException("Classpath sample file missing at " + location);
        } catch (IOException e) {
            throw new GoogleDriveException("Failed to read sample file at " + location, e);
        }
    }

    private ResumeAttachment loadClasspathFallbackResume() {
        try {
            Resource resource = resourceLoader.getResource("classpath:sample-drive-files/resume.pdf");
            byte[] data;
            String filename = "resume.pdf";

            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    data = inputStream.readAllBytes();
                }
            } else {
                data = "%PDF-1.4 Mock Resume Sample Content".getBytes(StandardCharsets.UTF_8);
            }

            String mimeType = mimeTypeDetector.detectMimeType(data, filename);
            return new ResumeAttachment(filename, mimeType, data);
        } catch (IOException e) {
            throw new AttachmentException("Failed to construct fallback resume attachment", e);
        }
    }
}
