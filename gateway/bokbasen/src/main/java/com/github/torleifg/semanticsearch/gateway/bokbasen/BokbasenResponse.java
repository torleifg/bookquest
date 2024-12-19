package com.github.torleifg.semanticsearch.gateway.bokbasen;

import org.editeur.ns.onix._3_0.reference.ONIXMessage;
import org.editeur.ns.onix._3_0.reference.Product;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class BokbasenResponse {
    private final ONIXMessage onixMessage;

    private BokbasenResponse(ONIXMessage onixMessage) {
        this.onixMessage = onixMessage;
    }

    public static BokbasenResponse from(ONIXMessage onixMessage) {
        return new BokbasenResponse(onixMessage);
    }

    public boolean hasProducts() {
        return onixMessage.getProduct() != null && !onixMessage.getProduct().isEmpty();
    }

    public List<Product> getProducts() {
        return Stream.ofNullable(onixMessage)
                .map(ONIXMessage::getProduct)
                .flatMap(Collection::stream)
                .toList();
    }
}
