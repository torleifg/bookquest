package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import com.github.torleifg.semanticsearch.book.service.MetadataGateway;
import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClient;
import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClientException;
import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClientResponse;
import com.github.torleifg.semanticsearch.gateway.common.repository.ResumptionTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.editeur.ns.onix._3_0.reference.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "scheduler", name = "gateway", havingValue = "bokbasen")
class BokbasenGateway implements MetadataGateway {
    private final MetadataClient metadataClient;
    private final BokbasenMapper bokbasenMapper;
    private final BokbasenProperties bokbasenProperties;

    private final ResumptionTokenRepository resumptionTokenRepository;

    BokbasenGateway(MetadataClient metadataClient, BokbasenMapper bokbasenMapper, BokbasenProperties bokbasenProperties, ResumptionTokenRepository resumptionTokenRepository) {
        this.metadataClient = metadataClient;
        this.bokbasenMapper = bokbasenMapper;
        this.bokbasenProperties = bokbasenProperties;

        this.resumptionTokenRepository = resumptionTokenRepository;
    }

    @Override
    public List<MetadataDTO> find() {
        final String serviceUri = bokbasenProperties.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final BokbasenResponse response;
        try {
            final MetadataClientResponse<ONIXMessage> metadataClientResponse = metadataClient.get(requestUri, ONIXMessage.class);
            response = BokbasenResponse.from(metadataClientResponse.getBody());

            Optional.ofNullable(metadataClientResponse.getResumptionToken()).ifPresent(token -> resumptionTokenRepository.save(serviceUri, token));
        } catch (MetadataClientException ex) {
            throw new BokbasenException(ex);
        }

        if (!response.hasProducts()) {
            log.info("Received 0 products from {}", requestUri);

            return List.of();
        }

        final var products = response.getProducts();

        log.info("Received {} product(s) from {}", products.size(), requestUri);

        final List<MetadataDTO> metadata = new ArrayList<>();

        for (final var product : products) {
            final DescriptiveDetail descriptiveDetail = product.getDescriptiveDetail();

            if (!isBook(descriptiveDetail)) {
                continue;
            }

            if (isDeleted(product.getNotificationType())) {
                metadata.add(bokbasenMapper.from(product.getRecordReference().getValue()));
            }

            metadata.add(bokbasenMapper.from(product));
        }

        return metadata;
    }

    private String createRequestUri(String serviceUri) {
        return resumptionTokenRepository.get(serviceUri)
                .map(token -> serviceUri + "?subscription=" + bokbasenProperties.getSubscription() + "&next=" + token.value())
                .orElseGet(() -> serviceUri + "?subscription=" + bokbasenProperties.getSubscription() + "&after=" + bokbasenProperties.getAfter());
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
