package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import no.bs.bibliografisk.model.GetV1PublicationsHarvest200Response;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class BibbiClient {
    private final RestClient restClient;

    public BibbiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public GetV1PublicationsHarvest200Response get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(GetV1PublicationsHarvest200Response.class);
    }
}
