package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.gateway.common.MetadataClient;
import com.github.torleifg.semanticsearch.gateway.common.MetadataClientResponse;
import com.github.torleifg.semanticsearch.gateway.common.ResumptionToken;
import com.github.torleifg.semanticsearch.gateway.common.ResumptionTokenRepository;
import org.editeur.ns.onix._3_0.reference.ONIXMessage;
import org.editeur.ns.onix._3_0.reference.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BokbasenGatewayTests {

    @Mock
    MetadataClient metadataClient;

    @Mock
    BokbasenMapper bokbasenMapper;

    @Mock
    BokbasenProperties bokbasenProperties;

    @Mock
    ResumptionTokenRepository resumptionTokenRepository;

    @InjectMocks
    BokbasenGateway bokbasenGateway;

    ObjectFactory objectFactory = new ObjectFactory();

    @Test
    void findAllTest() {
        when(bokbasenProperties.getServiceUri()).thenReturn("/harvest");
        when(bokbasenProperties.getSubscription()).thenReturn("extended");
        when(bokbasenProperties.getAfter()).thenReturn("19700101090000");

        var response = createResponse();

        when(metadataClient.get("/harvest?subscription=extended&after=19700101090000", ONIXMessage.class)).thenReturn(new MetadataClientResponse<>(Optional.empty(), response));

        var metadata = bokbasenGateway.find();
        assertEquals(0, metadata.size());
    }

    @Test
    void findFromResumptionTokenTest() {
        when(bokbasenProperties.getServiceUri()).thenReturn("/harvest");
        when(bokbasenProperties.getSubscription()).thenReturn("extended");

        var resumptionToken = "token";
        when(resumptionTokenRepository.get("/harvest")).thenReturn(Optional.of(new ResumptionToken(resumptionToken, Instant.now())));

        var response = createResponse();

        when(metadataClient.get("/harvest?subscription=extended&next=token", ONIXMessage.class)).thenReturn(new MetadataClientResponse<>(Optional.empty(), response));

        var metadata = bokbasenGateway.find();
        assertEquals(0, metadata.size());
    }

    ONIXMessage createResponse() {
        var response = objectFactory.createONIXMessage();

        var noProduct = objectFactory.createNoProduct();
        response.setNoProduct(noProduct);

        return response;
    }
}
