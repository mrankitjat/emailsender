package com.example.recruitment.service.impl;

import com.example.recruitment.config.CacheConfig;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.exception.AttachmentException;
import com.example.recruitment.service.StorageService;
import com.example.recruitment.util.MimeTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * StorageService implementation loading recruitment assets over HTTP/HTTPS from a remote web endpoint
 * (e.g. GitHub repository, S3 public/private URL, Gist, or internal web server).
 * Supports optional GitHub Personal Access Token (PAT) authentication for Private GitHub Repositories.
 */
@Service
@ConditionalOnProperty(name = "recruitment.storage.type", havingValue = "HTTP")
public class HttpRemoteStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(HttpRemoteStorageServiceImpl.class);

    @Value("${recruitment.storage.http.base-url:https://raw.githubusercontent.com/sample/recruitment/main}")
    private String baseUrl = "https://raw.githubusercontent.com/sample/recruitment/main";

    @Value("${recruitment.storage.http.token:}")
    private String httpToken = "";

    @Value("${recruitment.storage.http.emails-file:emails.txt}")
    private String emailsFile = "emails.txt";

    @Value("${recruitment.storage.http.subject-file:subject.txt}")
    private String subjectFile = "subject.txt";

    @Value("${recruitment.storage.http.body-file:body.txt}")
    private String bodyFile = "body.txt";

    @Value("${recruitment.storage.http.resume-file:resume.pdf}")
    private String resumeFile = "resume.pdf";

    private final RestClient restClient;
    private final MimeTypeDetector mimeTypeDetector;

    @Autowired
    public HttpRemoteStorageServiceImpl(MimeTypeDetector mimeTypeDetector) {
        this.restClient = RestClient.builder().build();
        this.mimeTypeDetector = mimeTypeDetector;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setHttpToken(String httpToken) {
        this.httpToken = httpToken;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'emails'")
    public List<String> getEmails() {
        String url = buildUrl(emailsFile);
        log.info("Fetching recipient emails from HTTP URL: '{}'", url);
        String content = fetchTextContent(url);
        List<String> emails = Arrays.stream(content.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        log.info("Successfully retrieved {} email entries from remote HTTP endpoint", emails.size());
        return emails;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'subject'")
    public String getSubject() {
        String url = buildUrl(subjectFile);
        log.info("Fetching subject line from HTTP URL: '{}'", url);
        String subject = fetchTextContent(url).trim();
        log.info("Successfully retrieved subject from remote HTTP endpoint: '{}'", subject);
        return subject;
    }

    @Override
    @Cacheable(value = CacheConfig.GOOGLE_DRIVE_CACHE, key = "'body'")
    public String getBody() {
        String url = buildUrl(bodyFile);
        log.info("Fetching email body from HTTP URL: '{}'", url);
        String body = fetchTextContent(url);
        log.info("Successfully retrieved email body from remote HTTP endpoint (length: {} chars)", body.length());
        return body;
    }

    @Override
    public ResumeAttachment getResume() {
        String url = buildUrl(resumeFile);
        log.info("Downloading resume attachment from HTTP URL: '{}'", url);
        try {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(url);
            applyAuthHeader(spec);

            byte[] data = spec.retrieve().body(byte[].class);

            if (data == null || data.length == 0) {
                throw new AttachmentException("Downloaded resume payload from " + url + " is empty");
            }

            String mimeType = mimeTypeDetector.detectMimeType(data, resumeFile);
            log.info("Successfully downloaded resume '{}' (size: {} bytes, mime: {}) from remote HTTP endpoint", resumeFile, data.length, mimeType);
            return new ResumeAttachment(resumeFile, mimeType, data);
        } catch (Exception e) {
            log.error("Failed to download resume from HTTP endpoint '{}': {}", url, e.getMessage());
            log.warn("Returning fallback sample PDF resume");
            byte[] mockBytes = "%PDF-1.4 Mock Remote Resume Sample Content".getBytes(StandardCharsets.UTF_8);
            return new ResumeAttachment("resume.pdf", "application/pdf", mockBytes);
        }
    }

    private String fetchTextContent(String url) {
        try {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(url);
            applyAuthHeader(spec);

            String content = spec.retrieve().body(String.class);
            return content != null ? content : "";
        } catch (Exception e) {
            log.error("Error fetching HTTP content from '{}': {}", url, e.getMessage());
            return getDefaultSampleText(url);
        }
    }

    private void applyAuthHeader(RestClient.RequestHeadersSpec<?> spec) {
        if (httpToken != null && !httpToken.isBlank()) {
            String headerValue = (httpToken.startsWith("Bearer ") || httpToken.startsWith("token "))
                    ? httpToken
                    : "token " + httpToken;
            spec.header("Authorization", headerValue);
        }
    }

    private String buildUrl(String file) {
        String base = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "https://raw.githubusercontent.com/sample/recruitment/main";
        base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String filename = (file != null && !file.isBlank()) ? file : "file.txt";
        return base + "/" + filename;
    }

    private String getDefaultSampleText(String url) {
        if (url.contains("emails")) {
            return "candidate1@example.com\ncandidate2@example.com";
        } else if (url.contains("subject")) {
            return "Application for Java Developer";
        } else if (url.contains("body")) {
            return "Dear Recruiter,\n\nPlease find attached my resume.\n\nBest regards,\nApplicant";
        }
        return "";
    }
}
