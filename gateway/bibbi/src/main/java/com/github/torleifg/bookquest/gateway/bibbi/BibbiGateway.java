package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import com.github.torleifg.bookquest.core.service.HarvestState;
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

class BibbiGateway implements GatewayService {
    private final BibbiProperties.GatewayConfig config;
    private final BibbiClient client;
    private final BibbiMapper mapper;

    public BibbiGateway(BibbiProperties.GatewayConfig config, BibbiClient client, BibbiMapper mapper) {
        this.config = config;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public GatewayResponse find(HarvestState state) {
        final String serviceUri = config.getServiceUri();
        final String requestUri = createRequestUri(serviceUri, state);

        final BibbiResponse response = BibbiResponse.from(client.get(requestUri));

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
                books.add(mapper.from(publication.getId()));
            } else {
                books.add(mapper.from(publication));
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
    public String getServiceUri() {
        return config.getServiceUri();
    }

    private String createRequestUri(String serviceUri, HarvestState state) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?limit=")
                .append(config.getLimit());

        final Optional<ResumptionToken> resumptionToken = state.resumptionToken();

        if (resumptionToken.isPresent() && !resumptionToken.get().isExpired(config.getTtl())) {
            return requestUri.append("&resumption_token=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        return state.lastModified()
                .map(instant -> requestUri.append("&query=")
                        .append(config.getQuery())
                        .append(" AND modified:[")
                        .append(ISO_INSTANT.format(instant))
                        .append(" TO *]")
                        .toString())
                .orElseGet(() -> requestUri.append("&query=")
                        .append(config.getQuery())
                        .toString());
    }
}
