package com.example.recruitment.service;

import com.example.recruitment.service.impl.EmailValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidationServiceTest {

    private EmailValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new EmailValidationServiceImpl();
    }

    @Test
    @DisplayName("Should validate valid email addresses correctly")
    void testIsValidEmail_Valid() {
        assertTrue(validationService.isValidEmail("abc@google.com"));
        assertTrue(validationService.isValidEmail("xyz.test+1@company.co.uk"));
        assertTrue(validationService.isValidEmail("careers@amazon.com"));
    }

    @Test
    @DisplayName("Should reject invalid email addresses")
    void testIsValidEmail_Invalid() {
        assertFalse(validationService.isValidEmail("invalid-email"));
        assertFalse(validationService.isValidEmail("abc@com"));
        assertFalse(validationService.isValidEmail("@domain.com"));
        assertFalse(validationService.isValidEmail(null));
        assertFalse(validationService.isValidEmail("   "));
    }

    @Test
    @DisplayName("Should filter out invalid, blank, and duplicate emails")
    void testValidateAndFilter() {
        List<String> rawEmails = List.of(
                "abc@google.com",
                "   ",
                "xyz@microsoft.com",
                "abc@google.com", // Duplicate
                "invalid-address",
                "careers@amazon.com"
        );

        List<String> result = validationService.validateAndFilter(rawEmails);

        assertEquals(3, result.size());
        assertTrue(result.contains("abc@google.com"));
        assertTrue(result.contains("xyz@microsoft.com"));
        assertTrue(result.contains("careers@amazon.com"));
    }

    @Test
    @DisplayName("Should handle null or empty input gracefully")
    void testValidateAndFilter_EmptyOrNull() {
        assertTrue(validationService.validateAndFilter(null).isEmpty());
        assertTrue(validationService.validateAndFilter(List.of()).isEmpty());
    }
}
