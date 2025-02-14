package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearch.book.repository.ResumptionToken;
import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import com.github.torleifg.semanticsearch.book.service.MetadataGateway;
import lombok.extern.slf4j.Slf4j;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.StatusType;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Slf4j
class OaiPmhGateway implements MetadataGateway {
    private final OaiPmhClient oaiPmhClient;
    private final OaiPmhMapper oaiPmhMapper;
    private final OaiPmhProperties oaiPmhProperties;

    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    private static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        try {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
        } catch (TransformerFactoryConfigurationError e) {
            throw new OaiPmhException(e);
        }
    }

    OaiPmhGateway(OaiPmhClient oaiPmhClient, OaiPmhMapper oaiPmhMapper, OaiPmhProperties oaiPmhProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
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

        final OaiPmhResponse response = OaiPmhResponse.from(oaiPmhClient.get(requestUri));

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

        final Optional<String> resumptionToken = response.getResumptionToken();

        if (resumptionToken.isPresent()) {
            resumptionTokenRepository.save(serviceUri, resumptionToken.get());
        } else {
            resumptionTokenRepository.get(serviceUri)
                    .filter(token -> token.isNotExpired(oaiPmhProperties.getTtl()))
                    .ifPresent(token -> resumptionTokenRepository.save(serviceUri, token.value()));
        }

        if (!response.hasRecords()) {
            log.info("Received 0 record(s) from {}", requestUri);

            return List.of();
        }

        final var oaiPmhrecords = response.getRecords();

        log.info("Received {} record(s) from {}", oaiPmhrecords.size(), requestUri);

        final List<Book> books = new ArrayList<>();

        for (final var oaiPmhRecord : oaiPmhrecords) {
            final String identifier = oaiPmhRecord.getHeader().getIdentifier();

            if (oaiPmhRecord.getHeader().getStatus() == StatusType.DELETED) {
                books.add(oaiPmhMapper.from(identifier));

                continue;
            }

            if (!(oaiPmhRecord.getMetadata().getAny() instanceof Element element)) {
                continue;
            }

            final MarcXmlReader marcXmlReader;
            try {
                final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

                final StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(element), new StreamResult(writer));

                marcXmlReader = new MarcXmlReader(new ByteArrayInputStream(writer.toString().getBytes()));
            } catch (TransformerException e) {
                continue;
            }

            while (marcXmlReader.hasNext()) {
                final Record record = marcXmlReader.next();

                books.add(oaiPmhMapper.from(identifier, record));
            }
        }

        Optional.of(oaiPmhrecords.getLast())
                .map(org.openarchives.oai._2.RecordType::getHeader)
                .map(HeaderType::getDatestamp)
                .map(Instant::parse)
                .ifPresent(lastModified -> lastModifiedRepository.save(serviceUri, lastModified.plusSeconds(1L)));

        return books;
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?verb=")
                .append(oaiPmhProperties.getVerb());

        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && resumptionToken.get().isNotExpired(oaiPmhProperties.getTtl())) {
            return requestUri.append("&resumptionToken=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        requestUri.append("&metadataPrefix=")
                .append(oaiPmhProperties.getMetadataPrefix());

        if (oaiPmhProperties.getSet() != null && !oaiPmhProperties.getSet().isBlank()) {
            requestUri.append("&set=").append(oaiPmhProperties.getSet());
        }

        final Optional<Instant> lastModified = lastModifiedRepository.get(serviceUri);

        if (lastModified.isPresent()) {
            return requestUri.append("&from=")
                    .append(ISO_INSTANT.format(lastModified.get()))
                    .toString();
        }

        return requestUri.toString();
    }
}
