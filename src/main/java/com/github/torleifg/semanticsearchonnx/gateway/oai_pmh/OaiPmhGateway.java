package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.service.MetadataGateway;
import com.github.torleifg.semanticsearchonnx.gateway.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionToken;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionTokenRepository;
import info.lc.xmlns.marcxchange_v1.RecordType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.StatusType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.w3c.dom.Element;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "metadata", name = "gateway", havingValue = "oai-pmh")
class OaiPmhGateway implements MetadataGateway {
    private final OaiPmhClient oaiPmhClient;
    private final OaiPmhMapper oaiPmhMapper;
    private final OaiPmhProperties oaiPmhProperties;

    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(RecordType.class);
        } catch (JAXBException e) {
            throw new OaiPmhException(e);
        }
    }

    public OaiPmhGateway(OaiPmhClient oaiPmhClient, OaiPmhMapper oaiPmhMapper, OaiPmhProperties oaiPmhProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        this.oaiPmhClient = oaiPmhClient;
        this.oaiPmhMapper = oaiPmhMapper;
        this.oaiPmhProperties = oaiPmhProperties;

        this.resumptionTokenRepository = resumptionTokenRepository;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    @Override
    public List<Book> find() {
        final String serviceUri = oaiPmhProperties.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final OaiPmhResponse response;
        try {
            response = OaiPmhResponse.from(oaiPmhClient.get(requestUri));
        } catch (RestClientResponseException ex) {
            if (ex instanceof HttpServerErrorException.InternalServerError) {
                resumptionTokenRepository.delete(serviceUri);
            }

            throw new OaiPmhException(ex);
        }

        if (response.hasErrors()) {

            if (response.hasBadResumptionTokenError()) {
                resumptionTokenRepository.delete(serviceUri);
            }

            if (response.hasNoRecordsMatchError()) {
                log.info("Received 0 records from {}", requestUri);

                return List.of();
            }

            throw new OaiPmhException(response.errorsToString());
        }

        if (!response.hasRecords()) {
            log.info("Received 0 record(s) from {}", requestUri);

            return List.of();
        }

        response.getResumptionToken().ifPresent(token -> resumptionTokenRepository.save(serviceUri, token));

        final var records = response.getRecords();

        log.info("Received {} record(s) from {}", records.size(), requestUri);

        final Unmarshaller unmarshaller;
        try {
            unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new OaiPmhException(e);
        }

        final List<Book> books = new ArrayList<>();

        for (final var record : records) {
            final String identifier = record.getHeader().getIdentifier();

            if (record.getHeader().getStatus() == StatusType.DELETED) {
                books.add(oaiPmhMapper.from(identifier));

                continue;
            }

            if (!(record.getMetadata().getAny() instanceof Element element)) {
                continue;
            }

            final RecordType metadata;
            try {
                metadata = unmarshaller.unmarshal(element, RecordType.class).getValue();
            } catch (JAXBException e) {
                throw new OaiPmhException(e);
            }

            books.add(oaiPmhMapper.from(identifier, metadata));
        }

        Optional.of(records.getLast())
                .map(org.openarchives.oai._2.RecordType::getHeader)
                .map(HeaderType::getDatestamp)
                .map(Instant::parse)
                .ifPresent(lastModified -> lastModifiedRepository.save(serviceUri, lastModified.plusSeconds(1L)));

        return books;
    }

    private String createRequestUri(String serviceUri) {
        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && resumptionToken.get().isNotExpired(oaiPmhProperties.getTtl())) {
            return serviceUri + "?verb=ListRecords&resumptionToken=" + resumptionToken.get().value();
        }

        return lastModifiedRepository.get(serviceUri)
                .map(lastModified -> serviceUri + "?verb=ListRecords&metadataPrefix=marc21&from=" + ISO_INSTANT.format(lastModified))
                .orElse(serviceUri + "?verb=ListRecords&metadataPrefix=marc21");
    }
}
