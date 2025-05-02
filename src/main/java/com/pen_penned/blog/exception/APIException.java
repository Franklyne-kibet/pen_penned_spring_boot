package com.pen_penned.blog.exception;

import java.io.Serial;

public class APIException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public APIException() {
        super("Unexpected API exception");
    }

    public APIException(String message) {
        super(message);
    }

    public APIException(String message, Throwable cause) {
        super(message, cause);
    }
}
