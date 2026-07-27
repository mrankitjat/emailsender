package com.example.recruitment.exception;

/**
 * Thrown when resume attachment retrieval, validation, or MIME type detection fails.
 */
public class AttachmentException extends RuntimeException {
    public AttachmentException(String message) {
        super(message);
    }

    public AttachmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
