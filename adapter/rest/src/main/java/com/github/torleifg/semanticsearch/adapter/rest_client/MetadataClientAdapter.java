package com.github.torleifg.semanticsearch.adapter.rest_client;

import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClient;
import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClientException;
import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClientResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Component
class MetadataClientAdapter implements MetadataClient {
    private final RestClient restClient;

    MetadataClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public <T> MetadataClientResponse<T> get(String uri, Class<T> clazz) {
        final ResponseEntity<T> entity;

        try {
            entity = restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(clazz);
        } catch (RestClientException e) {
            throw new MetadataClientException(e);
        }

        final MetadataClientResponse<T> metadataClientResponse = new MetadataClientResponse<>(entity.getBody());

        Optional.of(entity.getHeaders())
                .map(headers -> headers.get("Next"))
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .ifPresent(metadataClientResponse::setResumptionToken);

        return metadataClientResponse;
    }
}
