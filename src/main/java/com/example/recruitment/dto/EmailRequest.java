package com.example.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Optional payload for initiating recruitment emails.
 * Any missing or null field is automatically populated from Google Drive.
 */
@Schema(description = "Request body containing email parameters. All fields are optional.")
public record EmailRequest(
        @Schema(description = "Target recipient email addresses. If omitted or empty, loaded from emails.txt in Google Drive.", example = "[\"abc@company.com\", \"xyz@company.com\"]")
        List<String> to,

        @Schema(description = "Email subject line. If omitted or blank, loaded from subject.txt in Google Drive.", example = "Application for Senior Java Engineer")
        String subject,

        @Schema(description = "Email body text. If omitted or blank, loaded from body.txt in Google Drive.", example = "Dear Hiring Manager...")
        String body
) {
    public boolean hasTo() {
        return to != null && !to.isEmpty();
    }

    public boolean hasSubject() {
        return subject != null && !subject.isBlank();
    }

    public boolean hasBody() {
        return body != null && !body.isBlank();
    }
}
