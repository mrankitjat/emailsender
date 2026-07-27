package com.example.recruitment.service.impl;

import com.example.recruitment.service.EmailValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementation of EmailValidationService providing regex validation, duplicate removal, and blank string sanitization.
 */
@Service
public class EmailValidationServiceImpl implements EmailValidationService {

    private static final Logger log = LoggerFactory.getLogger(EmailValidationServiceImpl.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public List<String> validateAndFilter(List<String> rawEmails) {
        if (rawEmails == null || rawEmails.isEmpty()) {
            log.warn("Validation received null or empty email list");
            return List.of();
        }

        Set<String> validEmails = new LinkedHashSet<>();
        List<String> skippedEmails = new ArrayList<>();

        for (String raw : rawEmails) {
            if (raw == null || raw.isBlank()) {
                skippedEmails.add("<BLANK/NULL>");
                continue;
            }

            String trimmed = raw.trim();
            if (isValidEmail(trimmed)) {
                validEmails.add(trimmed);
            } else {
                skippedEmails.add(trimmed);
            }
        }

        if (!skippedEmails.isEmpty()) {
            log.warn("Skipped {} invalid/blank email addresses: {}", skippedEmails.size(), skippedEmails);
        }

        log.info("Successfully validated {} unique email addresses out of {} raw inputs", validEmails.size(), rawEmails.size());
        return new ArrayList<>(validEmails);
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
