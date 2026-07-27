package com.example.recruitment.service;

import com.example.recruitment.dto.EmailRequest;
import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.dto.ResumeAttachment;
import com.example.recruitment.service.impl.RecruitmentOrchestrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruitmentOrchestrationServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailValidationService emailValidationService;

    private RecruitmentOrchestrationService orchestrationService;
    private ResumeAttachment mockAttachment;

    @BeforeEach
    void setUp() {
        orchestrationService = new RecruitmentOrchestrationServiceImpl(
                storageService,
                emailService,
                emailValidationService
        );

        mockAttachment = new ResumeAttachment("resume.pdf", "application/pdf", "Content".getBytes(StandardCharsets.UTF_8));

        lenient().when(storageService.getEmails()).thenReturn(List.of("storage1@example.com", "storage2@example.com"));
        lenient().when(storageService.getSubject()).thenReturn("Storage Subject");
        lenient().when(storageService.getBody()).thenReturn("Storage Body");
        lenient().when(storageService.getResume()).thenReturn(mockAttachment);

        lenient().when(emailValidationService.validateAndFilter(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Priority 1: Should use full request payload when provided")
    void testProcessAndSendEmails_FullRequest() {
        EmailRequest request = new EmailRequest(
                List.of("req@example.com"),
                "Req Subject",
                "Req Body"
        );

        EmailResponse mockResponse = new EmailResponse(1, 1, 0, List.of());
        when(emailService.sendBulk(eq(List.of("req@example.com")), eq("Req Subject"), eq("Req Body"), eq(mockAttachment)))
                .thenReturn(mockResponse);

        EmailResponse response = orchestrationService.processAndSendEmails(request);

        assertNotNull(response);
        assertEquals(1, response.totalRecipients());
        verify(storageService, never()).getEmails();
        verify(storageService, never()).getSubject();
        verify(storageService, never()).getBody();
        verify(storageService, times(1)).getResume();
    }

    @Test
    @DisplayName("Priority 2: Should fallback missing fields to StorageService")
    void testProcessAndSendEmails_PartialRequest_OnlyTo() {
        EmailRequest request = new EmailRequest(
                List.of("req@example.com"),
                null, // Subject missing
                ""    // Body blank
        );

        EmailResponse mockResponse = new EmailResponse(1, 1, 0, List.of());
        when(emailService.sendBulk(eq(List.of("req@example.com")), eq("Storage Subject"), eq("Storage Body"), eq(mockAttachment)))
                .thenReturn(mockResponse);

        EmailResponse response = orchestrationService.processAndSendEmails(request);

        assertNotNull(response);
        verify(storageService, never()).getEmails();
        verify(storageService, times(1)).getSubject();
        verify(storageService, times(1)).getBody();
    }

    @Test
    @DisplayName("Priority 2: Should fallback everything to StorageService when request is null or empty")
    void testProcessAndSendEmails_NullRequest() {
        EmailResponse mockResponse = new EmailResponse(2, 2, 0, List.of());
        when(emailService.sendBulk(eq(List.of("storage1@example.com", "storage2@example.com")), eq("Storage Subject"), eq("Storage Body"), eq(mockAttachment)))
                .thenReturn(mockResponse);

        EmailResponse response = orchestrationService.processAndSendEmails(null);

        assertNotNull(response);
        assertEquals(2, response.totalRecipients());
        verify(storageService, times(1)).getEmails();
        verify(storageService, times(1)).getSubject();
        verify(storageService, times(1)).getBody();
        verify(storageService, times(1)).getResume();
    }
}
