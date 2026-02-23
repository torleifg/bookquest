package com.github.torleifg.bookquest.gateway.oai_pmh;

import org.openarchives.oai._2.OAIPMHtype;
import org.springframework.web.client.RestClient;

class OaiPmhClient {
    private final RestClient restClient;

    public OaiPmhClient(RestClient restClient) {
        this.restClient = restClient;
    }

    OAIPMHtype get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OAIPMHtype.class);
    }
}
