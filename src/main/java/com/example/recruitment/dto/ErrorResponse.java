package com.example.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Standardized API Error Response representation following RFC 7807 principles.
 */
@Schema(description = "Standardized error response payload")
public record ErrorResponse(
        @Schema(description = "HTTP Status Code", example = "500")
        int status,

        @Schema(description = "HTTP Status Error Description", example = "Internal Server Error")
        String error,

        @Schema(description = "Detailed failure description", example = "Failed to fetch credentials for Google Drive API")
        String message,

        @Schema(description = "Timestamp when the error occurred")
        LocalDateTime timestamp,

        @Schema(description = "Request URI path", example = "/api/email/send")
        String path
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), path);
    }
}
