package io.banjuer.glock.client.exception;

public class GLockInitException extends RuntimeException {
    public GLockInitException() {
        super();
    }

    public GLockInitException(String message) {
        super(message);
    }

    public GLockInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public GLockInitException(Throwable cause) {
        super(cause);
    }

    protected GLockInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
