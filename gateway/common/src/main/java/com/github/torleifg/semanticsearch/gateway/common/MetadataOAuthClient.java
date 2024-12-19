package com.github.torleifg.semanticsearch.gateway.common;

public interface MetadataOAuthClient {

    <T> T get(String uri, Class<T> clazz);
}
