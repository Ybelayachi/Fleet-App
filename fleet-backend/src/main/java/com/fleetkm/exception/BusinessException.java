package com.fleetkm.exception;

import org.springframework.http.HttpStatusCode;

/**
 * Custom exception for business logic violations.
 * Extends RuntimeException to be used as an unchecked exception.
 */
public final class BusinessException extends RuntimeException {

    /** HTTP status code for the exception response. */
    private final HttpStatusCode status;
    /** Reason/message describing the business rule violation. */
    private final String reason;
    /** Optional field name related to the violation. */
    private final String field;

    /**
     * Constructs BusinessException with status, reason, and field.
     *
     * @param httpStatus the HTTP status code (must not be null)
     * @param reasonMsg the reason message (must not be null)
     * @param fieldName the optional field name (may be null)
     */
    public BusinessException(final HttpStatusCode httpStatus,
                             final String reasonMsg,
                             final String fieldName) {
        super(reasonMsg);
        this.status = httpStatus;
        this.reason = reasonMsg;
        this.field = fieldName;
    }

    /**
     * Gets the HTTP status code.
     *
     * @return the HTTP status code
     */
    public HttpStatusCode getStatus() {
        return status;
    }

    /**
     * Gets the reason message.
     *
     * @return the reason message
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the optional field name.
     *
     * @return the field name or null if not set
     */
    public String getField() {
        return field;
    }
}
