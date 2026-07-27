package com.example.recruitment.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.Collections;

/**
 * Spring Configuration constructing the Google Drive client using Service Account OAuth2 authentication.
 */
@Configuration
public class GoogleDriveConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveConfig.class);

    @Value("${google.drive.credentials-path:classpath:credentials.json}")
    private String credentialsPath;

    @Value("${google.drive.folder-id:sample-folder-id}")
    private String folderId;

    @Value("${google.drive.application-name:recruitment-email-service}")
    private String applicationName;

    private final ResourceLoader resourceLoader;

    public GoogleDriveConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getFolderId() {
        return folderId;
    }

    @Bean
    public Drive googleDriveClient() {
        try {
            log.info("Initializing Google Drive API client using credentials path: '{}'", credentialsPath);
            Resource resource = resourceLoader.getResource(credentialsPath);

            if (!resource.exists()) {
                log.warn("Google Drive credentials file '{}' does not exist. Creating fallback unauthenticated Drive instance.", credentialsPath);
                NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                return new Drive.Builder(httpTransport, GsonFactory.getDefaultInstance(), null)
                        .setApplicationName(applicationName)
                        .build();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped(Collections.singleton(DriveScopes.DRIVE_READONLY));

                NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                return new Drive.Builder(httpTransport, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName(applicationName)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to initialize Google Drive client: {}", e.getMessage(), e);
            throw new IllegalStateException("Unable to construct Google Drive API client", e);
        }
    }
}
