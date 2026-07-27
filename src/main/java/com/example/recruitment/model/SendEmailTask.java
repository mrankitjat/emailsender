package com.example.recruitment.model;

import com.example.recruitment.dto.ResumeAttachment;

/**
 * Domain encapsulation of an individual email transmission job.
 */
public record SendEmailTask(
        String recipient,
        String subject,
        String body,
        ResumeAttachment attachment
) {
}
