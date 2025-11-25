package com.github.torleifg.bookquest.core.service;

public class GatewayException extends RuntimeException {

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(Throwable cause) {
        super(cause);
    }
}
