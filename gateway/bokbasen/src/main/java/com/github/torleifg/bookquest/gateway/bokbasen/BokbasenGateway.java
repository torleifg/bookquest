package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import com.github.torleifg.bookquest.core.service.HarvestState;
import org.editeur.ns.onix._3_1.reference.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class BokbasenGateway implements GatewayService {
    private final BokbasenProperties.GatewayConfig config;
    private final BokbasenClient client;
    private final BokbasenMapper mapper;

    BokbasenGateway(BokbasenProperties.GatewayConfig config, BokbasenClient client, BokbasenMapper mapper) {
        this.config = config;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public GatewayResponse find(HarvestState state) {
        final String serviceUri = config.getServiceUri();
        final String requestUri = createRequestUri(serviceUri, state);

        final ResponseEntity<ONIXMessage> entity = client.get(requestUri);

        final BokbasenResponse response = BokbasenResponse.from(entity.getBody());

        final String resumptionToken = Optional.of(entity.getHeaders())
                .map(headers -> headers.get("Next"))
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .orElse(null);

        if (!response.hasProducts()) {
            return new GatewayResponse(requestUri, List.of(), resumptionToken, null);
        }

        final var products = response.getProducts();

        final List<Book> books = new ArrayList<>();

        for (final var product : products) {
            final DescriptiveDetail descriptiveDetail = product.getDescriptiveDetail();

            if (!isBook(descriptiveDetail)) {
                continue;
            }

            if (isDeleted(product.getNotificationType())) {
                books.add(mapper.from(product.getRecordReference().getValue()));
            } else {
                books.add(mapper.from(product));
            }
        }

        return new GatewayResponse(requestUri, books, resumptionToken, null);
    }

    @Override
    public String getServiceUri() {
        return config.getServiceUri();
    }

    private String createRequestUri(String serviceUri, HarvestState state) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?subscription=")
                .append(config.getSubscription())
                .append("&pagesize=")
                .append(config.getPagesize());

        return state.resumptionToken()
                .map(token -> requestUri.append("&next=")
                        .append(token.value())
                        .toString()).orElseGet(() -> requestUri.append("&after=")
                        .append(config.getAfter())
                        .toString());
    }

    private static boolean isBook(DescriptiveDetail descriptiveDetail) {
        return Stream.ofNullable(descriptiveDetail)
                .map(DescriptiveDetail::getProductClassification)
                .flatMap(Collection::stream)
                .filter(productClassification -> productClassification.getProductClassificationType().getValue() == List9.fromValue("07"))
                .map(ProductClassification::getProductClassificationCode)
                .map(ProductClassificationCode::getValue)
                .anyMatch("Bok"::equals);
    }

    private static boolean isDeleted(NotificationType notificationType) {
        return notificationType.getValue() == List1.fromValue("05");
    }
}
