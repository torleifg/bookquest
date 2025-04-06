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
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.isBlank;

class BibbiGateway implements GatewayService {
    private final BibbiProperties.GatewayConfig gatewayConfig;

    private final BibbiClient bibbiClient;
    private final BibbiMapper bibbiMapper;

    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    BibbiGateway(BibbiProperties.GatewayConfig gatewayConfig, BibbiClient bibbiClient, BibbiMapper bibbiMapper, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        this.gatewayConfig = gatewayConfig;

        this.bibbiClient = bibbiClient;
        this.bibbiMapper = bibbiMapper;

        this.resumptionTokenRepository = resumptionTokenRepository;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    @Override
    public GatewayResponse find() {
        final String serviceUri = gatewayConfig.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final BibbiResponse response = BibbiResponse.from(bibbiClient.get(requestUri));

        final Optional<String> resumptionToken = response.getResumptionToken();

        if (resumptionToken.isPresent()) {
            resumptionTokenRepository.save(serviceUri, resumptionToken.get());
        } else {
            resumptionTokenRepository.get(serviceUri)
                    .filter(token -> token.isNotExpired(gatewayConfig.getTtl()))
                    .ifPresent(token -> resumptionTokenRepository.save(serviceUri, token.value()));
        }

        if (!response.hasPublications()) {
            return new GatewayResponse(requestUri, List.of());
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

        Optional.of(publications.getLast())
                .map(GetV1PublicationsHarvest200ResponsePublicationsInner::getBibliographicRecord)
                .map(BibliographicRecordMetadata::getModified)
                .map(OffsetDateTime::toInstant)
                .ifPresent(lastModified -> lastModifiedRepository.save(serviceUri, lastModified.plusSeconds(1L)));

        return new GatewayResponse(requestUri, books);
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?limit=")
                .append(gatewayConfig.getLimit());

        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && resumptionToken.get().isNotExpired(gatewayConfig.getTtl())) {
            return requestUri.append("&resumption_token=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        final Optional<Instant> lastModified = lastModifiedRepository.get(serviceUri);

        if (lastModified.isPresent()) {
            return requestUri.append("&query=")
                    .append(gatewayConfig.getQuery())
                    .append(String.format(" AND modified:[%s TO *]", ISO_INSTANT.format(lastModified.get())))
                    .toString();
        }

        return requestUri.append("&query=")
                .append(gatewayConfig.getQuery())
                .toString();
    }
}
