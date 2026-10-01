package com.ipeirotis.exception;

/**
 * Thrown when code tries to call the MTurk API after Amazon shut the service
 * down (see {@link com.ipeirotis.service.MturkService#CLOSURE_DATE}).
 */
public class MturkClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MturkClosedException(String message) {
        super(message);
    }
}
