package com.example.recruitment.service;

import com.example.recruitment.dto.EmailRequest;
import com.example.recruitment.dto.EmailResponse;

/**
 * High-level orchestration service resolving request parameters against Google Drive fallbacks
 * and executing bulk email workflows.
 */
public interface RecruitmentOrchestrationService {

    /**
     * Processes optional recruitment email request.
     * Evaluates priority rules: HTTP Request payload takes precedence; missing or empty fields are dynamically retrieved from Google Drive.
     *
     * @param request Optional EmailRequest DTO
     * @return EmailResponse summarizing total, success count, failure count, and failed recipient email list
     */
    EmailResponse processAndSendEmails(EmailRequest request);
}
