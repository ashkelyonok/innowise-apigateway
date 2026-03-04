package org.ashkelyonok.apigateway.exception;

import java.io.Serial;

public class InvalidSecurityParametersException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2165406166982856477L;

    public InvalidSecurityParametersException() {
        super("Security parameters are invalid");
    }

    public InvalidSecurityParametersException(String message) {
        super(message);
    }

    public InvalidSecurityParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}