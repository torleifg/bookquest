package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BibbiGatewayTests {

    @Mock
    BibbiClient bibbiClient;

    @Mock
    BibbiMapper bibbiMapper;

    @Mock
    BibbiProperties.GatewayConfig gatewayConfig;

    @Mock
    ResumptionTokenRepository resumptionTokenRepository;

    @Mock
    LastModifiedRepository lastModifiedRepository;

    @InjectMocks
    BibbiGateway bibbiGateway;

    @Test
    void findAllTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getLimit()).thenReturn(100);
        when(gatewayConfig.getQuery()).thenReturn("type:(audiobook OR book)");

        var bibbiResponse = createResponse();
        when(bibbiClient.get("/harvest?limit=100&query=type:(audiobook OR book)")).thenReturn(bibbiResponse);

        var gatewayResponse = bibbiGateway.find();
        assertEquals(0, gatewayResponse.books().size());
    }

    @Test
    void findFromResumptionTokenTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getLimit()).thenReturn(100);
        when(gatewayConfig.getTtl()).thenReturn(5);

        var resumptionToken = "token";
        when(resumptionTokenRepository.get("/harvest")).thenReturn(Optional.of(new ResumptionToken(resumptionToken, Instant.now())));

        var bibbiResponse = createResponse();
        when(bibbiClient.get("/harvest?limit=100&resumption_token=" + resumptionToken)).thenReturn(bibbiResponse);

        var gatewayResponse = bibbiGateway.find();
        assertEquals(0, gatewayResponse.books().size());
    }

    @Test
    void findFromLastModifiedTest() {
        when(gatewayConfig.getServiceUri()).thenReturn("/harvest");
        when(gatewayConfig.getLimit()).thenReturn(100);
        when(gatewayConfig.getQuery()).thenReturn("type:(audiobook OR book)");

        var lastModified = Instant.now();
        when(lastModifiedRepository.get("/harvest")).thenReturn(Optional.of(lastModified));

        var bibbiResponse = createResponse();
        when(bibbiClient.get("/harvest?limit=100&query=type:(audiobook OR book) AND modified:[" + lastModified + " TO *]")).thenReturn(bibbiResponse);

        var gatewayResponse = bibbiGateway.find();
        assertEquals(0, gatewayResponse.books().size());
    }

    GetV1PublicationsHarvest200Response createResponse() {
        var response = new GetV1PublicationsHarvest200Response();
        response.setPublications(List.of());

        return response;
    }
}
