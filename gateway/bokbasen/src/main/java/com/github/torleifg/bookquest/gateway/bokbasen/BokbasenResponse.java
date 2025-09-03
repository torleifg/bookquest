package com.github.torleifg.bookquest.gateway.bokbasen;

import org.editeur.ns.onix._3_1.reference.ONIXMessage;
import org.editeur.ns.onix._3_1.reference.Product;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

record BokbasenResponse(ONIXMessage onixMessage) {

    static BokbasenResponse from(ONIXMessage onixMessage) {
        return new BokbasenResponse(onixMessage);
    }

    boolean hasProducts() {
        return onixMessage.getProduct() != null && !onixMessage.getProduct().isEmpty();
    }

    List<Product> getProducts() {
        return Stream.ofNullable(onixMessage)
                .map(ONIXMessage::getProduct)
                .flatMap(Collection::stream)
                .toList();
    }
}
