package com.example.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Execution summary returned after processing email sending requests.
 */
@Schema(description = "Response summary containing metrics of sent and failed emails.")
public record EmailResponse(
        @Schema(description = "Total number of valid unique recipient email addresses targeted", example = "100")
        int totalRecipients,

        @Schema(description = "Number of emails successfully delivered", example = "98")
        int success,

        @Schema(description = "Number of emails that failed delivery", example = "2")
        int failed,

        @Schema(description = "List of recipient email addresses for which delivery failed", example = "[\"abc@test.com\", \"xyz@test.com\"]")
        List<String> failedEmails
) {
}
