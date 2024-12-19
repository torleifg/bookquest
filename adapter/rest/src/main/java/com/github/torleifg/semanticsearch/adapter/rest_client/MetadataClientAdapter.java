package com.github.torleifg.semanticsearch.adapter.rest_client;

import com.github.torleifg.semanticsearch.gateway.common.MetadataClient;
import com.github.torleifg.semanticsearch.gateway.common.MetadataClientException;
import com.github.torleifg.semanticsearch.gateway.common.MetadataClientResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
class MetadataClientAdapter implements MetadataClient {
    private final RestClient restClient;

    public MetadataClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public <T> MetadataClientResponse<T> get(String uri, Class<T> clazz) {
        try {
            final T body = restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(clazz);

            return new MetadataClientResponse<>(Optional.empty(), body);
        } catch (RestClientException e) {
            throw new MetadataClientException(e);
        }
    }
}
