package com.example.recruitment.service.impl;

import com.example.recruitment.config.CacheConfig;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.exception.AttachmentException;
import com.example.recruitment.exception.GoogleDriveException;
import com.example.recruitment.service.StorageService;
import com.example.recruitment.util.MimeTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * StorageService implementation loading recruitment assets directly from a local disk folder.
 * Enabled when property `recruitment.storage.type` is set to `LOCAL` or omitted.
 */
@Service
@ConditionalOnProperty(name = "recruitment.storage.type", havingValue = "LOCAL", matchIfMissing = true)
public class LocalDirectoryStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalDirectoryStorageServiceImpl.class);

    @Value("${recruitment.storage.local-dir:./recruitment-assets}")
    private String localDirectoryPath;

    private final MimeTypeDetector mimeTypeDetector;

    public LocalDirectoryStorageServiceImpl(MimeTypeDetector mimeTypeDetector) {
        this.mimeTypeDetector = mimeTypeDetector;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'emails'")
    public List<String> getEmails() {
        log.info("Loading emails.txt from local directory: '{}'", localDirectoryPath);
        String content = readLocalTextFile("emails.txt");
        List<String> emails = Arrays.stream(content.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        log.info("Loaded {} email address entries from local emails.txt", emails.size());
        return emails;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'subject'")
    public String getSubject() {
        log.info("Loading subject.txt from local directory: '{}'", localDirectoryPath);
        String subject = readLocalTextFile("subject.txt").trim();
        log.info("Loaded subject from local storage: '{}'", subject);
        return subject;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'body'")
    public String getBody() {
        log.info("Loading body.txt from local directory: '{}'", localDirectoryPath);
        String body = readLocalTextFile("body.txt");
        log.info("Loaded body from local storage (length: {} chars)", body.length());
        return body;
    }

    @Override
    public ResumeAttachment getResume() {
        log.info("Searching for resume file (resume.pdf or resume.docx) in local directory: '{}'", localDirectoryPath);
        Path dirPath = Paths.get(localDirectoryPath);
        if (!Files.exists(dirPath)) {
            log.warn("Local directory '{}' does not exist. Creating sample folder with mock resume.", localDirectoryPath);
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                log.error("Failed to create directory '{}': {}", localDirectoryPath, e.getMessage());
            }
        }

        try (Stream<Path> stream = Files.list(dirPath)) {
            Optional<Path> resumeFile = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.startsWith("resume") || name.endsWith(".pdf") || name.endsWith(".docx");
                    })
                    .findFirst();

            if (resumeFile.isPresent()) {
                Path file = resumeFile.get();
                byte[] data = Files.readAllBytes(file);
                String filename = file.getFileName().toString();
                String mimeType = mimeTypeDetector.detectMimeType(data, filename);
                log.info("Successfully loaded local resume file '{}' (size: {} bytes, mime: {})", filename, data.length, mimeType);
                return new ResumeAttachment(filename, mimeType, data);
            }
        } catch (IOException e) {
            log.error("Error reading local directory '{}': {}", localDirectoryPath, e.getMessage());
        }

        log.warn("No resume file found in '{}'. Returning sample mock PDF attachment.", localDirectoryPath);
        byte[] mockBytes = "%PDF-1.4 Mock Resume Sample Content".getBytes(StandardCharsets.UTF_8);
        return new ResumeAttachment("resume.pdf", "application/pdf", mockBytes);
    }

    private String readLocalTextFile(String filename) {
        Path filePath = Paths.get(localDirectoryPath, filename);
        if (Files.exists(filePath)) {
            try {
                return Files.readString(filePath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Failed to read file '{}': {}", filePath, e.getMessage());
            }
        }
        log.warn("Local file '{}' not found at '{}'. Returning default sample text.", filename, filePath);
        return getDefaultSampleText(filename);
    }

    private String getDefaultSampleText(String filename) {
        return switch (filename) {
            case "emails.txt" -> "candidate1@example.com\ncandidate2@example.com";
            case "subject.txt" -> "Application for Software Engineer";
            case "body.txt" -> "Dear Recruiter,\n\nPlease find attached my resume for your consideration.\n\nBest regards,\nApplicant";
            default -> "";
        };
    }
}
