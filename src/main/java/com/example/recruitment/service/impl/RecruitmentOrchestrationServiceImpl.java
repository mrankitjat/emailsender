package com.example.recruitment.service.impl;

import com.example.recruitment.dto.EmailRequest;
import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.service.EmailService;
import com.example.recruitment.service.EmailValidationService;
import com.example.recruitment.service.RecruitmentOrchestrationService;
import com.example.recruitment.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Enterprise implementation of RecruitmentOrchestrationService resolving request parameters vs. StorageService assets.
 */
@Service
public class RecruitmentOrchestrationServiceImpl implements RecruitmentOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(RecruitmentOrchestrationServiceImpl.class);

    private final StorageService storageService;
    private final EmailService emailService;
    private final EmailValidationService emailValidationService;

    public RecruitmentOrchestrationServiceImpl(StorageService storageService,
                                               EmailService emailService,
                                               EmailValidationService emailValidationService) {
        this.storageService = storageService;
        this.emailService = emailService;
        this.emailValidationService = emailValidationService;
    }

    @Override
    public EmailResponse processAndSendEmails(EmailRequest request) {
        log.info("Received recruitment email request: {}", request);

        // 1. Priority Resolution for Target Recipients ('to')
        List<String> rawRecipients;
        if (request != null && request.hasTo()) {
            log.info("Priority 1: Using target email recipient list provided in HTTP Request payload ({})", request.to());
            rawRecipients = request.to();
        } else {
            log.info("Priority 2: Request recipient list is null or empty. Fetching emails from storage (emails.txt)...");
            rawRecipients = storageService.getEmails();
        }

        // 2. Validate and filter email address list
        List<String> validRecipients = emailValidationService.validateAndFilter(rawRecipients);

        if (validRecipients.isEmpty()) {
            log.warn("No valid email recipients remain after validation. Aborting transmission.");
            return new EmailResponse(0, 0, 0, List.of());
        }

        // 3. Priority Resolution for Subject
        String targetSubject;
        if (request != null && request.hasSubject()) {
            log.info("Priority 1: Using email subject provided in HTTP Request payload: '{}'", request.subject());
            targetSubject = request.subject();
        } else {
            log.info("Priority 2: Request subject is null or blank. Fetching subject from storage (subject.txt)...");
            targetSubject = storageService.getSubject();
        }

        // 4. Priority Resolution for Body
        String targetBody;
        if (request != null && request.hasBody()) {
            log.info("Priority 1: Using email body provided in HTTP Request payload");
            targetBody = request.body();
        } else {
            log.info("Priority 2: Request body is null or blank. Fetching body from storage (body.txt)...");
            targetBody = storageService.getBody();
        }

        // 5. Download Resume Attachment dynamically
        log.info("Fetching resume attachment from storage...");
        ResumeAttachment attachment = storageService.getResume();

        // 6. Trigger bulk email transmission
        log.info("Orchestrating bulk delivery to {} valid recipients...", validRecipients.size());
        return emailService.sendBulk(validRecipients, targetSubject, targetBody, attachment);
    }
}
