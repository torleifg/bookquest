package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.editeur.ns.onix._3_1.reference.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
class BokbasenGateway implements GatewayService {
    private final BokbasenProperties.GatewayConfig gatewayConfig;

    private final BokbasenClient bokbasenClient;
    private final BokbasenMapper bokbasenMapper;

    private final ResumptionTokenRepository resumptionTokenRepository;

    BokbasenGateway(BokbasenProperties.GatewayConfig gatewayConfig, BokbasenClient bokbasenClient, BokbasenMapper bokbasenMapper, ResumptionTokenRepository resumptionTokenRepository) {
        this.gatewayConfig = gatewayConfig;

        this.bokbasenClient = bokbasenClient;
        this.bokbasenMapper = bokbasenMapper;

        this.resumptionTokenRepository = resumptionTokenRepository;
    }

    @Override
    public List<Book> find() {
        final String serviceUri = gatewayConfig.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final ResponseEntity<ONIXMessage> entity = bokbasenClient.get(requestUri);
        final BokbasenResponse response = BokbasenResponse.from(entity.getBody());

        Optional.of(entity.getHeaders())
                .map(headers -> headers.get("Next"))
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .ifPresent(token -> resumptionTokenRepository.save(serviceUri, token));

        if (!response.hasProducts()) {
            log.info("Received 0 products from {}", requestUri);

            return List.of();
        }

        final var products = response.getProducts();

        log.info("Received {} product(s) from {}", products.size(), requestUri);

        final List<Book> books = new ArrayList<>();

        for (final var product : products) {
            final DescriptiveDetail descriptiveDetail = product.getDescriptiveDetail();

            if (!isBook(descriptiveDetail)) {
                continue;
            }

            if (isDeleted(product.getNotificationType())) {
                books.add(bokbasenMapper.from(product.getRecordReference().getValue()));
            }

            books.add(bokbasenMapper.from(product));
        }

        return books;
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?subscription=")
                .append(gatewayConfig.getSubscription())
                .append("&pagesize=")
                .append(gatewayConfig.getPagesize());

        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent()) {
            return requestUri.append("&next=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        return requestUri.append("&after=")
                .append(gatewayConfig.getAfter())
                .toString();
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
