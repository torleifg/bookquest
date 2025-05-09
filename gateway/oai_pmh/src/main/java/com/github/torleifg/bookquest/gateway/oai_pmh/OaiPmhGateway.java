package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
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

class OaiPmhGateway implements GatewayService {
    private final OaiPmhProperties.GatewayConfig gatewayConfig;

    private final OaiPmhClient oaiPmhClient;
    private final OaiPmhMapper oaiPmhMapper;

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

    OaiPmhGateway(OaiPmhProperties.GatewayConfig gatewayConfig, OaiPmhClient oaiPmhClient, OaiPmhMapper oaiPmhMapper, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        this.gatewayConfig = gatewayConfig;

        this.oaiPmhClient = oaiPmhClient;
        this.oaiPmhMapper = oaiPmhMapper;

        this.resumptionTokenRepository = resumptionTokenRepository;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    @Override
    public GatewayResponse find() {
        final String serviceUri = gatewayConfig.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final OaiPmhResponse response = OaiPmhResponse.from(oaiPmhClient.get(requestUri));

        if (response.hasErrors()) {

            if (response.hasBadResumptionTokenError()) {
                resumptionTokenRepository.delete(serviceUri);
            }

            if (response.hasNoRecordsMatchError()) {
                return new GatewayResponse(requestUri, List.of());
            }

            throw new OaiPmhException(response.errorsToString());
        }

        final Optional<String> resumptionToken = response.getResumptionToken();

        if (resumptionToken.isPresent()) {
            resumptionTokenRepository.save(serviceUri, resumptionToken.get());
        } else {
            resumptionTokenRepository.get(serviceUri)
                    .filter(token -> !token.isExpired(gatewayConfig.getTtl()))
                    .ifPresent(token -> resumptionTokenRepository.save(serviceUri, token.value()));
        }

        if (!response.hasRecords()) {
            return new GatewayResponse(requestUri, List.of());
        }

        final var oaiPmhRecords = response.getRecords();

        final List<Book> books = new ArrayList<>();

        for (final var oaiPmhRecord : oaiPmhRecords) {
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

        Optional.of(oaiPmhRecords.getLast())
                .map(org.openarchives.oai._2.RecordType::getHeader)
                .map(HeaderType::getDatestamp)
                .map(Instant::parse)
                .ifPresent(lastModified -> lastModifiedRepository.save(serviceUri, lastModified.plusSeconds(1L)));

        return new GatewayResponse(requestUri, books);
    }

    private String createRequestUri(String serviceUri) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?verb=")
                .append(gatewayConfig.getVerb());

        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && !resumptionToken.get().isExpired(gatewayConfig.getTtl())) {
            return requestUri.append("&resumptionToken=")
                    .append(resumptionToken.get().value())
                    .toString();
        }

        requestUri.append("&metadataPrefix=")
                .append(gatewayConfig.getMetadataPrefix());

        if (gatewayConfig.getSet() != null && !gatewayConfig.getSet().isBlank()) {
            requestUri.append("&set=").append(gatewayConfig.getSet());
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
