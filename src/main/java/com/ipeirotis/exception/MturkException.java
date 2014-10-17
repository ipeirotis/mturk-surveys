package com.ipeirotis.exception;

public class MturkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MturkException() {
        super();
    }

    public MturkException(Throwable cause, String format, Object... formatArgs) {
        super(formatArgs.length == 0 ? format : String.format(format, formatArgs), cause);
    }

    public MturkException(String format, Object... formatArgs) {
        super(formatArgs.length == 0 ? format : String.format(format, formatArgs));
    }

    public MturkException(Throwable cause) {
        super(cause);
    }

}
