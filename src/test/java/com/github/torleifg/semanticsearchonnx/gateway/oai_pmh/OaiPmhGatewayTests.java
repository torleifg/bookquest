package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import com.github.torleifg.semanticsearchonnx.gateway.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionToken;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.ObjectFactory;

import java.time.Instant;
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
    OaiPmhProperties oaiPmhProperties;

    @Mock
    ResumptionTokenRepository resumptionTokenRepository;

    @Mock
    LastModifiedRepository lastModifiedRepository;

    @InjectMocks
    OaiPmhGateway oaiPmhGateway;

    ObjectFactory objectFactory = new ObjectFactory();

    @Test
    void findAllTest() {
        when(oaiPmhProperties.getServiceUri()).thenReturn("/harvest");

        var response = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&metadataPrefix=marc21")).thenReturn(response);

        var metadata = oaiPmhGateway.find();

        assertEquals(0, metadata.size());
    }

    @Test
    void findFromResumptionTokenTest() {
        when(oaiPmhProperties.getServiceUri()).thenReturn("/harvest");
        when(oaiPmhProperties.getTtl()).thenReturn(5L);

        var resumptionToken = "token";
        when(resumptionTokenRepository.get("/harvest")).thenReturn(Optional.of(new ResumptionToken(resumptionToken, Instant.now())));

        var response = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&resumptionToken=" + resumptionToken)).thenReturn(response);

        var metadata = oaiPmhGateway.find();
        assertEquals(0, metadata.size());
    }

    @Test
    void findFromLastModifiedTest() {
        when(oaiPmhProperties.getServiceUri()).thenReturn("/harvest");

        var lastModified = Instant.now();
        when(lastModifiedRepository.get("/harvest")).thenReturn(Optional.of(lastModified));

        var response = createResponse();
        when(oaiPmhClient.get("/harvest?verb=ListRecords&metadataPrefix=marc21&from=" + ISO_INSTANT.format(lastModified))).thenReturn(response);

        var metadata = oaiPmhGateway.find();
        assertEquals(0, metadata.size());
    }

    OAIPMHtype createResponse() {
        var response = objectFactory.createOAIPMHtype();

        var listRecordsType = objectFactory.createListRecordsType();
        response.setListRecords(listRecordsType);

        return response;
    }
}
