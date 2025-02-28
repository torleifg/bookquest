package com.github.torleifg.bookquest.gateway.oai_pmh;

class OaiPmhException extends RuntimeException {

    OaiPmhException(String message) {
        super(message);
    }

    OaiPmhException(Throwable cause) {
        super(cause);
    }
}
