package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import no.bs.bibliografisk.model.BibliographicRecordMetadata;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.isBlank;

record BibbiGateway(BibbiProperties.GatewayConfig gatewayConfig, BibbiClient bibbiClient, BibbiMapper bibbiMapper,
                    ResumptionTokenRepository resumptionTokenRepository,
                    LastModifiedRepository lastModifiedRepository) implements GatewayService {

    @Override
    public GatewayResponse find() {
        final String serviceUri = gatewayConfig.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final BibbiResponse response = BibbiResponse.from(bibbiClient.get(requestUri));

        final String resumptionToken = response.getResumptionToken();

        if (!response.hasPublications()) {
            return new GatewayResponse(requestUri, List.of(), resumptionToken, null);
        }

        final var publications = response.getPublications();

        final List<Book> books = new ArrayList<>();

        for (final var publication : publications) {
            if (isBlank(publication.getId())) {
                continue;
            }

            if (publication.getDeleted() != null) {
                books.add(bibbiMapper.from(publication.getId()));
            } else {
                books.add(bibbiMapper.from(publication));
            }
        }

        final Instant lastModified = publications.stream()
                .map(GetV1PublicationsHarvest200ResponsePublicationsInner::getBibliographicRecord)
                .map(BibliographicRecordMetadata::getModified)
                .map(OffsetDateTime::toInstant)
                .max(Comparator.naturalOrder())
                .map(instant -> instant.plusSeconds(1L))
                .orElse(null);

        return new GatewayResponse(requestUri, books, resumptionToken, lastModified);
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

        if (response.lastModified() != null) {
            lastModifiedRepository.save(serviceUri, response.lastModified());
        }
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?limit=")
                .append(gatewayConfig.getLimit());

        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && !resumptionToken.get().isExpired(gatewayConfig.getTtl())) {
            return requestUri.append("&resumption_token=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        return lastModifiedRepository.get(serviceUri)
                .map(instant -> requestUri.append("&query=")
                        .append(gatewayConfig.getQuery())
                        .append(String.format(" AND modified:[%s TO *]", ISO_INSTANT.format(instant)))
                        .toString()).orElseGet(() -> requestUri.append("&query=")
                        .append(gatewayConfig.getQuery())
                        .toString());
    }
}
