package com.example.recruitment.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Domain DTO representing a resume attachment file retrieved from storage.
 */
public record ResumeAttachment(
        String filename,
        String contentType,
        byte[] data
) {
    public ResumeAttachment {
        Objects.requireNonNull(filename, "Filename cannot be null");
        Objects.requireNonNull(contentType, "Content type cannot be null");
        Objects.requireNonNull(data, "Attachment data cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumeAttachment that = (ResumeAttachment) o;
        return Objects.equals(filename, that.filename) &&
                Objects.equals(contentType, that.contentType) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(filename, contentType);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "ResumeAttachment{" +
                "filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", dataLength=" + (data != null ? data.length : 0) +
                '}';
    }
}
