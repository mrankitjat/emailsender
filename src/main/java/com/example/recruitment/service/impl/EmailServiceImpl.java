package com.example.recruitment.service.impl;

import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.exception.MailSendingException;
import com.example.recruitment.model.SendEmailTask;
import com.example.recruitment.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise implementation of EmailService leveraging JavaMailSender, MimeMessageHelper, Spring Retry,
 * and asynchronous task execution for high-throughput recruitment email delivery.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final Executor emailTaskExecutor;

    @Value("${spring.mail.username:noreply@company.com}")
    private String senderEmail = "noreply@company.com";

    @Value("${email.rate-limit.delay-ms:0}")
    private long rateLimitDelayMs;

    public EmailServiceImpl(JavaMailSender mailSender, Executor emailTaskExecutor) {
        this.mailSender = mailSender;
        this.emailTaskExecutor = emailTaskExecutor;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Override
    @Retryable(
            retryFor = { Exception.class },
            maxAttemptsExpression = "${email.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${email.retry.backoff-ms:1000}")
    )
    public void sendEmail(SendEmailTask task) {
        log.info("Attempting email transmission to recipient: '{}', Subject: '{}'", task.recipient(), task.subject());
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String fromAddress = (senderEmail != null && !senderEmail.isBlank()) ? senderEmail : "noreply@company.com";
            helper.setFrom(fromAddress);
            helper.setTo(task.recipient());
            helper.setSubject(task.subject());
            helper.setText(task.body(), false);

            if (task.attachment() != null && task.attachment().data() != null && task.attachment().data().length > 0) {
                helper.addAttachment(
                        task.attachment().filename(),
                        new ByteArrayResource(task.attachment().data()),
                        task.attachment().contentType()
                );
            }

            mailSender.send(mimeMessage);
            log.info("SUCCESS: Email delivered successfully to '{}'", task.recipient());
        } catch (Exception e) {
            log.error("FAILURE: Failed to deliver email to '{}'. Cause: {}", task.recipient(), e.getMessage());
            throw new MailSendingException("Failed email delivery to " + task.recipient() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public EmailResponse sendBulk(List<String> recipients, String subject, String body, ResumeAttachment attachment) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("Bulk send invoked with empty recipient list");
            return new EmailResponse(0, 0, 0, List.of());
        }

        int totalRecipients = recipients.size();
        log.info("Starting bulk email dispatch to {} recipients. Subject: '{}'", totalRecipients, subject);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> failedEmails = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> futures = recipients.stream()
                .map(recipient -> CompletableFuture.runAsync(() -> {
                    if (rateLimitDelayMs > 0) {
                        try {
                            Thread.sleep(rateLimitDelayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    SendEmailTask task = new SendEmailTask(recipient, subject, body, attachment);
                    try {
                        sendEmail(task);
                        successCount.incrementAndGet();
                    } catch (Exception ex) {
                        log.error("Bulk process failure for recipient '{}': {}", recipient, ex.getMessage());
                        failureCount.incrementAndGet();
                        failedEmails.add(recipient);
                    }
                }, emailTaskExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Bulk email distribution completed! Total: {}, Success: {}, Failed: {}",
                totalRecipients, successCount.get(), failureCount.get());

        if (!failedEmails.isEmpty()) {
            log.warn("Failed delivery recipient list: {}", failedEmails);
        }

        return new EmailResponse(totalRecipients, successCount.get(), failureCount.get(), new ArrayList<>(failedEmails));
    }
}
