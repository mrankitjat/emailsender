package com.example.recruitment.exception;

/**
 * Thrown when an unrecoverable failure occurs while transmitting an email via SMTP.
 */
public class MailSendingException extends RuntimeException {
    public MailSendingException(String message) {
        super(message);
    }

    public MailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
