package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.service.GatewayException;
import com.github.torleifg.bookquest.core.service.GatewayResponse;
import com.github.torleifg.bookquest.core.service.GatewayService;
import com.github.torleifg.bookquest.core.service.HarvestState;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.StatusType;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

record OaiPmhGateway(OaiPmhProperties.GatewayConfig gatewayConfig, OaiPmhClient oaiPmhClient,
                     OaiPmhMapper oaiPmhMapper) implements GatewayService {
    private static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        try {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
        } catch (TransformerFactoryConfigurationError e) {
            throw new GatewayException(e);
        }
    }

    @Override
    public GatewayResponse find(HarvestState state) {
        final String serviceUri = gatewayConfig.getServiceUri();
        final String requestUri = createRequestUri(serviceUri, state);

        final OaiPmhResponse response = OaiPmhResponse.from(oaiPmhClient.get(requestUri));

        if (response.hasUnrecoverableErrors()) {
            throw new GatewayException(response.errorsToString());
        }

        final String resumptionToken = response.getResumptionToken();

        if (!response.hasRecords()) {
            return new GatewayResponse(requestUri, List.of(), resumptionToken, null);
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

        final Instant lastModified = oaiPmhRecords.stream()
                .map(RecordType::getHeader)
                .map(HeaderType::getDatestamp)
                .filter(datestamp -> datestamp != null && !datestamp.isBlank())
                .map(Instant::parse)
                .max(Comparator.naturalOrder())
                .map(instant -> instant.plusSeconds(1L))
                .orElse(null);

        return new GatewayResponse(requestUri, books, resumptionToken, lastModified);
    }

    @Override
    public String getServiceUri() {
        return gatewayConfig.getServiceUri();
    }

    private String createRequestUri(String serviceUri, HarvestState state) {
        final StringBuilder requestUri = new StringBuilder(serviceUri)
                .append("?verb=")
                .append(gatewayConfig.getVerb());

        final Optional<ResumptionToken> resumptionToken = state.resumptionToken();

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

        return state.lastModified()
                .map(instant -> requestUri.append("&from=")
                        .append(ISO_INSTANT.format(instant))
                        .toString()).orElseGet(requestUri::toString);
    }
}
