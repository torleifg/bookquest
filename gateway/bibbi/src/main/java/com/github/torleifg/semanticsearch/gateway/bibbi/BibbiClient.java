package com.github.torleifg.semanticsearch.gateway.bibbi;

import no.bs.bibliografisk.model.GetV1PublicationsHarvest200Response;
import org.springframework.web.client.RestClient;

class BibbiClient {
    private final RestClient restClient;

    BibbiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    GetV1PublicationsHarvest200Response get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(GetV1PublicationsHarvest200Response.class);
    }
}
