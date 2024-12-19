package com.github.torleifg.semanticsearch.gateway.common;

public interface MetadataClient {

    <T> MetadataClientResponse<T> get(String uri, Class<T> clazz);
}
