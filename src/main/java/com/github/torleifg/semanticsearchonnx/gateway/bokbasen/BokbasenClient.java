package com.github.torleifg.semanticsearchonnx.gateway.bokbasen;

import org.editeur.ns.onix._3_0.reference.ONIXMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class BokbasenClient {
    private final RestClient oAuth2RestClient;

    public BokbasenClient(RestClient oAuth2RestClient) {
        this.oAuth2RestClient = oAuth2RestClient;
    }

    public ResponseEntity<ONIXMessage> get(String uri) {
        return oAuth2RestClient
                .get()
                .uri(uri)
                .retrieve()
                .toEntity(ONIXMessage.class);
    }
}
