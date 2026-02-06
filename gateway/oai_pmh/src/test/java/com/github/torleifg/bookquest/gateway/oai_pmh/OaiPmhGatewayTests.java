package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.service.HarvestState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.ObjectFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OaiPmhGatewayTests {

    @Mock
    OaiPmhClient oaiPmhClient;

    @Mock
    OaiPmhMapper oaiPmhMapper;

    @Mock
    OaiPmhProperties.GatewayConfig gatewayConfig;

    @InjectMocks
    OaiPmhGateway oaiPmhGateway;

    final ObjectFactory objectFactory = new ObjectFactory();

    @Test
    void findAllTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getVerb()).thenReturn("ListRecords");
        when(gatewayConfig.getMetadataPrefix()).thenReturn("marc21");

        var oaiPmhResponse = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&metadataPrefix=marc21")).thenReturn(oaiPmhResponse);

        var state = new HarvestState(Optional.empty(), Optional.empty());
        var gatewayResponse = oaiPmhGateway.find(state);

        assertEquals(0, gatewayResponse.books().size());
    }

    @Test
    void findFromResumptionTokenTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getVerb()).thenReturn("ListRecords");
        when(gatewayConfig.getTtl()).thenReturn(5L);

        var resumptionToken = "token";
        var tokenObj = new ResumptionToken(resumptionToken, Instant.now().minus(2, ChronoUnit.MINUTES));

        var oaiPmhResponse = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&resumptionToken=" + resumptionToken)).thenReturn(oaiPmhResponse);

        var state = new HarvestState(Optional.of(tokenObj), Optional.empty());
        var gatewayResponse = oaiPmhGateway.find(state);
        assertEquals(0, gatewayResponse.books().size());
    }

    @Test
    void findFromExpiredResumptionTokenTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getVerb()).thenReturn("ListRecords");
        when(gatewayConfig.getMetadataPrefix()).thenReturn("marc21");
        when(gatewayConfig.getTtl()).thenReturn(5L);

        var resumptionToken = "token";
        var tokenObj = new ResumptionToken(resumptionToken, Instant.now().minus(6, ChronoUnit.MINUTES));
        var lastModified = Instant.now();

        var oaiPmhResponse = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&metadataPrefix=marc21&from=" + ISO_INSTANT.format(lastModified))).thenReturn(oaiPmhResponse);

        var state = new HarvestState(Optional.of(tokenObj), Optional.of(lastModified));
        var gatewayResponse = oaiPmhGateway.find(state);
        assertEquals(0, gatewayResponse.books().size());
    }

    @Test
    void findFromLastModifiedTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getVerb()).thenReturn("ListRecords");
        when(gatewayConfig.getMetadataPrefix()).thenReturn("marc21");

        var lastModified = Instant.now();

        var oaiPmhResponse = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&metadataPrefix=marc21&from=" + ISO_INSTANT.format(lastModified))).thenReturn(oaiPmhResponse);

        var state = new HarvestState(Optional.empty(), Optional.of(lastModified));
        var gatewayResponse = oaiPmhGateway.find(state);
        assertEquals(0, gatewayResponse.books().size());
    }

    OAIPMHtype createResponse() {
        var response = objectFactory.createOAIPMHtype();

        var listRecordsType = objectFactory.createListRecordsType();
        response.setListRecords(listRecordsType);

        return response;
    }
}
