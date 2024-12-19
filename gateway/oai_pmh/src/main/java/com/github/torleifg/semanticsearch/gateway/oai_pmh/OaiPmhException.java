package com.github.torleifg.semanticsearch.gateway.oai_pmh;

class OaiPmhException extends RuntimeException {

    public OaiPmhException(String message) {
        super(message);
    }

    public OaiPmhException(Throwable cause) {
        super(cause);
    }
}
