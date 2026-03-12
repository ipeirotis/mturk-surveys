package com.ipeirotis.exception;

public class TaskEnqueueException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TaskEnqueueException() {
        super();
    }

    public TaskEnqueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskEnqueueException(String message) {
        super(message);
    }

    public TaskEnqueueException(Throwable cause) {
        super(cause);
    }
}
