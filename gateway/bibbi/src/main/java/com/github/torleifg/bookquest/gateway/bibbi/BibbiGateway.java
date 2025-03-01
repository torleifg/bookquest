package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.application.repository.ResumptionToken;
import com.github.torleifg.bookquest.application.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.application.service.MetadataGateway;
import lombok.extern.slf4j.Slf4j;
import no.bs.bibliografisk.model.BibliographicRecordMetadata;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
class BibbiGateway implements MetadataGateway {
    private final BibbiClient bibbiClient;
    private final BibbiMapper bibbiMapper;
    private final BibbiProperties bibbiProperties;

    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    BibbiGateway(BibbiClient bibbiClient, BibbiMapper bibbiMapper, BibbiProperties bibbiProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        this.bibbiClient = bibbiClient;
        this.bibbiMapper = bibbiMapper;
        this.bibbiProperties = bibbiProperties;

        this.resumptionTokenRepository = resumptionTokenRepository;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    @Override
    public List<Book> find() {
        final String serviceUri = bibbiProperties.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final BibbiResponse response = BibbiResponse.from(bibbiClient.get(requestUri));

        final Optional<String> resumptionToken = response.getResumptionToken();

        if (resumptionToken.isPresent()) {
            resumptionTokenRepository.save(serviceUri, resumptionToken.get());
        } else {
            resumptionTokenRepository.get(serviceUri)
                    .filter(token -> token.isNotExpired(bibbiProperties.getTtl()))
                    .ifPresent(token -> resumptionTokenRepository.save(serviceUri, token.value()));
        }

        if (!response.hasPublications()) {
            log.info("Received 0 publications from {}", requestUri);

            return List.of();
        }

        final var publications = response.getPublications();

        log.info("Received {} publications from {}", publications.size(), requestUri);

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

        return books;
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?limit=")
                .append(bibbiProperties.getLimit());

        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && resumptionToken.get().isNotExpired(bibbiProperties.getTtl())) {
            return requestUri.append("&resumption_token=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        final Optional<Instant> lastModified = lastModifiedRepository.get(serviceUri);

        if (lastModified.isPresent()) {
            return requestUri.append("&query=")
                    .append(bibbiProperties.getQuery())
                    .append(String.format(" AND modified:[%s TO *]", ISO_INSTANT.format(lastModified.get())))
                    .toString();
        }

        return requestUri.append("&query=")
                .append(bibbiProperties.getQuery())
                .toString();
    }
}
