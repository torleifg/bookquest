package com.github.torleifg.semanticsearchonnx.gateway.bokbasen;

import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionToken;
import com.github.torleifg.semanticsearchonnx.gateway.repository.ResumptionTokenRepository;
import org.editeur.ns.onix._3_0.reference.ONIXMessage;
import org.editeur.ns.onix._3_0.reference.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BokbasenGatewayTests {

    @Mock
    BokbasenClient bokbasenClient;

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

        when(bokbasenClient.get("/harvest?subscription=extended&after=19700101090000")).thenReturn(ResponseEntity.ok(response));

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

        when(bokbasenClient.get("/harvest?subscription=extended&next=token")).thenReturn(ResponseEntity.ok(response));

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
