package com.github.torleifg.bookquest.gateway.bokbasen;

import org.editeur.ns.onix._3_1.reference.ONIXMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

record BokbasenClient(RestClient restClient) {

    ResponseEntity<ONIXMessage> get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .toEntity(ONIXMessage.class);
    }
}
