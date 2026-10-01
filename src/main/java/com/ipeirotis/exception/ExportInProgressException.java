package com.ipeirotis.exception;

/**
 * Another BigQuery export for the same date holds the lease. Mapped to HTTP 409 so
 * Cloud Tasks retries the delivery later.
 */
public class ExportInProgressException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExportInProgressException(String message) {
        super(message);
    }
}
