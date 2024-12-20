package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClient;
import com.github.torleifg.semanticsearch.gateway.common.client.MetadataClientResponse;
import com.github.torleifg.semanticsearch.gateway.common.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearch.gateway.common.repository.ResumptionToken;
import com.github.torleifg.semanticsearch.gateway.common.repository.ResumptionTokenRepository;
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
    MetadataClient metadataClient;

    @Mock
    BibbiMapper bibbiMapper;

    @Mock
    BibbiProperties bibbiProperties;

    @Mock
    ResumptionTokenRepository resumptionTokenRepository;

    @Mock
    LastModifiedRepository lastModifiedRepository;

    @InjectMocks
    BibbiGateway bibbiGateway;

    @Test
    void findAllTest() {
        when(bibbiProperties.getServiceUri()).thenReturn("/harvest");

        var response = createResponse();
        when(metadataClient.get("/harvest?query=type:(audiobook OR book)", GetV1PublicationsHarvest200Response.class)).thenReturn(new MetadataClientResponse<>(response));

        var metadata = bibbiGateway.find();
        assertEquals(0, metadata.size());
    }

    @Test
    void findFromResumptionTokenTest() {
        when(bibbiProperties.getServiceUri()).thenReturn("/harvest");
        when(bibbiProperties.getTtl()).thenReturn(5L);

        var resumptionToken = "token";
        when(resumptionTokenRepository.get("/harvest")).thenReturn(Optional.of(new ResumptionToken(resumptionToken, Instant.now())));

        var response = createResponse();
        when(metadataClient.get("/harvest?resumption_token=" + resumptionToken, GetV1PublicationsHarvest200Response.class)).thenReturn(new MetadataClientResponse<>(response));

        var metadata = bibbiGateway.find();
        assertEquals(0, metadata.size());
    }

    @Test
    void findFromLastModifiedTest() {
        when(bibbiProperties.getServiceUri()).thenReturn("/harvest");

        var lastModified = Instant.now();
        when(lastModifiedRepository.get("/harvest")).thenReturn(Optional.of(lastModified));

        var response = createResponse();
        when(metadataClient.get("/harvest?query=type:(audiobook OR book) AND modified:[" + lastModified + " TO *]", GetV1PublicationsHarvest200Response.class)).thenReturn(new MetadataClientResponse<>(response));

        var metadata = bibbiGateway.find();
        assertEquals(0, metadata.size());
    }

    GetV1PublicationsHarvest200Response createResponse() {
        var response = new GetV1PublicationsHarvest200Response();
        response.setPublications(List.of());

        return response;
    }
}
