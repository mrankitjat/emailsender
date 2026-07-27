package com.example.recruitment.service;

import java.util.List;

/**
 * Service contract for email address format validation and sanitation.
 */
public interface EmailValidationService {

    /**
     * Sanitizes raw email input: trims whitespace, removes duplicates, filters invalid email formats, and logs skipped addresses.
     *
     * @param rawEmails List of input email address strings
     * @return List of validated, unique email addresses
     */
    List<String> validateAndFilter(List<String> rawEmails);

    /**
     * Validates individual email address string format.
     *
     * @param email Email string
     * @return true if valid RFC 5322 format
     */
    boolean isValidEmail(String email);
}
