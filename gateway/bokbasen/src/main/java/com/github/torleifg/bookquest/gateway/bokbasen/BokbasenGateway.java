package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import org.editeur.ns.onix._3_1.reference.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

record BokbasenGateway(BokbasenProperties.GatewayConfig gatewayConfig, BokbasenClient bokbasenClient,
                       BokbasenMapper bokbasenMapper,
                       ResumptionTokenRepository resumptionTokenRepository) implements GatewayService {

    @Override
    public GatewayResponse find() {
        final String serviceUri = gatewayConfig.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final ResponseEntity<ONIXMessage> entity = bokbasenClient.get(requestUri);

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
                books.add(bokbasenMapper.from(product.getRecordReference().getValue()));
            } else {
                books.add(bokbasenMapper.from(product));
            }
        }

        return new GatewayResponse(requestUri, books, resumptionToken, null);
    }

    @Override
    public void updateHarvestState(GatewayResponse response) {
        final String serviceUri = gatewayConfig.getServiceUri();

        final String token = response.resumptionToken();

        if (token != null && !token.isBlank()) {
            resumptionTokenRepository.save(serviceUri, token);
        } else {
            resumptionTokenRepository.delete(serviceUri);
        }
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?subscription=")
                .append(gatewayConfig.getSubscription())
                .append("&pagesize=")
                .append(gatewayConfig.getPagesize());

        return resumptionTokenRepository.get(serviceUri)
                .map(token -> requestUri.append("&next=")
                        .append(token.value())
                        .toString()).orElseGet(() -> requestUri.append("&after=")
                        .append(gatewayConfig.getAfter())
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
