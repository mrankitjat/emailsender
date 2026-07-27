package com.example.recruitment.exception;

/**
 * Thrown when operations with the Google Drive API fail.
 */
public class GoogleDriveException extends RuntimeException {
    public GoogleDriveException(String message) {
        super(message);
    }

    public GoogleDriveException(String message, Throwable cause) {
        super(message, cause);
    }
}
