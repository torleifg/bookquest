package com.github.torleifg.semanticsearch.adapter.rest_client;

import com.github.torleifg.semanticsearch.gateway.common.MetadataClient;
import com.github.torleifg.semanticsearch.gateway.common.MetadataClientException;
import com.github.torleifg.semanticsearch.gateway.common.MetadataClientResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Component
class MetadataClientOAuthAdapter implements MetadataClient {
    private final RestClient OAuthRestClient;

    public MetadataClientOAuthAdapter(RestClient OAuthRestClient) {
        this.OAuthRestClient = OAuthRestClient;
    }

    @Override
    public <T> MetadataClientResponse<T> get(String uri, Class<T> clazz) {
        try {
            final ResponseEntity<T> entity = OAuthRestClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(clazz);

            Optional<String> next = Optional.of(entity.getHeaders())
                    .map(headers -> headers.get("Next"))
                    .map(Collection::stream)
                    .flatMap(Stream::findFirst);

            return new MetadataClientResponse<>(next, entity.getBody());
        } catch (
                RestClientException e) {
            throw new MetadataClientException(e);
        }
    }
}
