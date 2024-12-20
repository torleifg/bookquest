package com.github.torleifg.semanticsearch.gateway.common.client;

import lombok.Data;

@Data
public class MetadataClientResponse<T> {
    private T body;
    private String resumptionToken;

    public MetadataClientResponse(T body) {
        this.body = body;
    }
}
