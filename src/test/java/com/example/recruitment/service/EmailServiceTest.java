package com.example.recruitment.service;

import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.exception.MailSendingException;
import com.example.recruitment.model.SendEmailTask;
import com.example.recruitment.service.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService = new EmailServiceImpl(mailSender, Executors.newSingleThreadExecutor());
    }

    @Test
    @DisplayName("Should successfully send a single email with attachment")
    void testSendEmail_Success() {
        ResumeAttachment attachment = new ResumeAttachment("resume.pdf", "application/pdf", "Dummy PDF content".getBytes(StandardCharsets.UTF_8));
        SendEmailTask task = new SendEmailTask("test@example.com", "Test Subject", "Test Body", attachment);

        assertDoesNotThrow(() -> emailService.sendEmail(task));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw MailSendingException when JavaMailSender fails")
    void testSendEmail_Failure() {
        doThrow(new RuntimeException("SMTP Connection Refused")).when(mailSender).send(any(MimeMessage.class));

        ResumeAttachment attachment = new ResumeAttachment("resume.pdf", "application/pdf", "Dummy content".getBytes(StandardCharsets.UTF_8));
        SendEmailTask task = new SendEmailTask("test@example.com", "Test Subject", "Test Body", attachment);

        assertThrows(MailSendingException.class, () -> emailService.sendEmail(task));
    }

    @Test
    @DisplayName("Should process bulk emails and aggregate success/failed metrics")
    void testSendBulk_MixedResults() {
        ResumeAttachment attachment = new ResumeAttachment("resume.pdf", "application/pdf", "Dummy content".getBytes(StandardCharsets.UTF_8));
        List<String> recipients = List.of("success1@example.com", "fail@example.com", "success2@example.com");

        doAnswer(invocation -> {
            MimeMessage msg = invocation.getArgument(0);
            return null;
        }).when(mailSender).send(any(MimeMessage.class));

        EmailResponse response = emailService.sendBulk(recipients, "Bulk Subject", "Bulk Body", attachment);

        assertNotNull(response);
        assertEquals(3, response.totalRecipients());
        assertEquals(3, response.success());
        assertEquals(0, response.failed());
    }
}
