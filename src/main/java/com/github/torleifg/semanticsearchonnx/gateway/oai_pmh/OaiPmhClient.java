package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import org.openarchives.oai._2.OAIPMHtype;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class OaiPmhClient {
    private final RestClient restClient;

    public OaiPmhClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public OAIPMHtype get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OAIPMHtype.class);
    }
}
