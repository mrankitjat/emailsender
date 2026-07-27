package com.example.recruitment.service;

import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.model.SendEmailTask;

import java.util.List;

/**
 * Service interface governing single and bulk email operations via SMTP with Spring Mail.
 */
public interface EmailService {

    /**
     * Sends a single recruitment email with attachment. Includes Spring Retry mechanism for transient errors.
     *
     * @param task Domain object containing recipient, subject, body, and attachment
     */
    void sendEmail(SendEmailTask task);

    /**
     * Orchestrates bulk email dispatching to multiple recipients concurrently using thread pool execution.
     * Continues dispatching even if individual recipient emails fail, logging each outcome.
     *
     * @param recipients List of validated target email addresses
     * @param subject    Email subject line
     * @param body       Email message body content
     * @param attachment Resume attachment payload
     * @return EmailResponse summarizing total, success count, failure count, and failed recipient email list
     */
    EmailResponse sendBulk(List<String> recipients, String subject, String body, ResumeAttachment attachment);
}
