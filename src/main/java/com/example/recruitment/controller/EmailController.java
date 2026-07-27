package com.example.recruitment.controller;

import com.example.recruitment.dto.EmailRequest;
import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.dto.ErrorResponse;
import com.example.recruitment.service.RecruitmentOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing recruitment email endpoints.
 */
@RestController
@RequestMapping("/api/email")
@Tag(name = "Recruitment Email API", description = "Endpoints for initiating automated recruitment emails with Google Drive fallback")
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final RecruitmentOrchestrationService orchestrationService;

    public EmailController(RecruitmentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @Operation(
            summary = "Send Recruitment Emails",
            description = "Triggers email sending with attached resume. Accepts an optional JSON request body override. Any null/missing parameter ('to', 'subject', 'body') is dynamically retrieved from Google Drive."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emails processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal processing failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Google Drive API unavailable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmails(@RequestBody(required = false) EmailRequest request) {
        log.info("REST Request received: POST /api/email/send with payload: {}", request);
        EmailResponse response = orchestrationService.processAndSendEmails(request);
        return ResponseEntity.ok(response);
    }
}
