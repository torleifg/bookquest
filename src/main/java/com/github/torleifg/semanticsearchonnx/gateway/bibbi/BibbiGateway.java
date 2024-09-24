package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.service.MetadataGateway;
import com.github.torleifg.semanticsearchonnx.gateway.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionToken;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionTokenRepository;
import lombok.extern.slf4j.Slf4j;
import no.bs.bibliografisk.model.BibliographicRecordMetadata;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "metadata", name = "gateway", havingValue = "bibbi")
class BibbiGateway implements MetadataGateway {
    private final BibbiClient bibbiClient;
    private final BibbiMapper bibbiMapper;
    private final BibbiProperties bibbiProperties;

    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    public BibbiGateway(BibbiClient bibbiClient, BibbiMapper bibbiMapper, BibbiProperties bibbiProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
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

        final BibbiResponse response;
        try {
            response = BibbiResponse.from(bibbiClient.get(requestUri));
        } catch (RestClientException ex) {
            if (ex instanceof HttpClientErrorException.BadRequest) {
                resumptionTokenRepository.delete(serviceUri);
            }

            throw new BibbiException(ex);
        }

        response.getResumptionToken().ifPresent(token -> resumptionTokenRepository.save(serviceUri, token));

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
        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && resumptionToken.get().isNotExpired(bibbiProperties.getTtl())) {
            return serviceUri + "?resumption_token=" + resumptionToken.get().value();
        }

        return lastModifiedRepository.get(serviceUri)
                .map(lastModified -> serviceUri + String.format("?query=type:(audiobook OR book) AND modified:[%s TO *]", ISO_INSTANT.format(lastModified)))
                .orElse(serviceUri + "?query=type:(audiobook OR book)");

    }
}
